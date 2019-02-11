package com.buzzfuzz.buzz;

import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

/**
 * Hello world!
 *
 */
public class Engine {
	
	public static Reflections reflections;
	
	public static String outputDir = "${project.build.directory}";
	private static final String buzzDir = "buzz-reports";
	
	private static boolean writing = false;
	
	@SuppressWarnings({ "rawtypes" })
	public static void run(Set<Method> methods, Reflections reflect) {
		
		// Make reports directory if it doesn't already exist
		File directory = Paths.get(outputDir, buzzDir).toFile();
	    if (! directory.exists()){
	        directory.mkdir();
	    }

		reflections = reflect;
		
		Map<Class, Set<Method>> map = new HashMap<Class, Set<Method>>();

		// TODO: now that the runner fuzzes by method, don't need to sort by Class
		for (Method method : methods) {
			method.setAccessible(true);
			Class cls = method.getDeclaringClass();
			if (map.containsKey(cls)) {
				map.get(cls).add(method);
			} else {
				Set<Method> mth = new HashSet<Method>();
				mth.add(method);
				map.put(cls, mth);
			}
		}

		List<Runner> runners = new ArrayList<Runner>();
		
		for (Map.Entry<Class, Set<Method>> entry : map.entrySet()) {
			Class key = entry.getKey();
			for (Method method : entry.getValue()) {
				Runner runner = new Runner(key, method, 500);
				runner.start();
				runners.add(runner);
			}
		}
		
		for (Runner runner : runners) {
			try {
//				while (runner.isAlive()) {
//					if (runner.getEllapsedTime() > 10000) {
//						logTimeout();
//						Runner battonPass = new Runner(runner);
//						runner.interrupt();
//						runner = battonPass;
//						runner.start();
//					}
//				}
					
				runner.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void logTimeout() {
		// This is a complicated problem to solve and log.
		// Eventually should print some kind of log that lead up to the timeout.
		System.out.println("TIMEOUT");
	}
	
	public static void log(Exception t, long seed) {
		t.printStackTrace();
		Throwable e = t;
		while (e.getCause() != null && e.getCause().getStackTrace().length > 0) {
			e = e.getCause();
		}
		// Wait until we aren't writing
		while(writing);
		
		// start lock (should probably do this in a better way
		writing = true;
		
		StackTraceElement recentCrash = e.getStackTrace()[0];
		
		File crashDir = Paths.get(
				outputDir, 
				buzzDir, 
				recentCrash.getClassName().substring(recentCrash.getClassName().lastIndexOf('.')+1) + '.' + recentCrash.getMethodName() + "():" + recentCrash.getLineNumber(),
				e.getClass().getSimpleName()).toFile();
		if (!crashDir.exists())
			crashDir.mkdirs();
		
		File example = Paths.get(crashDir.getPath(), Long.toString(seed)).toFile();
		if (example.exists())
			example.delete();
			
		example.mkdir();
		File strace = Paths.get(example.getPath(), "stacktrace.txt").toFile();
		PrintStream ps;
		try {
			ps = new PrintStream(strace);
			e.printStackTrace(ps);
			
			// In the future, can strip off parts of stacktrace that go into my code
			// e.getStackTrace();
			
			ps.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		// end lock
		writing = false;
		
	}
}
