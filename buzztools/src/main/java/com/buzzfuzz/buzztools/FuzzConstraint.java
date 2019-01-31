package com.buzzfuzz.buzztools;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(FuzzConstraints.class)
public @interface FuzzConstraint {
	String objectPath() default "";
	int lowerBound() default Integer.MIN_VALUE;
	int upperBound() default Integer.MAX_VALUE;
	//etc...
	String configFile() default "";
}

