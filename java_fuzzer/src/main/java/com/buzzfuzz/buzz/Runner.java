/**
 * 
 */
package com.buzzfuzz.buzz;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.buzzfuzz.buzz.traversal.InstanceDispatcher;

/**
 * @author Johnny Rockett
 *
 */
public class Runner extends Thread {

	private Class<?> initClass;
	private Method initMethod;
	private RNG rng;
	private long startTime;
	private int nruns;

	@SuppressWarnings("rawtypes")
	public Runner(Class cls, Method method, int nruns) {
		super();
		initClass = cls;
		initMethod = method;
		rng = new RNG();
		this.nruns = nruns;
	}
	
	public Runner(Runner runner) {
		super();
		this.initClass = runner.initClass;
		this.initMethod = runner.initMethod;
		this.rng = runner.rng;
		this.nruns = runner.nruns;
	}
	
	public long getEllapsedTime() {
		return java.lang.System.currentTimeMillis() - startTime;
	}
	
	public void run() {
		
		rng.parseConfig(initMethod);
		
		// Mutate so that we try some new things
		rng.mutateConfig();
		
		while (nruns > 0) {
			startTime = java.lang.System.currentTimeMillis();
			try {
				Object instance = new InstanceDispatcher(rng).getInstance(initClass);
				Object result = initMethod.invoke(instance, new InstanceDispatcher(rng)
						.randomArgs(initMethod.getParameterTypes(), initMethod.getGenericParameterTypes()));
				System.out.println();
				System.out.println("Fuzzing finished and created: " + result.toString());
				System.out.println();
				
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			nruns--;
		}
	}
}
