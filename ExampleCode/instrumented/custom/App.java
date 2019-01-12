package custom;
import rng_fuzzing.java_fuzzer.Engine;


public class App {

	public static void main(String[] args) {
		System.out.println("Hello, World!");
		String[] pkg = { BusinessLogic.class.getPackage().getName() };
		Engine.run(pkg);

	}

}
