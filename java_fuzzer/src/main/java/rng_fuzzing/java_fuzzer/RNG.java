package rng_fuzzing.java_fuzzer;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Random;

public class RNG {
	
	private Random rand;

	public RNG() {
		this(System.currentTimeMillis());
	}

	public RNG(long seed) {
		rand = new Random(seed);
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
}
