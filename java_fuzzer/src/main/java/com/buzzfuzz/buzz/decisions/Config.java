package com.buzzfuzz.buzz.decisions;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.buzzfuzz.buzz.decisions.ConfigTree.Scope;

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
				
				this.config = fileConfig;
//				mergeNewTree(fileConfig);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(this.config);
		}
	}
	
	private void mergeNewTree(ConfigTree tree) {
		mergeScopes(this.config.getRoot(), tree.getRoot());
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
				NamedNodeMap satt = xmlScope.getAttributes();
				Node target;
				if ((target = satt.getNamedItem("Target")) != null) {
					scope.setTarget(parseTarget(target));
				}
				Node constraint;
				if ((constraint = satt.getNamedItem("Constraint")) != null) {
					scope.setConstraint(parseConstraint(constraint));
				}
				Node scopes;
				if ((scopes = satt.getNamedItem("Scopes")) != null) {
					evaluateScopes(scopes.getChildNodes(), scope);
				}
				configScope.addChild(scope);
			}
		}
	}
	
	private Target parseTarget(Node xmlTarget) {
		NamedNodeMap tAtts = xmlTarget.getAttributes();
		Target target = new Target();
		
		Node instancePath;
		if ((instancePath = tAtts.getNamedItem("instancePath")) != null) {
			target.setInstancePath(instancePath.getNodeValue());
		}
		
		return target;
	}
	
	private Constraint parseConstraint(Node xmlConstraint) {
		NamedNodeMap cAtts = xmlConstraint.getAttributes();
		Constraint constraint = new Constraint();
		
		Node nullProb;
		if ((nullProb = cAtts.getNamedItem("nullProb")) != null) {
			double value = Double.parseDouble(nullProb.getNodeValue());
			// might want to verify that it is within 0 and 1.0
			constraint.setNullProb(value);
		}
		
		return constraint;
	}

}
