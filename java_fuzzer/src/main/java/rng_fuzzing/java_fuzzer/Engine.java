package rng_fuzzing.java_fuzzer;

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
        
        //TODO: now that the runner fuzzes by method, don't need to sort by Class
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
        		Class key = entry.getKey();
        		for (Method method : entry.getValue()) {
	        		Runner runner = new Runner(key, method);
			    runner.start();
        		}
        	}
    }
}
