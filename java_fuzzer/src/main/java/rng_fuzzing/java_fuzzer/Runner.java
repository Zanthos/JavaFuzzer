/**
 * 
 */
package rng_fuzzing.java_fuzzer;

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
		if (constructors != null) {
			Constructor cntr = constructors[0];
		
			try {
				inst = cntr.newInstance(randomArgs(cntr.getParameterTypes()));
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
    	
    		System.out.println(method.getName() + "\t---------");
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
    }
    
    @SuppressWarnings("rawtypes")
	private Object[] randomArgs(Class[] types) {
    		Object[] instances = new Object[types.length];
    		for (int i=0; i < types.length; i++) {
    			System.out.println("\t" + types[i].getTypeName());
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
	    	else {
	    		return createInstance(cls);
	    	}
    }
}
