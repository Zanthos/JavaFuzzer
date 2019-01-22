package com.buzzfuzz.buzz.traversal;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.buzzfuzz.buzz.RNG;

public class InstanceDispatcher {
	
	private Set<Class<?>> history;
	private RNG rng;
	
	public InstanceDispatcher(RNG rng, Set<Class<?>> chain) {
		this.rng = rng;
		history = chain == null ? new HashSet<Class<?>>() : new HashSet<Class<?>>(chain);
	}
	
	public InstanceDispatcher(RNG rng) {
		this(rng, null);
	}
	
	public InstanceDispatcher(InstanceDispatcher dispatcher) {
		this(dispatcher.rng, dispatcher.history);
	}
	
	public InstanceDispatcher(InstanceFinder finder) {
		this(finder.rng, finder.history);
	}
	
	public Set<Class<?>> getHistory() {
		return history;
	}
	
	public RNG getRNG() {
		return rng;
	}
	
	private void log(String msg) {
		int indent = history.size();
		while (indent > 0) {
			System.out.print("    ");
			indent--;
		}
		System.out.println(msg);
	}
	
	public Object getInstance(Class<?> target) {
		
		history.add(target);
		Object instance = checkPrimatives(target);
		
		if (instance == null) {
			instance = checkClasses(target);
		}
		return instance;
	}
	
	public Object checkClasses(Class<?> target) {
		
		Object inst = new ConstructorFinder(this).findInstance(target);
		if (inst == null) {
			inst = new LocalFactoryFinder(this).findInstance(target);
			if (inst == null) {
				inst = new FactoryFinder(this).findInstance(target);
				if (inst == null) {
					inst = new SubclassFinder(this).findInstance(target);
					if (inst == null)
						log("Could not find a way to get an instance of this class.");
				}
			}
		}
		return inst;
	}
	
	public Object checkPrimatives(Class<?> target) {
		if (target.equals(int.class)) {
			return rng.getInt();
		} else if (target.equals(long.class)) {
			return rng.getLong();
		} else if (target.equals(char.class)) {
			return rng.getChar();
		} else if (target.equals(float.class)) {
			return rng.getFloat();
		} else if (target.equals(double.class)) {
			return rng.getDouble();
		} else if (target.equals(boolean.class)) {
			return rng.getBool();
		} else if (target.equals(byte.class)) {
			return rng.getByte();
		} else if (target.equals(short.class)) {
			return rng.getShort();
		} else if (target.equals(String.class)) {
			return rng.getString();
		} else if (target.isEnum()) {
			Object[] values = target.getEnumConstants();
			int index = rng.fromRange(0, values.length - 1);
			return values[index];
		} else if (target.isArray()) {
			Class<?> type = target.getComponentType();
			return randomArray(type);
		} else if (target.equals(List.class) ) {
			Class<?> type = (Class<?>) ((ParameterizedType)target.getGenericSuperclass()).getActualTypeArguments()[0];
			if (type == null) {
				log("type is null");
			} else {
				log(type.getName());
			}
			Object instance = randomArray(type);
			if (instance == null)
				return null;
			return Arrays.asList((Object[]) Array.newInstance(type, 0).getClass().cast(randomArray(type)));
		} else {
			return null;
		}
	}
	
	private Object randomArray(Class<?> type) {
		int length = rng.fromRange(0, 10);
		Object array = Array.newInstance(type, length);
		for (int i = 0; i < length; i++) {
			Object instance = new InstanceDispatcher(this).getInstance(type);
			if (instance == null) {
				return null;
			}
			Array.set(array, i, instance);
		}
		return Array.newInstance(type, 0).getClass().cast(array);
	}
	
	public Object[] randomArgs(Class<?>[] args) {
		Object[] instances = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			instances[i] = new InstanceDispatcher(this).getInstance(args[i]);
			// If any of the arguments return null, this path isn't valid
			if (instances[i] == null)
				return null;
		}
		return instances;
	}

}
