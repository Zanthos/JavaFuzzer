package com.buzzfuzz.buzz.decisions;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.buzzfuzz.buzz.decisions.ConfigTree.Scope;
import com.buzzfuzz.buzz.decisions.Target;

public class Config {
	
	ConfigTree config = new ConfigTree();
	
	public Config() {
		
	}
	
	public void addConfigFile(String path) {
		if (!path.isEmpty() && path != null) {
			try {
				DocumentBuilderFactory factory =	DocumentBuilderFactory.newInstance();
				DocumentBuilder builder;
				builder = factory.newDocumentBuilder();
				Document doc = builder.parse(path);
				
				ConfigTree fileConfig = new ConfigTree();
				
				Node xmlConfig = doc.getElementsByTagName("config").item(0);
				
				for (int i=0; i < xmlConfig.getChildNodes().getLength(); i++) {
					Node child = xmlConfig.getChildNodes().item(i);
					if (child.getNodeName().equals("scopes")) {
						evaluateScopes(child.getChildNodes(), fileConfig.getRoot());
					}
				}
				
				mergeNewTree(fileConfig);
//				mergeNewTree(fileConfig);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(this.config);
		}
	}
	
	private void mergeNewTree(ConfigTree tree) {
		config = tree;
//		mergeScopes(this.config.getRoot(), tree.getRoot());
	}
	
	private void mergeScopes(Scope initS, Scope newS) {
		List<Scope> initChildren = initS.getChildren();
		for (Scope child : newS.getChildren()) {
			if (initChildren.contains(child)) {
				mergeScopes(initChildren.get(initChildren.indexOf(child)), child);
			} else {
				initChildren.add(child);
			}
		}
	}
	
	private void evaluateScopes(NodeList xmlScopes, Scope configScope) {
		
		for (int i=0; i < xmlScopes.getLength(); i++) {
			Node xmlScope = xmlScopes.item(i);
			if (xmlScope.getNodeName().equals("scope")) {
				Scope scope = new Scope();
				NodeList schild = xmlScope.getChildNodes();
				for (int j=0; j < schild.getLength(); j++) {
					Node sAtt = schild.item(j);
					
					if (sAtt.getNodeName().equals("Target")) {
						scope.setTarget(parseTarget(sAtt));
					} else if (sAtt.getNodeName().equals("Constraint")) {
						scope.setConstraint(parseConstraint(sAtt));
					} else if (sAtt.getNodeName().equals("Scopes")) {
						evaluateScopes(sAtt.getChildNodes(), scope);
					}
				}
				configScope.addChild(scope);
			}
		}
	}
	
	private Target parseTarget(Node xmlTarget) {
		NodeList tAtts = xmlTarget.getChildNodes();
		Target target = new Target();
		
		for (int i=0; i < tAtts.getLength(); i++) {
			Node child = tAtts.item(i);
			if (child.getNodeName().equals("instancePath")) {
				target.setInstancePath(child.getNodeValue());
			}
			// More later
		}
		
		return target;
	}
	
	private Constraint parseConstraint(Node xmlConstraint) {
		NodeList cAtts = xmlConstraint.getChildNodes();
		Constraint constraint = new Constraint();
		
		for (int i=0; i < cAtts.getLength(); i++) {
			Node child = cAtts.item(i);
			if (child.getNodeName().equals("nullProb")) {
				double value = Double.parseDouble(child.getNodeValue());
				// might want to verify that it is within 0 and 1.0
				constraint.setNullProb(value);
			}
		}
		
		return constraint;
	}

	public Constraint findConstraintFor(Context context) {
		return findConstraintFor(context, config.getRoot()).x;
	}
	
	private Tuple<Constraint, Integer> findConstraintFor(Context context, Scope scope) {
		if (validateContext(scope.getTarget(), context)) {
			// Should also build up the constraints as we go down
			// This is getting complicated
			Constraint constraint = scope.getConstraint();
			if (constraint != null)
				System.out.println("VALIDATED CONSTRAINT: " + constraint.getNullProb());
			int maxDepth = 0;
			if (scope.getChildren() != null) {
				for (Scope child : scope.getChildren()) {
					Tuple<Constraint, Integer> result = findConstraintFor(context, child);
					if (result.y >= maxDepth) {
						if (constraint == null)
							constraint = result.x;
						else constraint.override(result.x);
						maxDepth = result.y;
					}
				}
			}
			return new Tuple<Constraint, Integer>(constraint, maxDepth);
		}
		return null;
	}
	
	private boolean validateContext(Target target, Context context) {
		if (target == null) {
			System.out.println("TARGET WAS NULL");
			return true;
		}
		if (context.getInstancePath() != null && target.getInstancePath() != null)
			return context.getInstancePath().contains(target.getInstancePath()); // Eventually use regex
		return false; // This should have a lot of things later
	}
	
	public class Tuple<X, Y> { 
		public final X x; 
		public final Y y; 
		public Tuple(X x, Y y) { 
			this.x = x; 
			this.y = y; 
		} 
	} 

}
