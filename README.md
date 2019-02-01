## Buzz Fuzz Engine

This repo contains a fuzzing framework for Java that allows for easily fuzz testing your code with limitted input from the user.

Through the use of Java annotations, the user can specify which methods they would like to fuzz

For instance: 
```
	@Fuzz
	private void exampleFunction2(String sTest, DataClass[] daTest) {
		System.out.println("\tRunning with values: " + sTest + ", " + Arrays.toString(daTest));
	}
```

## How to use it

This project takes the form of a maven plugin that provides the fuzzing lifecycle combined with a maven dependency for adding the relevant annotations to your project

You can use the annotations by adding the following you your pom.xml

'''
	<dependencies>
        <dependency>
	      <groupId>com.buzzfuzz.buzztools</groupId>
	      <artifactId>buzz-tools</artifactId>
	      <version>0.0.1-SNAPSHOT</version>
	    </dependency>
    </dependencies>
'''

You can then add the maven plugin that drives these annotations by adding the following to your pom.xml

'''
	<plugin>
		<groupId>com.buzzfuzz.buzz</groupId>
		<artifactId>buzz-maven-plugin</artifactId>
		<version>0.0.1-SNAPSHOT</version>
		<executions>
			<execution>
				<goals>
				<goal>test</goal>
				</goals>
			</execution>
		</executions>
	</plugin>
'''

## Configure its execution

In addition to allowing the engine to attempt to build example instances of the methods on its own, you can also constrain its decisions and actions based on an optional config file. This configuration can be specified via the FuzzConstraint annotation as follows:

'''
@Fuzz
@FuzzConstraint(objectPath="Color.Integer", lowerbound=0)
@FuzzConstraint(configFile="fuzzConfig.xml")
	private void exampleFunction2(String sTest, DataClass[] daTest) {
		System.out.println("\tRunning with values: " + sTest + ", " + Arrays.toString(daTest));
	}
'''

As you can see, the configuration can either be set directly in the annotation using an objectPath followed by certain constraints like lowerBound, or by specifying a file that indicates these constraints in a similar fassion. Below is what one of these configuration files should look like

'''
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <scopes>
        <scope>
            <target>
                <instancePath>RadarChart</instancePath>
            </target>

            <constraint>
                <nullProb>0.1</nullProb>
            </constraint>

            <scopes>
                <scope>
                    <target>
                    		<instancePath>Color</instancePath>
                    </target>
                    
                    <constraint>
                    		<nullProb>0.8</nullProb>
                    </constraint>
                </scope>
            </scopes>
        </scope>
    </scopes>
</config>
'''