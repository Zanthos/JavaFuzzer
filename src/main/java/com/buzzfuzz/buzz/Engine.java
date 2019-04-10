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

import com.buzzfuzz.rog.decisions.Config;
import com.buzzfuzz.rog.utility.ConfigUtil;

/**
 * Hello world!
 *
 */
public class Engine {

	public static FROG rog;
	
	public static String outputDir = "${project.build.directory}";
    private static final String buzzDir = "fuzzBugs";

    public static Set<Method> methodTargets;

	@SuppressWarnings({ "rawtypes" })
	public static void fuzz(Set<Method> methods) {

        if (rog == null)
            return;

        methodTargets = methods;

		System.out.println("-------------------------------------------------------");
		System.out.println(" F U Z Z  T E S T S");
		System.out.println("-------------------------------------------------------");
		
		// Make reports directory if it doesn't already exist
		File directory = Paths.get(outputDir, buzzDir).toFile();
	    if (! directory.exists()){
	        directory.mkdir();
	    }

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

		List<Thread> runners = new ArrayList<Thread>();
		
		for (Map.Entry<Class, Set<Method>> entry : map.entrySet()) {
			Class key = entry.getKey();
			for (Method method : entry.getValue()) {
				Thread runner = new Thread(new Runner(method, 100));
				runner.start();
				runners.add(runner);
			}
		}

		for (Thread runner : runners) {
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

        // Keep checking exceptions until all new ones have been managed
        File exceptionsDir = Paths.get(outputDir, buzzDir).toFile();
        new ExceptionManager(250, 50).manage(exceptionsDir);
    }

    public static String getMethodName(Method method) {
        // Might eventually need to separate methods by their arguments
        return method.getDeclaringClass().getSimpleName() + '_' + method.getName();
    }
	
	public synchronized static void report(String mthName, int popSize, int crashes, long time, Set<String> crashNames) {
		System.out.println("Fuzzing Method " + mthName);
		System.out.println("Valid runs: " + popSize +
				", Crashes: " + crashes + 
				", Time elapsed: " + time + " ms");
		System.out.println();
		System.out.println("Exceptions found:");
		for (String exception : crashNames) {
			System.out.println("    " + exception);
		}
		System.out.println();
	}
	
	public synchronized static void logTimeout() {
		// This is a complicated problem to solve and log.
		// Eventually should print some kind of log that leads up to the timeout.
		System.out.println("TIMEOUT");
	}

	public synchronized static void log(Throwable e, Config config) {
        // We want to root of the problem or else we'll always see an invokation exception

		Throwable t = e;
		while (t.getCause() != null && t.getCause().getStackTrace().length > 0) {
			t = t.getCause();
        }

//		t.printStackTrace();
		String crashName;
		if (t.getStackTrace().length != 0) {
			StackTraceElement recentCrash = t.getStackTrace()[0];
			crashName = recentCrash.getClassName().substring(recentCrash.getClassName().lastIndexOf('.')+1) + '_' + recentCrash.getMethodName() + '_' + recentCrash.getLineNumber();
		} else {
			crashName = "No_Stacktrace";
		}

		File crashDir = Paths.get(
                outputDir,
                buzzDir,
                getMethodName(config.getCallerMethod()),
				t.getClass().getSimpleName(),
				crashName).toFile();
		if (!crashDir.exists())
			crashDir.mkdirs();
		
		File strace = Paths.get(crashDir.toURI().getPath(), "stacktrace.txt").toFile();
		PrintStream ps;
		try {
			ps = new PrintStream(strace);
			t.printStackTrace(ps);
			
			// In the future, can strip off parts of stacktrace that go into my code
			// e.getStackTrace();
			
			ps.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		// Now we need to print the log and config
        ConfigUtil.log(crashDir.toURI().getPath(), config);
    }
}
