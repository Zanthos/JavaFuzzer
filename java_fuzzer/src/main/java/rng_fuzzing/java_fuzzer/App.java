package rng_fuzzing.java_fuzzer;

import java.lang.reflect.Method;
import java.util.Set;

import org.reflections.Reflections;
import rng_fuzzing.java_fuzzer.Fuzz;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    		Reflections reflections = new Reflections("my.package.prefix");
    		Set<Method> deprecateds = reflections.getMethodsAnnotatedWith(Fuzz.class);
        System.out.println( deprecateds.toString() );
    }
}
