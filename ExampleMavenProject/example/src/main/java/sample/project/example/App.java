package sample.project.example;

import rng_fuzzing.java_fuzzer.Fuzz;
import test.BusinessLogic;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        BusinessLogic logic = new BusinessLogic();
        logic.exampleFunction3("yes", true);
    }
	
	
}
