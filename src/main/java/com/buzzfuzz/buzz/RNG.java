package com.buzzfuzz.buzz;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.buzzfuzz.buzz.decisions.Config;
import com.buzzfuzz.buzz.decisions.Constraint;
import com.buzzfuzz.buzz.decisions.Target;
import com.buzzfuzz.buzztools.FuzzConstraint;
import com.buzzfuzz.buzztools.FuzzConstraints;

public class RNG {
	
	private Random rand;
	private Config config;
	private long seed;
	private Set<String> crashes;
	private int crashCount;

	public RNG() {
		this(System.currentTimeMillis());
	}

	public RNG(long seed) {
		this.seed = seed;
		rand = new Random(seed);
		crashes = new HashSet<String>();
		crashCount++;
//		config = new Config();
	}
	
	public void log(String msg) {
		config.log(msg);
	}
	
	public void setConfig(Config config) {
		this.config = config;
	}
	
	public long getSeed() {
		return this.seed;
	}
	
	public Set<String> getCrashes() {
		return this.crashes;
	}
	
	public int getCrashCount() {
		return this.crashCount;
	}
	
	public Random getRNG() {
		return this.rand;
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
		long test = rand.nextLong();
		System.out.println("RETURNING LONG VALUE: " + test);
		return test;
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
	
	public void mutateConfig() {
		// Should iterate through all constraints specified in config and have a small chance to mutate it
	}
	
	public static Config parseConfig(Method method) {
		Config config = new Config();
		FuzzConstraints constraintsAnnotation = method.getAnnotation(FuzzConstraints.class);
		if (constraintsAnnotation != null) {
			FuzzConstraint[] constraints = constraintsAnnotation.value();
			
			for( FuzzConstraint constraint : constraints ) {
				evaluateConstraint(config, constraint);
			}
		} else {
			// Maybe there is only one
			FuzzConstraint constraintAnnotation = method.getAnnotation(FuzzConstraint.class);
			if (constraintAnnotation != null) {
				evaluateConstraint(config, constraintAnnotation);
			}
		}
		return config;
	}
	
	private static void evaluateConstraint(Config config, FuzzConstraint constraint) {
		// Also need to add pairs given in annotations
		String configFile = constraint.configFile();
		if (configFile != null && !configFile.isEmpty())
			config.addConfigFile(configFile);
	}
	
	public Constraint getConstraint(Target target) {
		return config.findConstraintFor(target);
	}
	
	public boolean shouldMutate() {
		// Eventually, this should be a ratio based on how many constraints there are
		return should(0.2);
	}
	
	public boolean should(double prob) {
		return rand.nextDouble() < prob;
	}
	
	public Constraint makeConstraint(Target target) {
		// These should be set based on constraints / probabilities that create the best random constraint for exploratory fuzzing
		Constraint constraint = new Constraint();
		constraint.setNullProb(rand.nextDouble());
		constraint.setProb(rand.nextDouble());
		config.addPair(target, constraint);
		return constraint;
	}
	
	public void logCrash(Exception e) {
		Throwable t = e;
		while (t.getCause() != null && t.getCause().getStackTrace().length > 0) {
			t = t.getCause();
		}
		crashes.add(t.getClass().getSimpleName());
		crashCount++;
		String path = Engine.log(t, seed);
		printConfig(path);
	}
	
	// This should really be the Engine's job to make sure that everything is still thread-safe
	private void printConfig(String path) {
		File corpus = Paths.get(path, "corpus").toFile();
		if (!corpus.exists())
			corpus.mkdir();
		
		File output = Paths.get(corpus.toURI().getPath(), String.valueOf(config.hashCode())).toFile();
		if (output.exists())
			output.delete();
		
		output.mkdir();
		
		// Print the log of decisions that were made
		File logger = Paths.get(output.toURI().getPath(), "log.txt").toFile();
		BufferedWriter writer = null;
		try {
		    writer = new BufferedWriter(new FileWriter(logger));
		    writer.append(config.getLog());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		    if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		// Print configuration that was used
		File configuration = Paths.get(output.getPath(), "config.xml").toFile();
		
		Document doc = config.toXML();
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			
		     StreamResult result = new StreamResult(configuration);
		    
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	    
	}
}
