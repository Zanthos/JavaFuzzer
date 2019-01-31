package com.buzzfuzz.buzz.traversal;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.buzzfuzz.buzz.RNG;

public class InstanceDispatcher {
	
	private Set<ClassPkg> history;
	private RNG rng;
	
	public InstanceDispatcher(RNG rng, Set<ClassPkg> chain) {
		this.rng = rng;
		history = chain == null ? new HashSet<ClassPkg>() : new HashSet<ClassPkg>(chain);
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
	
	public Set<ClassPkg> getHistory() {
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
	
	public Object getInstance(ClassPkg target) {
		
		if (history.contains(target))
			return null;
		
		history.add(target);
		
		Object instance = checkPrimatives(target.getClazz());
		
		if (instance == null) {
			instance = checkCommon(target);
		}
		
		if (instance == null) {
			// Eventually will need full ClassPkg
			instance = checkClasses(target.getClazz());
		}
		return instance;
	}
	
	public Object getInstance(Class<?> target) {
		return getInstance(new ClassPkg(target, null));
	}
	
	public Object checkClasses(Class<?> target) {
		
		Object inst = new FuzzConstructorFinder(this).findInstance(target);
		if (inst == null) {
			inst = new ConstructorFinder(this).findInstance(target);
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
		} else {
			return null;
		}
	}
	
	private Object checkCommon(ClassPkg target) {
		
		if (target.getClazz().isArray()) {
			Class<?> type = target.getClazz().getComponentType();
			return randomArray(type);
		} if (target.getClazz().equals(List.class) ) {
			Class<?> type = (Class<?>)target.getGenerics()[0];
			return Arrays.asList((Object[]) Array.newInstance(type, 0).getClass().cast(randomArray(type)));
		}
		return null;
	}
	
	private Object randomArray(ClassPkg type) {
		int length = rng.fromRange(0, 10);
		Object array = Array.newInstance(type.getClazz(), length);
		for (int i = 0; i < length; i++) {
			Object instance = new InstanceDispatcher(this).getInstance(type);
			if (instance == null) {
				return null;
			}
			Array.set(array, i, instance);
		}
		return Array.newInstance(type.getClazz(), 0).getClass().cast(array);
	}
	
	private Object randomArray(Class<?> type) {
		return randomArray(new ClassPkg(type, null));
	}
	
	public ClassPkg[] packageClasses(Class<?>[] args, Type[] genArgs) {
		ClassPkg[] pkgs = new ClassPkg[args.length];
		int genIndex = 0;
		for (int i=0; i < args.length; i++) {
			int paramCount = args[i].getTypeParameters().length;
			Type[] generics = null;
			if (paramCount != 0) {
				generics = Arrays.copyOfRange(genArgs, genIndex, paramCount);
			
				// Sort out wild cards to a single type.
				for (int j=0; j < generics.length; j++) {
					if (generics[j] instanceof ParameterizedType) {
						ParameterizedType pt = (ParameterizedType)generics[j];
						for (Type type : pt.getActualTypeArguments()) {
							WildcardType wc = (WildcardType)type;
							generics[j] = wc.getUpperBounds()[0];
						}
					}
				}
			}
			
			pkgs[i] = new ClassPkg(args[i], generics);
			genIndex += paramCount;
		}
		return pkgs;
	}
	
	public Object[] randomArgs(Class<?>[] args, Type[] genArgs) {
		
		ClassPkg[] pkgs = packageClasses(args,  genArgs);
		
		Object[] instances = new Object[args.length];
		for (int i = 0; i < pkgs.length; i++) {
			instances[i] = new InstanceDispatcher(this).getInstance(pkgs[i]);
			// If any of the arguments return null, this path isn't valid
			if (instances[i] == null)
				return null;
		}
		return instances;
	}
	
	public Object[] randomArgs(Method method) {
		return randomArgs(method.getParameterTypes(), method.getGenericParameterTypes());
	}
	
	public Object[] randomArgs(Constructor<?> method) {
		return randomArgs(method.getParameterTypes(), method.getGenericParameterTypes());
	}

}
