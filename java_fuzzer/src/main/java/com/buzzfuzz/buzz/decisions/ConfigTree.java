package com.buzzfuzz.buzz.decisions;

import java.util.ArrayList;
import java.util.List;

public class ConfigTree {
    private Scope root;

    public ConfigTree() {
        root = new Scope();
        root.children = new ArrayList<Scope>();
    }
    
    public Scope getRoot() {
    		return this.root;
    }

    public static class Scope {
        private Target target;
        private Constraint constraint;
        private List<Scope> children;
        
        public void addChild(Scope scope) {
        		children.add(scope);
        }
        
        public void setTarget(Target t) {
        		this.target = t;
        }
        
        public Target getTarget() {
        		return this.target;
        }
        
        public void setConstraint(Constraint c) {
        		this.constraint = c;
        }
        
        public Constraint getConstraint() {
        		return this.constraint;
        }
        
        public List<Scope> getChildren() {
        		return this.children;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof Scope) {
            		return this.target.equals(((Scope) o).target);
            } 
            
            return false;
        }
    }
}