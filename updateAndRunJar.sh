cd java_fuzzer
mvn package shade:shade
cd ..
javac -cp ".:/Users/Rockett/Projects/Fuzzing/JavaFuzzing/java_fuzzer/target/java_fuzzer-0.0.1-SNAPSHOT.jar" ExampleCode/src/custom/App.java ExampleCode/src/custom/BusinessLogic.java
cd ExampleCode/src
java -cp ".:/Users/Rockett/Projects/Fuzzing/JavaFuzzing/java_fuzzer/target/java_fuzzer-0.0.1-SNAPSHOT.jar" custom.App
cd ../..
