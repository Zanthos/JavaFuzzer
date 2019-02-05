package com.buzzfuzz.buzz;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Random;

import com.buzzfuzz.buzz.decisions.Config;
import com.buzzfuzz.buzz.decisions.Constraint;
import com.buzzfuzz.buzz.decisions.Context;
import com.buzzfuzz.buzztools.FuzzConstraint;
import com.buzzfuzz.buzztools.FuzzConstraints;

public class RNG {
	
	private Random rand;
	private Config config;
	private long seed;

	public RNG() {
		this(System.currentTimeMillis());
	}

	public RNG(long seed) {
		this.seed = seed;
		rand = new Random(seed);
		config = new Config();
	}
	
	public long getSeed() {
		return this.seed;
	}
	
	public int fromRange(int low, int high) {
		if (low > high) {
			int temp = low;
			low = high;
			high = temp;
		}
		return rand.nextInt(high-low+1) + low;
	}
	
	public double fromRange(double low, double high) {
		if (low > high) {
			double temp = low;
			low = high;
			high = temp;
		}
		return rand.nextDouble() * (high-low) + low;
	}
	
	public float fromRange(float low, float high) {
		if (low > high) {
			float temp = low;
			low = high;
			high = temp;
		}
		return rand.nextFloat() * (high-low) + low;
	}
	
	public long fromRange(long low, long high) {
		if (low > high) {
			long temp = low;
			low = high;
			high = temp;
		}
		return low + (long)(rand.nextDouble()*(high-low));
	}
	
	public short fromRange(short low, short high) {
		if (low > high) {
			short temp = low;
			low = high;
			high = temp;
		}
		return (short) (low + (rand.nextDouble()*(high-low)));
	}
	
	public int getInt() {
		return rand.nextInt();
	}
	
	public double getDouble() {
		byte[] bytes = new byte[8];
		rand.nextBytes(bytes);
		return ByteBuffer.wrap(bytes).getDouble();
	}
	
	public float getFloat() {
		byte[] bytes = new byte[4];
		rand.nextBytes(bytes);
		return ByteBuffer.wrap(bytes).getFloat();
	}
	
	public long getLong() {
		return rand.nextLong();
	}
	
	public short getShort() {
		return (short)rand.nextInt();
	}
	
	public char getChar() {
		return (char)rand.nextInt();
	}
	
	public boolean getBool() {
		return rand.nextBoolean();
	}
	
	public byte getByte() {
		byte[] bytes = new byte[1];
		rand.nextBytes(bytes);
		return bytes[0];
	}
	
	public String getString() {
		int length = rand.nextInt(30);
		return getString(length);
	}
	
	public String getString(int length) {
		byte[] bytes = new byte[length];
		rand.nextBytes(bytes);
		return new String(bytes, Charset.forName("UTF-8"));
	}
	
	public void parseConfig(Method method) {
		FuzzConstraints constraintsAnnotation = method.getAnnotation(FuzzConstraints.class);
		if (constraintsAnnotation != null) {
			FuzzConstraint[] constraints = constraintsAnnotation.value();
			
			for( FuzzConstraint constraint : constraints ) {
				evaluateConstraint(constraint);
			}
		} else {
			// Maybe there is only one
			FuzzConstraint constraintAnnotation = method.getAnnotation(FuzzConstraint.class);
			if (constraintAnnotation != null) {
				evaluateConstraint(constraintAnnotation);
			}
		}
	}
	
	private void evaluateConstraint(FuzzConstraint constraint) {
		String configFile = constraint.configFile();
		if (configFile != null && !configFile.isEmpty())
			this.config.addConfigFile(configFile);
	}
	
	public boolean should(Context context) {
		Constraint constraint = config.findConstraintFor(context);
		if (constraint == null) // We maybe be good to still choose constraints randomly to try them out.
			return true; // should have default constraint later
		double prob = constraint.getNullProb();
		double chance = fromRange(0.0, 1.0); // This can be done more efficiently
		if (chance > prob)
			return true;
		else return false;
	}
}
