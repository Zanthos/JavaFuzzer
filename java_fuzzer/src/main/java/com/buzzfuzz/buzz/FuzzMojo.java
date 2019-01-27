package com.buzzfuzz.buzz;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.buzzfuzz.buzztools.Fuzz;

/**
 * 
 * @author Rockett
 */
@Mojo( name = "test", defaultPhase = LifecyclePhase.TEST, requiresDependencyResolution = ResolutionScope.COMPILE )
public class FuzzMojo  extends AbstractMojo
{
	
	@Parameter( defaultValue = "${project}", readonly = true, required = true )
	private MavenProject mavenProject;
	
	@Parameter(defaultValue = "${project.build.directory}")
    private String projectBuildDir;
	
	@SuppressWarnings("unchecked")
	public void execute() throws MojoExecutionException
    {
		// Create a list of all relevant urls for loading .class files
		List<String> srcRoots = null;
		try {
			srcRoots = mavenProject.getCompileClasspathElements();
		} catch (DependencyResolutionRequiredException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		URL[] urls = new URL[srcRoots.size()];
		for (int i=0; i < srcRoots.size(); i++) {
			try {
				urls[i] = (new File(srcRoots.get(i))).toURI().toURL();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		URLClassLoader child = new URLClassLoader(
				urls, 
				this.getClass().getClassLoader());
		
		// Create Reflections object based on class loader
		Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(
				ClasspathHelper
				.forClassLoader(child))
				.addClassLoader(child)
				.addScanners(
						new MethodAnnotationsScanner(),
						new SubTypesScanner(),
						new CarefulMethodParameterScanner()));
		
//		for (String key : reflections.getStore().get("CarefulMethodParameterScanner").values()) {
//			System.out.println(key);
//		}
		
		Set<Method> methods = reflections.getMethodsAnnotatedWith(Fuzz.class);
		for (Method meth : methods) {
			System.out.println("Fuzzing method " + meth.getName());
		}
		
		Engine.run(methods, reflections);
    }
}