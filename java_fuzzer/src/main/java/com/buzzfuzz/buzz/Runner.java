/**
 * 
 */
package com.buzzfuzz.buzz;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import com.google.common.collect.*;

import org.reflections.Reflections;
import org.reflections.util.Utils;
import org.reflections.Store;

import com.google.common.collect.Multimap;

import edu.emory.mathcs.backport.java.util.Arrays;

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
		int count = 10;
		while (count > 0) {
			try {
				Object instance = getInstance(initClass, new HashSet<Class<?>>());
				initMethod.invoke(instance, randomArgs(initMethod.getParameterTypes(), new HashSet<Class<?>>()));
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
	
	private void log(String msg, int indent) {
		while (indent > 0) {
			System.out.print("    ");
			indent--;
		}
		System.out.println(msg);
	}
	
	private void log(String msg) {
		log(msg, 0);
	}
	
	/**
	 * Attempts to create an instance of the given class by finding a constructor that can create it.
	 * 
	 * @param cls - class to attempt to create an instance of
	 * @return instance of given class or null if no viable Factory was found
	 */
	private Object createFromConstructor(Class<?> cls, Set<Class<?>> chain) {
		log("Attempting to find constructor for " + cls.getCanonicalName(), chain.size());
		Constructor<?>[] constructors = cls.getConstructors();
		if (constructors.length != 0) {
			Constructor<?> cntr = constructors[0];
			try {
				log("Constructor found for " + cls.getCanonicalName(), chain.size());
				log("Arguments are:", chain.size());
				for (Class<?> arg : cntr.getParameterTypes()) {
					log(arg.getSimpleName(), chain.size());
				}
				Object[] args = randomArgs(cntr.getParameterTypes(), chain);
				if (args == null)
					return null;
				return cntr.newInstance(args);
			} catch (InstantiationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		log("No constructor found for " + cls.getCanonicalName(), chain.size());
		return null;
	}
	
	/**
	 * Attempts to create an instance of the given class by finding a factory that can create it.
	 * 
	 * @param cls - class to attempt to create an instance of
	 * @return instance of given class or null if no viable Factory was found
	 */
	private Object createFromFactory(Class<?> cls, Set<Class<?>> chain) {
		log("Attempting to find factory for " + cls.getCanonicalName(), chain.size());
		
//		Set<Method> candidates = Engine.reflections.getMethodsReturn(cls);
		Multimap<String, String> store = Engine.reflections.getStore().get("CarefulMethodParameterScanner");
		
		Set<String> result = new HashSet<String>();
        for (String key : Utils.names(cls)) {
            result.addAll(store.get(key));
        }
        
		Set<Method> candidates = Utils.getMethodsFromDescriptors( result, Engine.reflections.getConfiguration().getClassLoaders());
		for (Method meth : candidates) {
			System.out.println(meth.getName());
		}
		while (true) {
			if (!candidates.iterator().hasNext())
				break;
			
			Method candidate = candidates.iterator().next();
			System.out.println(candidate.getDeclaringClass().getSimpleName());
			// If the candidate hasn't been used before or is within the current class, we can use it
			if (!chain.contains(candidate.getDeclaringClass()) || candidate.getDeclaringClass().equals(cls)) {
				log("Found factory " + candidate.getDeclaringClass().getCanonicalName() + " for " + cls.getCanonicalName(), chain.size());
				Object instance = null;
				if (!Modifier.isStatic(candidate.getModifiers())) {
					instance = getInstance(candidate.getDeclaringClass(), chain);
					if (instance == null) {
						candidates.remove(candidate);
						continue;
					}
				}
				try {
					log("Arguments are:", chain.size());
					for (Class<?> arg : candidate.getParameterTypes()) {
						log(arg.getSimpleName(), chain.size());
					}
					Object[] args = randomArgs(candidate.getParameterTypes(), chain);
					if (args == null) {
						candidates.remove(candidate);
						continue;
					}
					
					Object outcome = candidate.invoke(instance, args);
					if (outcome != null) {
						return outcome;
					} else {
						candidates.remove(candidate);
					}
						
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
			} else {
				candidates.remove(candidate);
			}
		}
		
		log("No factory found for " + cls.getCanonicalName(), chain.size());
		return null;
	}
	
	/**
	 * Attempts to create an instance of the given class by creating a subclass of the class.
	 * 
	 * @param cls - class to attempt to create an instance of
	 * @return instance of given class or null if no viable subClass was found
	 */
	private Object createFromSubClass(Class cls, Set<Class<?>> chain) {
		log("Attempting to find subClass for " + cls.getCanonicalName(), chain.size());
		// Get list of subtypes
		Set<Class<?>> subTypes = Engine.reflections.getSubTypesOf(cls);
		
		Class<?> choice = null;
		
		while (true) {
			if (!subTypes.iterator().hasNext()) {
				break;
			}
			choice = subTypes.iterator().next();
			if (chain.contains(choice)) {
				subTypes.remove(choice);
			} else {
				log("Subclass " + choice.getCanonicalName() + " found for " + cls.getCanonicalName(), chain.size());
				Object outcome = getInstance(choice, chain);
				if (outcome != null) {
					return outcome;
				} else {
					subTypes.remove(choice);
				}
			}
		}
		
		log("No subClass found for " + cls.getCanonicalName(), chain.size());
		return null;

//		Class genericType = (Class)((ParameterizedType)cls.getGenericSuperclass()).getActualTypeArguments()[0];

//		List<Class<?>> subTypes = new ArrayList<Class<?>>();
//		subTypes.addAll(Engine.reflections.getSubTypesOf(cls));
//
////		List<Class<? extends test>> subTypes = Arrays.asList((Class<?>[]) Engine.reflections.getSubTypesOf(cls).toArray());
//		Object resolution = null;
//		int index = -1;
//		while(resolution == null) {
//			if (index >= 0)
//				subTypes.remove(index);
//			if (subTypes.size() == 0)
//				return null;
//			index = rng.fromRange(0, subTypes.size()-1);
//			Class<?> subType = subTypes.get(index);
//			resolution = createInstance(subType);
//		}
//		
//		return resolution;
		
//		Class array = Array.newInstance(cls.getComponentType(), 0).getClass();
//		return Arrays.asList(getInstance(array));
	}
	
	@SuppressWarnings("rawtypes")
	private Object createInstance(Class cls, Set<Class<?>> oldChain) {
		Set<Class<?>> chain = new HashSet<Class<?>>(oldChain);
		chain.add(cls);
		log("Creating instance of type " + cls.getName(), chain.size());
		log(Arrays.toString(chain.toArray()), chain.size());
		
		Object inst = null;
		
		// Create instance of class
		inst = createFromConstructor(cls, chain);
		if (inst == null) {
			inst = createFromFactory(cls, chain);
			if (inst == null) {
				inst = createFromSubClass(cls, chain);
				if (inst == null)
					log("Could not find a way to get an instance of this class.", chain.size());
			}
		}
		
		// TODO: Mutate the class
			
		return inst;
	}

	/**
	 * Creates random instances of the given classes for the purpose of invoking methods
	 * 
	 * @param types - array of the argument types that should be randomized
	 * @return - Object array of instances for the given class array
	 */
	@SuppressWarnings("rawtypes")
	private Object[] randomArgs(Class[] types, Set<Class<?>> chain) {
		Object[] instances = new Object[types.length];
		for (int i = 0; i < types.length; i++) {
			instances[i] = getInstance(types[i], chain);
			if (instances[i] == null)
				return null;
			log("Argument type " + types[i].getName() + " evaluated as " + instances[i].toString(), chain.size());
		}
		return instances;
	}

	@SuppressWarnings("rawtypes")
	private Object getInstance(Class cls, Set<Class<?>> chain) {
		if (cls.equals(int.class)) {
			return rng.getInt();
		} else if (cls.equals(long.class)) {
			return rng.getLong();
		} else if (cls.equals(char.class)) {
			return rng.getChar();
		} else if (cls.equals(float.class)) {
			return rng.getFloat();
		} else if (cls.equals(double.class)) {
			return rng.getDouble();
		} else if (cls.equals(boolean.class)) {
			return rng.getBool();
		} else if (cls.equals(byte.class)) {
			return rng.getByte();
		} else if (cls.equals(short.class)) {
			return rng.getShort();
		} else if (cls.equals(String.class)) {
			return rng.getString();
		} else if (cls.isArray()) {
			Class type = cls.getComponentType();
			int length = rng.fromRange(0, 10);
			Object array = Array.newInstance(type, length);
			for (int i = 0; i < length; i++) {
				Array.set(array, i, getInstance(type, chain));
			}
			return Array.newInstance(type, 0).getClass().cast(array);
		}
		else if (cls.isEnum()) {
			Object[] values = cls.getEnumConstants();
			int index = rng.fromRange(0, values.length - 1);
			return values[index];
		} else {
			return createInstance(cls, chain);
		}
	}
}
