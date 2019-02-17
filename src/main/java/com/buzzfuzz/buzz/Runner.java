/**
 * 
 */
package com.buzzfuzz.buzz;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.buzzfuzz.buzz.traversal.InstanceDispatcher;

/**
 * @author Johnny Rockett
 *
 */
public class Runner extends Thread {

	private Class<?> initClass;
	private Method initMethod;
	private long startTime;
	private int popSize;

	public Runner(Class<?> cls, Method method, int nruns) {
		super();
		this.initClass = cls;
		this.initMethod = method;
		this.popSize = nruns;
	}
	
	public Runner(Runner runner) {
		this(runner.initClass, runner.initMethod, runner.popSize);
	}
	
	public long getEllapsedTime() {
		return java.lang.System.currentTimeMillis() - startTime;
	}
	
	public void run() {
		
		// Each run should create a population of purely random configs
		// and then in a loop, (grade them all, breed them, and mutate them)
		
//		Config baseConfig = RNG.parseConfig(initMethod);
		
		// Mutate so that we try some new things
//		rng.mutateConfig();
		
		Set<String> crashes = new HashSet<String>();
		int crashCount = 0;
		startTime = java.lang.System.currentTimeMillis();
		
		int count = 0;
		while (count < popSize) {
			
			RNG rng = new RNG();
			
			rng.setConfig(RNG.parseConfig(initMethod));
			try {
				Object instance = new InstanceDispatcher(rng).getInstance(initClass);
				initMethod.invoke(instance, new InstanceDispatcher(rng)
						.randomArgs(initMethod.getGenericParameterTypes()));
				
				// Eventually add this to the log as well and use for crash-free corpus
//				System.out.println();
//				System.out.println("Fuzzing finished and created: " + String.valueOf(result));
//				System.out.println();
			} catch (Exception e) {
				rng.logCrash(e);
			} finally {
				crashes.addAll(rng.getCrashes());
				crashCount += rng.getCrashCount();
			}
			count++;
		}
		
		Engine.report(initMethod.getName(), popSize, crashCount, getEllapsedTime(), crashes);
		
//		rng.printConfig();
	}
}
