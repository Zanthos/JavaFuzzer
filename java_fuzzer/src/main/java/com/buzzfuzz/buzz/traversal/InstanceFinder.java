package com.buzzfuzz.buzz.traversal;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Set;

import com.buzzfuzz.buzz.RNG;
import com.buzzfuzz.buzz.decisions.Context;

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
			
			Context context = getContext(choice, target);
			
			if (!rng.should(context)) {
				log("SETTING TO NULL BECAUSE OF NULLPROB");
				return null;
			}
			
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
		return history.contains(new ClassPkg(target, null));
	}
	
	public abstract Object attemptPath(Object choice);
	
	public abstract ArrayList<?> getOptions(Class<?> target);
	
	// Returns true is this choice is invalid
	public abstract boolean validateChoice(Object choice, Class<?> target);
	
	private Context getContext(Object choice, Class<?> target) {
		Context context = new Context();
		String instancePath = "";
		for (ClassPkg instance : history) {
			instancePath += instance.getClazz().getSimpleName();
			if (instance.getGenerics() != null) {
				instancePath += '<';
				for (Type generic : instance.getGenerics()) {
					instancePath += generic.getTypeName() + ',';
				}
				instancePath = instancePath.substring(0, instancePath.length()-1);
				instancePath += '>';
			}
			instancePath += '.';
		}
		instancePath = instancePath.substring(0, instancePath.length()-1);
		context.setInstancePath(instancePath);
		
		log(instancePath);
		
		return context;
	}

}
