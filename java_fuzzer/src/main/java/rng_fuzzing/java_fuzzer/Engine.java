package rng_fuzzing.java_fuzzer;

import java.lang.reflect.Method;
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
    public static void main( String[] args )
    {
    	 	Reflections reflections = new Reflections(new ConfigurationBuilder()
    	            .setUrls(ClasspathHelper.forPackage(args[0]))
    	            .setScanners(new MethodAnnotationsScanner()));
    	        Set<Method> methods = reflections.getMethodsAnnotatedWith(Fuzz.class);
    	        System.out.println(methods);
    }
}
