package com.buzzfuzz.buzz.decisions;

public class Constraint {
	private Double nullProb;
	private Double prob;
	
	public void setNullProb(double prob) {
		this.nullProb = prob;
	}
	
	public double getNullProb() {
		return this.nullProb;
	}
	
	public void setProb(double prob) {
		this.prob = prob;
	}
	
	public double getProb() {
		return this.prob;
	}
	
	public void override(Constraint constraint) {
		if (constraint.nullProb != null)
			this.nullProb = constraint.nullProb;
		if (constraint.prob != null)
			this.prob = constraint.prob;
			
	}
}
