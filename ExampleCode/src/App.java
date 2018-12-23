import custom.BusinessLogic;
import rng_fuzzing.java_fuzzer.Engine;


public class App {

	public static void main(String[] args) {
		
		String[] pkg = {BusinessLogic.class.getPackage().getName()};
		Engine.main(pkg);

	}

}
