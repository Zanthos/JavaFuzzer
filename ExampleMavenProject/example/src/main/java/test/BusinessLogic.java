package test;

import java.util.Arrays;

public class BusinessLogic {
	
	public enum State {
		NORTH_CAROLINA, ALASKA, FLORIDA, MISSISSIPPI
	}

	public class DataClass {
		private String name;
		private int age;
		private State location;
		
		public DataClass(String name, int age, State location) {
			this.name = name;
			this.age = age;
			this.location = location;
		}
		
		public String toString() {
			return name + " is " + age + " years old and lives in " + location;
		}
	}

	public BusinessLogic() {
		super();
	}

	@com.buzzfuzz.buzztools.Fuzz
	public void exampleFunction1(int iTest, State eTest) throws Exception {
		System.out.println("\tRunning with values: " + iTest + ", " + eTest);
	}

	@com.buzzfuzz.buzztools.Fuzz
	private void exampleFunction2(String sTest, DataClass[] daTest) {
		System.out.println("\tRunning with values: " + sTest + ", " + Arrays.toString(daTest));
	}

	@SuppressWarnings("unused")
	public void exampleFunction3(String sTest, boolean bTest) {

	}
}
