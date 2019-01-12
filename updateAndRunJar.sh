cd java_fuzzer
mvn package shade:shade
cd ..
javac -cp ".:/Users/Rockett/Projects/Fuzzing/JavaFuzzing/java_fuzzer/target/java_fuzzer-0.0.1-SNAPSHOT.jar" ExampleCode/src/custom/*.java
java -jar ./jacoco-0.8.2/lib/jacococli.jar instrument ./ExampleCode/src/ --dest ./ExampleCode/instrumented/
cd ExampleCode/bin
java -cp "./../instrumented:/Users/Rockett/Projects/Fuzzing/JavaFuzzing/java_fuzzer/target/java_fuzzer-0.0.1-SNAPSHOT.jar:./../../jacoco-0.8.2/lib/jacocoagent.jar" custom.App
cd ../..
java -jar jacoco-0.8.2/lib/jacococli.jar report ExampleCode/bin/jacoco.exec --classfiles ExampleCode/src/ --sourcefiles ExampleCode/src/ --html jacoco-report
open ./jacoco-report/index.html