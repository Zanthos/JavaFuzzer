package com.buzzfuzz.buzz.traversal;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class FuzzConstructorFinder extends InstanceFinder {

	public FuzzConstructorFinder(InstanceDispatcher dispatcher) {
		super(dispatcher);
		logName = "Fuzz Constructor";
	}

	@Override
	public Object attemptPath(Object choice) {
		Method fuzzCntr = (Method)choice;
		Object[] args = new InstanceDispatcher(this).randomArgs(fuzzCntr.getParameterTypes(), fuzzCntr.getGenericParameterTypes());
		if (args == null)
			return null;
		
		Object instance = null;
		try {
			instance = fuzzCntr.invoke(null, args);
		} catch (Exception e) {
			rng.logCrash(e);
		}
		
		return instance;
	}

	@Override
	public ArrayList<?> getOptions(Class<?> target) {
		Method[] methods = target.getDeclaredMethods();
		
		ArrayList<Method> fuzzConstructors = new ArrayList<Method>();
		for (Method mth : methods) {
			if (mth.getName().equals("buzzfuzzConstructor")) {
				mth.setAccessible(true);
				fuzzConstructors.add(mth);
			}
		}
		return fuzzConstructors;
	}

	@Override
	public boolean validateChoice(Object choice, Class<?> target) {
		// Fair to try any of them
		return false;
	}
	
	

}
