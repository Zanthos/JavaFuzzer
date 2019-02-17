package com.buzzfuzz.buzz;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Set;

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
import com.buzzfuzz.buzz.CarefulMethodParameterScanner;

/**
 * 
 * @author Rockett
 */
@Mojo( name = "test", defaultPhase = LifecyclePhase.TEST, requiresDependencyResolution = ResolutionScope.COMPILE )
public class FuzzMojo  extends AbstractMojo
{
	
	@Parameter( defaultValue = "${project}", readonly = true, required = true )
	private MavenProject mavenProject;
	
//	@Parameter(defaultValue = "${project.build.outputDirectory}")
//    private String projectBuildDir;
	
	@Parameter(defaultValue = "${project.build.directory}")
    private String projectOutputDir;
	
	@Parameter(defaultValue = "${project.build.testOutputDirectory}")
	private String projectTestClassDir;
	
	@Parameter(defaultValue = "${project.build.outputDirectory}")
	private String projectClassDir;
	
	public void execute() throws MojoExecutionException
    {
		// Create a list of all relevant urls for loading .class files
//		List<String> srcRoots = null;
//		try {
//			srcRoots = mavenProject.getCompileClasspathElements();
//		} catch (DependencyResolutionRequiredException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		URL[] urls = new URL[srcRoots.size() + 1];
		URL[] urls = null;
		try {
			urls = new URL[] {
				Paths.get(projectTestClassDir).toFile().toURI().toURL(),
				Paths.get(projectClassDir).toFile().toURI().toURL()
			};
		} catch (MalformedURLException e2){
			e2.printStackTrace();
		}
		
//		try {
//			urls[urls.length-1] = Paths.get(projectTestClassDir).toFile().toURI().toURL();
//		} catch (MalformedURLException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		for (int i=0; i < srcRoots.size(); i++) {
//			try {
//				urls[i] = (new File(srcRoots.get(i))).toURI().toURL();
//			} catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
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
//		for (Method meth : methods) {
//			System.out.println("Fuzzing method " + meth.getName());
//		}
		
		Engine.outputDir = projectOutputDir;
		
		Engine.run(methods, reflections);
    }
}