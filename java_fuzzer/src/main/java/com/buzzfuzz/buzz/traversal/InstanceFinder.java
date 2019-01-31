package com.buzzfuzz.buzz.traversal;

import java.util.ArrayList;
import java.util.Set;

import com.buzzfuzz.buzz.RNG;

public abstract class InstanceFinder {
	
	ArrayList<?> options;
	Set<ClassPkg> history;
	RNG rng;
	String logName = null;
	
	public InstanceFinder(Set<ClassPkg> chain, RNG rng) {
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
		
		log("Finding " + logName + " for type " + target.getSimpleName());
		
		// get group of options
		options = getOptions(target);
		
		// loop through options
		while (true) {
			if (options.size() == 0) {
				log("Couldn't find " + logName + " for type " + target.getSimpleName());
				return null;
			}
			
			int index = rng.fromRange(0, options.size()-1);
			
			Object choice = options.get(index);
			
			if (validateChoice(choice, target)) {
				log("Already tried " + target.getSimpleName() + " before.");
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

	public boolean isClassinHistory(Class<?> target) {
//		for (ClassPkg pkg : history) {
//			if (pkg.getClazz().equals(target)) {
//				System.out.println("DONE BEFORE: " + target.getSimpleName());
//				return true;
//			}
//		}
//		System.out.println("NOT IN HISTORY: " + target.getSimpleName());
		return history.contains(new ClassPkg(target, null));
	}
	
	public abstract Object attemptPath(Object choice);
	
	public abstract ArrayList<?> getOptions(Class<?> target);
	
	// Returns true is this choice is invalid
	public abstract boolean validateChoice(Object choice, Class<?> target);

}
