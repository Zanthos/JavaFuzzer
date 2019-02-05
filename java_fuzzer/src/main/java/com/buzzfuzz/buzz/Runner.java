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

	@SuppressWarnings("rawtypes")
	public Runner(Class cls, Method method) {
		super();
		initClass = cls;
		initMethod = method;
		rng = new RNG();
	}
	
	public void run() {
		
		rng.parseConfig(initMethod);
		
		int count = 500;
		while (count > 0) {
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
			count--;
		}
	}
}
