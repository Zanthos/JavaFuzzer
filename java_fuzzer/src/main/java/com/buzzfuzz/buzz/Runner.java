/**
 * 
 */
package com.buzzfuzz.buzz;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Johnny Rockett
 *
 */
public class Runner extends Thread {

	private Object instance;
	private Method method;
	private RNG rng;

	@SuppressWarnings("rawtypes")
	public Runner(Class cls, Method method) {
		super();
		rng = new RNG();
		instance = createInstance(cls);
		this.method = method;
	}

	@SuppressWarnings("rawtypes")
	private Object createInstance(Class cls) {
		System.out.println("Creating instance of type " + cls.getName());
		Object inst = null;
		Constructor[] constructors = cls.getDeclaredConstructors();
		System.out.println(cls.getName() + ' ' + constructors.length);
		if (constructors.length != 0) {
			Constructor cntr = constructors[0];
			cntr.setAccessible(true);
			try {
				Object[] args = randomArgs(cntr.getParameterTypes());
				inst = cntr.newInstance(args);
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
		else {
			Set<Class<?>> subTypes = Engine.reflections.getSubTypesOf(cls);
			inst = createInstance(subTypes.iterator().next());
			
//			Class genericType = (Class)((ParameterizedType)cls.getGenericSuperclass()).getActualTypeArguments()[0];

//			List<Class<?>> subTypes = new ArrayList<Class<?>>();
//			subTypes.addAll(Engine.reflections.getSubTypesOf(cls));
//
////			List<Class<? extends test>> subTypes = Arrays.asList((Class<?>[]) Engine.reflections.getSubTypesOf(cls).toArray());
//			Object resolution = null;
//			int index = -1;
//			while(resolution == null) {
//				if (index >= 0)
//					subTypes.remove(index);
//				if (subTypes.size() == 0)
//					return null;
//				index = rng.fromRange(0, subTypes.size()-1);
//				Class<?> subType = subTypes.get(index);
//				resolution = createInstance(subType);
//			}
//			
//			return resolution;
			
//			Class array = Array.newInstance(cls.getComponentType(), 0).getClass();
//			return Arrays.asList(getInstance(array));
		}
		return inst;
	}

	public void run() {
		int count = 10;
		while (count > 0) {
			try {
				System.out.println(instance);
				method.invoke(instance, randomArgs(method.getParameterTypes()));
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

	@SuppressWarnings("rawtypes")
	private Object[] randomArgs(Class[] types) {
		Object[] instances = new Object[types.length];
		for (int i = 0; i < types.length; i++) {
			instances[i] = getInstance(types[i]);
			System.out.println("Argument type " + types[i].getName() + " evaluated as " + instances[i].toString());
		}
		return instances;
	}

	@SuppressWarnings("rawtypes")
	private Object getInstance(Class cls) {
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
				Array.set(array, i, getInstance(type));
			}
			return Array.newInstance(type, 0).getClass().cast(array);
		}
		else if (cls.isEnum()) {
			Object[] values = cls.getEnumConstants();
			int index = rng.fromRange(0, values.length - 1);
			return values[index];
		} else {
			return createInstance(cls);
		}
	}
}
