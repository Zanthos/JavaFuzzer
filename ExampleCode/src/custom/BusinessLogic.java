package custom;

import rng_fuzzing.java_fuzzer.Fuzz;

public class BusinessLogic {

	public BusinessLogic() {
		super();
	}
	
	@Fuzz
	public void exampleFunction1(int iTest, float fTest) throws Exception {
		System.out.println("\tRunning with values: " + iTest + ", " + fTest);
		if (iTest == 0 && fTest < 0) {
			throw new Exception();
		}
	}
	
	@Fuzz
	private void exampleFunction2(String sTest, boolean bTest) {
		System.out.println("\tRunning with values: " + sTest + ", " + bTest);
	}
	
	@SuppressWarnings("unused")
	private void exampleFunction3(String sTest, boolean bTest) {
		
	}
}
