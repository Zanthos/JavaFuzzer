/**
 * 
 */
package com.buzzfuzz.buzz;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.buzzfuzz.buzztools.FuzzConstraint;
import com.buzzfuzz.buzztools.FuzzConstraints;
import com.buzzfuzz.rog.decisions.Config;
import com.buzzfuzz.rog.utility.ConfigUtil;

/**
 * @author Johnny Rockett
 *
 */
public class Runner implements Runnable {

	private Class<?> initClass;
	private Method initMethod;
	private long startTime;
	private int popSize;

	public Runner(Method method, int nruns) {
        this(method, nruns, null);
    }

    public Runner(Method method, int nruns, Config config) {
        this.initClass = method.getDeclaringClass();
		this.initMethod = method;
        this.popSize = nruns;
        // Run with config
    }
	
	public Runner(Runner runner) {
		this(runner.initMethod, runner.popSize);
	}
	
	public long getEllapsedTime() {
		return java.lang.System.currentTimeMillis() - startTime;
	}
	
	public void run() {
        execute();
    }

    public void execute() {

		// To start with, we don't give a config so that one is generated as it goes
		// and then in a loop, (grade them all, breed them, and mutate them)
		
//		Config baseConfig = RNG.parseConfig(initMethod);

		// Mutate so that we try some new things
//		rng.mutateConfig();
		
		Set<String> crashes = new HashSet<String>();
		int crashCount = 0;
		startTime = java.lang.System.currentTimeMillis();
		
		int count = 0;
		while (count < popSize) {

            Config config = parseConfig(initMethod);
            config.setCallerMethod(initMethod);
			try {
                Object instance = Engine.rog.getInstance(initClass, config);
                Object[] args = Engine.rog.getArgInstancesFor(initMethod, config);
				initMethod.invoke(instance, args);

				// Eventually add this to the log as well and use for crash-free corpus
//				System.out.println();
//				System.out.println("Fuzzing finished and created: " + String.valueOf(result));
//				System.out.println();
			} catch (Exception e) {
                Engine.log(e, config);
			}
			count++;
		}
		
		Engine.report(initMethod.getName(), popSize, crashCount, getEllapsedTime(), crashes);
		
//		rng.printConfig();
    }

    public static Config parseConfig(Method method) {
        Config config = new Config();
		FuzzConstraints constraintsAnnotation = method.getAnnotation(FuzzConstraints.class);
		if (constraintsAnnotation != null) {
			FuzzConstraint[] constraints = constraintsAnnotation.value();
			
			for( FuzzConstraint constraint : constraints ) {
				evaluateConstraint(config, constraint);
			}
		} else {
			// Maybe there is only one
			FuzzConstraint constraintAnnotation = method.getAnnotation(FuzzConstraint.class);
			if (constraintAnnotation != null) {
				evaluateConstraint(config, constraintAnnotation);
			}
		}
		return config;
	}
	
	private static void evaluateConstraint(Config config, FuzzConstraint constraint) {
		// Also need to add pairs given in annotations
		String configFile = constraint.configFile();
		if (configFile != null && !configFile.isEmpty())
			ConfigUtil.mergeNewTree(config.getTree(), ConfigUtil.createConfigFromFile(configFile));
    }
}
