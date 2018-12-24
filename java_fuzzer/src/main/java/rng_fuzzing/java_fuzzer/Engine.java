package rng_fuzzing.java_fuzzer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import rng_fuzzing.java_fuzzer.Fuzz;

/**
 * Hello world!
 *
 */
public class Engine 
{
    @SuppressWarnings("rawtypes")
	public static void run( String[] args )
    {
    	 	Reflections reflections = new Reflections(new ConfigurationBuilder()
    	            .setUrls(ClasspathHelper.forPackage(args[0]))
    	            .setScanners(new MethodAnnotationsScanner()));
        Set<Method> methods = reflections.getMethodsAnnotatedWith(Fuzz.class);
        Map<Class, Set<Method>> map = new HashMap<Class, Set<Method>>();
        
        for (Method method : methods) {
        		method.setAccessible(true);
        		Class cls = method.getDeclaringClass();
    			if ( map.containsKey(cls) ) {
    				map.get(cls).add(method);
    			}
    			else {
    				Set<Method> mth = new HashSet<Method>();
    				mth.add(method);
    				map.put(cls, mth);
    			}
        }
        
        for (Map.Entry<Class, Set<Method>> entry : map.entrySet()) {
		    fuzz(entry);
        	}
    }
    
    @SuppressWarnings("rawtypes")
	private static void fuzz(Map.Entry<Class, Set<Method>> fuzzMethods) {
    		Object obj = null;
    		Constructor[] constructors = fuzzMethods.getKey().getConstructors();
    		if (constructors != null) {
    			Constructor cntr = constructors[0];
    		
			try {
				obj = cntr.newInstance(randomArgs(cntr.getParameterTypes()));
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
    	
    		for (Method method : fuzzMethods.getValue()) {
	    		System.out.println(method.getName() + "\t---------");
	    		try {
					method.invoke(obj, randomArgs(method.getParameterTypes()));
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
    }
    
    @SuppressWarnings("rawtypes")
	private static Object[] randomArgs(Class[] types) {
    		Object[] instances = new Object[types.length];
    		for (int i=0; i < types.length; i++) {
    			System.out.println("\t" + types[i].getTypeName());
    			instances[i] = getInstance(types[i]);
    		}
    		return instances;
    }
    
    @SuppressWarnings("rawtypes")
	private static Object getInstance(Class cls) {
	    	if (cls.equals(int.class)) {
	    		return 1;
	    	}
	    	else if (cls.equals(long.class)) {
	    		return 1L;
	    	}
	    	else if (cls.equals(char.class)) {
	    		return 'a';
	    	}
	    	else if (cls.equals(float.class)) {
	    		return (float)1.0;
	    	}
	    	else if (cls.equals(double.class)) {
	    		return (double)1.0;
	    	}
	    	else if (cls.equals(boolean.class)) {
	    		return true;
	    	}
	    	else if (cls.equals(byte.class)) {
	    		return (byte)1;
	    	}
	    	else if (cls.equals(short.class)) {
	    		return (short)1;
	    	}
	    	else return null;
    }
}
