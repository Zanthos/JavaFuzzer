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
    private Config config;

	public Runner(Method method, int nruns) {
        this(method, nruns, null);
    }

    public Runner(Method method, int nruns, Config config) {
        this.initClass = method.getDeclaringClass();
		this.initMethod = method;
        this.popSize = nruns;
        this.config = config;
        if (this.config != null)
            this.config.setCallerMethod(method);
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

        if (this.config == null) {
            this.config = parseConfig(initMethod);
            this.config.setCallerMethod(this.initMethod);
        }

		Set<String> crashes = new HashSet<String>();
		int crashCount = 0;
		startTime = java.lang.System.currentTimeMillis();

		int count = 0;
		while (count < popSize) {
            Config runningConfig = this.config.clone();
			try {
                Object instance = Engine.rog.getInstance(initClass, runningConfig);
                Object[] args = Engine.rog.getArgInstancesFor(initMethod, runningConfig);
				initMethod.invoke(instance, args);

				// Eventually add this to the log as well and use for crash-free corpus
//				System.out.println();
//				System.out.println("Fuzzing finished and created: " + String.valueOf(result));
//				System.out.println();
			} catch (Exception e) {
                Engine.log(e, runningConfig);
			}
			count++;
		}

		// Engine.report(initMethod.getName(), popSize, crashCount, getEllapsedTime(), crashes);
		
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
