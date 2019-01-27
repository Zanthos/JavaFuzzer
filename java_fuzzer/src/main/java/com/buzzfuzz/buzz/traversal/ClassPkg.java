package com.buzzfuzz.buzz.traversal;

import java.lang.reflect.Type;
import java.util.Arrays;

public class ClassPkg {
	
	private Class<?> clazz;
	
	private Type[] generics;
	
	public ClassPkg(Class<?> clazz, Type[] generics) {
		this.clazz = clazz;
		this.generics = generics;
	}
	
	public Class<?> getClazz() {
		return clazz;
	}
	
	public Type[] getGenerics() {
		return generics;
	}
	
	@Override
    public boolean equals(Object o) {
        if (o instanceof Class<?>) {
        		return this.clazz.equals((Class<?>)o);
        } else if (o instanceof ClassPkg) {
        		ClassPkg opkg = (ClassPkg)o;
        		boolean compare = this.toString().equals(o.toString());
        		if (compare) {
        			System.out.println(this.toString() + " = " + o.toString());
        		} else System.out.println(this.toString() + "!= " + o.toString());
        		return compare;
        		
//        		if (this.generics == null || this.generics.length == 0) {
//        			return opkg.getGenerics() == null || opkg.getGenerics().length == 0;
//        		} else if (opkg.getGenerics() != null && this.generics.length == opkg.getGenerics().length ) {
//	        		// If I don't have to, shouldn't compare string names
//	        		for (int i=0; i<this.generics.length; i++) {
//	        			 if (!this.generics[i].getTypeName().equals(opkg.getGenerics()[i].getTypeName())) {
//	        				 System.out.println(this.toString() + " != " + Not equal");
//	        				 return false;
//	        			 }
//	        		}
//	        		System.out.println("Equal");
//	        		return true;
//        		}
        } 
        
        return false;
    }
	
	@Override
    public int hashCode() {
        return this.toString().hashCode();
    }
	
	@Override
	public String toString() {
		return "Class: " + clazz.getSimpleName() + ", Generics: " + Arrays.toString(generics);
	}

}
