package com.buzzfuzz.buzz.traversal;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.buzzfuzz.buzz.RNG;
import com.buzzfuzz.buzz.decisions.Constraint;
import com.buzzfuzz.buzz.decisions.Target;

public class InstanceDispatcher {
	
	private Set<ClassPkg> history;
	private Constraint constraint;
	private RNG rng;
	
	public InstanceDispatcher(RNG rng, Set<ClassPkg> chain) {
		this.rng = rng;
		history = chain == null ? new LinkedHashSet<ClassPkg>() : new LinkedHashSet<ClassPkg>(chain);
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
			msg = "    " + msg;
			indent--;
		}
		rng.log(msg + '\n');
	}
	
	public Object tryGetInstance(ClassPkg target) {
		if (history.contains(target))
			return null;
		
		// Maybe in load method?
		history.add(target);
		loadConstraint(getContext(target.getClazz()));
		
		if (!target.getClazz().isPrimitive() && rng.should(constraint.getNullProb())) {// Should eventually work with full ClassPkg
			log("Returning null instead of instance");
			return null;
		}
		
		return getInstance(target);
	}
	
	public Object tryGetInstance(Class<?> target) {
		return tryGetInstance(new ClassPkg(target, null));
	}
	
	public Object getInstance(ClassPkg target) {
		
		// Might need to move history check here
		
		// If this method was called directly
		if(constraint == null) {
			history.add(target);
			loadConstraint(getContext(target.getClazz()));
		}
		
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
	
	private Target getContext(Class<?> target) {
		Target context = new Target();
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
	
	private void loadConstraint(Target target) {
		Constraint constraint = rng.getConstraint(target);
		
		if (constraint == null) {
//			System.out.println("MAKING NEW CONSTRAINT");
			constraint = rng.makeConstraint(target);
		} 
//		else System.out.println("USING EXISTING CONSTRAINT" + constraint.toString());
		this.constraint = constraint;
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
			System.out.println("GETTING LONG PRIMITIVE");
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
	
	@SuppressWarnings("unchecked")
	private Object checkCommon(ClassPkg target) {
		
		if (target.getClazz().isArray()) {
			Class<?> type = target.getClazz().getComponentType();
			return randomArray(type);
		} else if (target.getClazz().equals(List.class) ) {
			if (target.getGenerics().length == 0)
				return null;
			Class<?> type = (Class<?>)target.getGenerics()[0];
			log("Creating List of type: " + type.getSimpleName());
			Object array = randomArray(type);
			if (array != null)
				return Arrays.asList(Array.newInstance(type, 0).getClass().cast(array));
			else return null;
		} else if (target.getClazz().equals(BigInteger.class)) {
			return new BigInteger(rng.fromRange(2, 32), rng.getRNG());
		} else if (target.getClazz().equals(Number.class)) {
			return rng.getDouble();
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
			instances[i] = new InstanceDispatcher(this).tryGetInstance(pkgs[i]);
			// If any of the arguments return BadPath, return BadPath
//			if (instances[i].getClass().)
//				return null;
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
