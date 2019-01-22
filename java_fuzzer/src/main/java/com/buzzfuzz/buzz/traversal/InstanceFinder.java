package com.buzzfuzz.buzz.traversal;

import java.util.ArrayList;
import java.util.Set;

import com.buzzfuzz.buzz.RNG;

public abstract class InstanceFinder {
	
	ArrayList<?> options;
	Set<Class<?>> history;
	RNG rng;
	String logName = null;
	
	public InstanceFinder(Set<Class<?>> chain, RNG rng) {
		history = chain;
		this.rng = rng;
	}
	
	public InstanceFinder(InstanceDispatcher dispatcher) {
		history = dispatcher.getHistory();
		rng = dispatcher.getRNG();
	}
	
	protected void log(String msg) {
		int indent = history.size();
		while (indent > 0) {
			System.out.print("    ");
			indent--;
		}
		System.out.println(msg);
	}
	
	public Object findInstance(Class<?> target) {
		
		log("Finding " + logName + " for type " + target.getTypeName());
		
		// get group of options
		options = getOptions(target);
		
		// loop through options
		while (true) {
			if (options.size() == 0) {
				log("Couldn't find " + logName + " for type " + target.getTypeName());
				return null;
			}
			
			int index = rng.fromRange(0, options.size()-1);
			
			Object choice = options.get(index);
			
			if (validateChoice(choice, target)) {
				options.remove(choice);
				continue;
			}
		
		
			Object attempt = attemptPath(choice);
			if (attempt == null) {
				options.remove(choice);
			} else {
				return attempt;
			}
		}
		
		// verify if the option is good
	}
	
	public abstract Object attemptPath(Object choice);
	
	public abstract ArrayList<?> getOptions(Class<?> target);
	
	public abstract boolean validateChoice(Object choice, Class<?> target);

}
