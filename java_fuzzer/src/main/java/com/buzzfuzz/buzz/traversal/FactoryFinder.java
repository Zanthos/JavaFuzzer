package com.buzzfuzz.buzz.traversal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.reflections.util.Utils;

import com.buzzfuzz.buzz.Engine;
import com.google.common.collect.Multimap;

public class FactoryFinder extends InstanceFinder {


	public FactoryFinder(InstanceDispatcher dispatcher) {
		super(dispatcher);
		logName = "Factory";
	}

	@Override
	public Object attemptPath(Object choice) {
		Method candidate = (Method)choice;
		log("Attempting factory method " + candidate.getName() + " from " + candidate.getDeclaringClass().getTypeName());
		
		Object instance = null;
		
		if (!Modifier.isStatic(candidate.getModifiers())) {
			instance = new InstanceDispatcher(this).tryGetInstance(candidate.getDeclaringClass());
			if (instance == null) {
				return null;
			}
		}
		
		Object[] args = new InstanceDispatcher(this).randomArgs(candidate.getParameterTypes(), candidate.getGenericParameterTypes());
		if (args == null) {
			return null;
		}
		
		Object outcome = null;
		try {
			outcome = candidate.invoke(instance, args);
		} catch (Exception e) {
			Engine.log(e, rng.getSeed());
		}
		
		return outcome;
		
	}

	@Override
	public ArrayList<?> getOptions(Class<?> target) {
		Multimap<String, String> store = Engine.reflections.getStore().get("CarefulMethodParameterScanner");
		
		Set<String> result = new HashSet<String>();
        for (String key : Utils.names(target)) {
            result.addAll(store.get(key));
        }
        
        ArrayList<Method> candidates = new ArrayList<Method>();
        candidates.addAll(Utils.getMethodsFromDescriptors( result, Engine.reflections.getConfiguration().getClassLoaders()));
		
        return candidates;
	}

	@Override
	public boolean validateChoice(Object choice, Class<?> target) {
		// Factory methods could be within the current class, which would be in the history
		Class<?> declaringClass = ((Method)choice).getDeclaringClass();
		return isClassinHistory(declaringClass) || declaringClass.equals(target);
	}

}
