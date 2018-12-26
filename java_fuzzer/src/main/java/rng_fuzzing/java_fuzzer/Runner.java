/**
 * 
 */
package rng_fuzzing.java_fuzzer;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
		Object inst = null;
		Constructor[] constructors = cls.getConstructors();
		if (constructors.length != 0) {
			Constructor cntr = constructors[0];
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
		return inst;
	}
	
	public void run() {
		int count = 10;
    		while(count > 0) {
	    		try {
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
    		for (int i=0; i < types.length; i++) {
    			instances[i] = getInstance(types[i]);
    		}
    		return instances;
    }
    
    @SuppressWarnings("rawtypes")
	private Object getInstance(Class cls) {
	    	if (cls.equals(int.class)) {
	    		return rng.getInt();
	    	}
	    	else if (cls.equals(long.class)) {
	    		return rng.getLong();
	    	}
	    	else if (cls.equals(char.class)) {
	    		return rng.getChar();
	    	}
	    	else if (cls.equals(float.class)) {
	    		return rng.getFloat();
	    	}
	    	else if (cls.equals(double.class)) {
	    		return rng.getDouble();
	    	}
	    	else if (cls.equals(boolean.class)) {
	    		return rng.getBool();
	    	}
	    	else if (cls.equals(byte.class)) {
	    		return rng.getByte();
	    	}
	    	else if (cls.equals(short.class)) {
	    		return rng.getShort();
	    	}
	    	else if (cls.equals(String.class)) {
	    		return rng.getString();
	    	}
	    	else if (cls.isArray()) {
	    		Class type = cls.getComponentType();
	    		int length = rng.fromRange(0, 10);
	    		Object array = Array.newInstance(type, length);
	    		for (int i=0; i < length; i++) {
	    			Array.set(array, i, getInstance(type));
	    		}
	    		System.out.println(array.toString());
	    		return Array.newInstance(type, 0).getClass().cast(array);
	    	}
	    	else if (cls.isEnum()) {
	    		Object[] values = cls.getEnumConstants();
	    		int index = rng.fromRange(0, values.length - 1);
	    		return values[index];
	    	}
	    	else {
	    		return createInstance(cls);
	    	}
    }
}
