package com.buzzfuzz.buzz.decisions;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.buzzfuzz.buzz.decisions.ConfigTree.Scope;
import com.buzzfuzz.buzz.decisions.Target;

public class Config {
	
	ConfigTree config;
	
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
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
    public int hashCode() {
        return config.hashCode();
    }
	
	private void mergeNewTree(ConfigTree tree) {
		config = tree;
//		mergeTrees(config, tree.getRoot());
	}
	
	// Merges two trees together, overriding the first tree with the second where applicable
	private void mergeTrees(ConfigTree t1, Scope scope) {
		if (t1 == null)
			t1 = new ConfigTree(scope);
			
		// iterate through target, constraint pairs and addPair continually
		if (scope.getTarget() != null && scope.getConstraint() != null)
			t1.addPair(scope.getTarget(), scope.getConstraint()); // should just have this method validate nulls
		for (Scope child : scope.getChildren()) {
			mergeTrees(t1, child);
		}
	}
	
	public void addPair(Target target, Constraint constraint) {
		config.addPair(target, constraint);
	}
	
	private void evaluateScopes(NodeList xmlScopes, Scope configScope) {
		
		for (int i=0; i < xmlScopes.getLength(); i++) {
			Node xmlScope = xmlScopes.item(i);
			if (xmlScope.getNodeName().equals("scope")) {
				Scope scope = new Scope();
				NodeList schild = xmlScope.getChildNodes();
				for (int j=0; j < schild.getLength(); j++) {
					Node sAtt = schild.item(j);
					
					if (sAtt.getNodeName().equals("target")) {
						scope.setTarget(parseTarget(sAtt));
					} else if (sAtt.getNodeName().equals("constraint")) {
						scope.setConstraint(parseConstraint(sAtt));
					} else if (sAtt.getNodeName().equals("scopes")) {
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
				System.out.println("Target value: " + child.getTextContent());
				target.setInstancePath(child.getTextContent());
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
				System.out.println("Constraint value: " + child.getTextContent());
				double value = Double.parseDouble(child.getTextContent());
				// might want to verify that it is within 0 and 1.0
				constraint.setNullProb(value);
			}
		}
		
		return constraint;
	}

	public Constraint findConstraintFor(Target target) {
		return config.findPairFor(target, config.getRoot()).x.y.y;
	}
	
	private static void setAttribute(Document doc, Element parent, String name, String value) {
		Element elem = doc.createElement(name);
	    elem.setTextContent(value);
	    parent.appendChild(elem);
	}
	
	// TODO: These should be moved out to a utility class
	private static void appendScopes(Document doc, Element elem, Scope parent) {
		
		// Add target if it exists
		if (parent.getTarget() != null) {
			Target target = parent.getTarget();
			Element xmlTarget = doc.createElement("target");
			
			if (target.getInstancePath() != null)
				setAttribute(doc, xmlTarget, "instancePath", target.getInstancePath());
			if (target.getMethodPath() != null)
				setAttribute(doc, xmlTarget, "methodPath", target.getMethodPath());
			
			elem.appendChild(xmlTarget);
		}
		
		// Add constraint if it exists
		if (parent.getConstraint() != null) {
			Constraint constraint = parent.getConstraint();
			Element xmlConstraint = doc.createElement("constraint");
			
			if (constraint.getNullProb() != null)
				setAttribute(doc, xmlConstraint, "nullProb", constraint.getNullProb().toString());
			if (constraint.getProb() != null)
				setAttribute(doc, xmlConstraint, "prob", constraint.getProb().toString());
			
			elem.appendChild(xmlConstraint);
		}
			
		// Recursively add children
		if (parent.getChildren().size() > 0) {
			Element xmlScopes = doc.createElement("scopes");
			
			for (Scope child : parent.getChildren()) {
				Element childScope = doc.createElement("scope");
				appendScopes(doc, childScope, child);
				xmlScopes.appendChild(childScope);
			}
			
			elem.appendChild(xmlScopes);
		}
	}
	
	public Document toXML() {
		Document doc = null;
		try {
		    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		    // root elements
		    doc = docBuilder.newDocument();
		    Element rootElement = doc.createElement("config");
		    doc.appendChild(rootElement);
		    
		    appendScopes(doc, rootElement, config.getRoot());

		  } catch (ParserConfigurationException pce) {
		    pce.printStackTrace();
		  }
		return doc;
	}

}
