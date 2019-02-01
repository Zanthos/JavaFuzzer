package com.buzzfuzz.buzz.decisions;

public class Context {
	private String instancePath;
	private String methodPath;
	private String argumentName;
	private String subTypeOf;
	private String factoryFrom;
	private String constructorOf;
	
	public Context() {
		super();
	}
	
	public String getInstancePath() {
		return instancePath;
	}
	public void setInstancePath(String instancePath) {
		this.instancePath = instancePath;
	}
	public String getMethodPath() {
		return methodPath;
	}
	public void setMethodPath(String methodPath) {
		this.methodPath = methodPath;
	}
	public String getArgumentName() {
		return argumentName;
	}
	public void setArgumentName(String argumentName) {
		this.argumentName = argumentName;
	}
	public String getSubTypeOf() {
		return subTypeOf;
	}
	public void setSubTypeOf(String subTypeOf) {
		this.subTypeOf = subTypeOf;
	}
	public String getFactoryFrom() {
		return factoryFrom;
	}
	public void setFactoryFrom(String factoryFrom) {
		this.factoryFrom = factoryFrom;
	}
	public String getConstructorOf() {
		return constructorOf;
	}
	public void setConstructorOf(String constructorOf) {
		this.constructorOf = constructorOf;
	}
}
