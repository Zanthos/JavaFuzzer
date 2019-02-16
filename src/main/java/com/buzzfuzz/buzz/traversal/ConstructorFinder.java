package com.buzzfuzz.buzz.traversal;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import com.buzzfuzz.buzz.Engine;

import edu.emory.mathcs.backport.java.util.Arrays;

public class ConstructorFinder extends InstanceFinder {

	public ConstructorFinder(InstanceDispatcher dispatcher) {
		super(dispatcher);
		logName = "Constructor";
	}

	@Override
	public Object attemptPath(Object choice) {
		Constructor<?> cntr = (Constructor<?>)choice;
		Object[] args = new InstanceDispatcher(this).randomArgs(cntr.getParameterTypes(), cntr.getGenericParameterTypes());
		if (args == null)
			return null;
		
		Object instance = null;
		try {
			instance = cntr.newInstance(args);
		} catch (Exception e) {
			String path = Engine.log(e, rng.getSeed());
			rng.printConfig(path);
			// Eventually use this exception to drive config
		}
		
		return instance;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<?> getOptions(Class<?> target) {
		return new ArrayList<Constructor<?>>(Arrays.asList(target.getConstructors()));
	}

	@Override
	public boolean validateChoice(Object choice, Class<?> target) {
		// Any constructor will be fine
		return false;
	}

}
