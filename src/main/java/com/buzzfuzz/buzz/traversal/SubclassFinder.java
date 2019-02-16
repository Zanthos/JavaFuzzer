package com.buzzfuzz.buzz.traversal;

import java.util.ArrayList;

import com.buzzfuzz.buzz.Engine;

public class SubclassFinder extends InstanceFinder {


	public SubclassFinder(InstanceDispatcher dispatcher) {
		super(dispatcher);
		logName = "Subclass";
	}

	@Override
	public Object attemptPath(Object choice) {
		return new InstanceDispatcher(this).tryGetInstance((Class<?>)choice);
	}

	@Override
	public ArrayList<?> getOptions(Class<?> target) {
		ArrayList<Class<?>> typesList = new ArrayList<Class<?>>();
		typesList.addAll(Engine.reflections.getSubTypesOf(target));
		return typesList;
	}

	@Override
	public boolean validateChoice(Object choice, Class<?> target) {
		// Any subType would be fine
		return isClassinHistory((Class<?>)choice);
	}

}
