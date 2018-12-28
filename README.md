## Java Fuzzing Framework

This repo contains a fuzzing framework for Java that allows for easily switching out different fuzzing techniques for camparisons and optimizations.

Through the use of Java annotations, the user can specify the algorithms wanted for fuzzing. (Strategy pattern)

With the current master branch, adding the @Fuzz annotation to a method will cause it to be run simply with random values, both primitive and complex. This instance of the monte carlo fuzzing technique is considered the default strategy.

For instance: 
```
	@Fuzz
	private void exampleFunction2(String sTest, DataClass[] daTest) {
		System.out.println("\tRunning with values: " + sTest + ", " + Arrays.toString(daTest));
	}
```

Throughout a semester of testing out different techniques, I hope to add to this open source collection and test out new techniques as well.