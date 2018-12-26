package custom;

import rng_fuzzing.java_fuzzer.Fuzz;

public class BusinessLogic {
	
	public enum State {
		NORTH_CAROLINA, ALASKA, FLORIDA, MISSISSIPPI
	}

	public BusinessLogic() {
		super();
	}
	
	@Fuzz
	public void exampleFunction1(int iTest, State eTest) throws Exception {
		System.out.println("\tRunning with values: " + iTest + ", " + eTest);
//		if (iTest == 0 && fTest < 0) {
//			throw new Exception();
//		}
	}
	
	@Fuzz
	private void exampleFunction2(String sTest, int[] saTest) {
		System.out.println("\tRunning with values: " + sTest + ", " + saTest);
	}
	
	@SuppressWarnings("unused")
	private void exampleFunction3(String sTest, boolean bTest) {
		
	}
}
