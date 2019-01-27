/**
 * 
 */
package com.buzzfuzz.buzz;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.buzzfuzz.buzz.traversal.ClassPkg;
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
		rng = new RNG();
		initClass = cls;
		initMethod = method;
	}
	
	public void run() {
		int count = 50;
		while (count > 0) {
			try {
//				Type[] genericParameterTypes = initMethod.getGenericParameterTypes();
//				
//				for(Type genericParameterType : genericParameterTypes){
//				    if(genericParameterType instanceof ParameterizedType){
//				        ParameterizedType aType = (ParameterizedType) genericParameterType;
//				        Type[] parameterArgTypes = aType.getActualTypeArguments();
//				        for(Type parameterArgType : parameterArgTypes){
//				        		Class<?> parameterArgClass = (Class) parameterArgType;
//				        		System.out.println(parameterArgClass.getName());
//				        }
//				    }
//				}
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
