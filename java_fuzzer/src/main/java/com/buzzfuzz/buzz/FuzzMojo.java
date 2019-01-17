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
	
	public void execute() throws MojoExecutionException
    {
//		File file = mavenProject.getArtifact().getFile();
		List<String> srcRoots = null;
		try {
			srcRoots = mavenProject.getCompileClasspathElements();
		} catch (DependencyResolutionRequiredException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		List<?> srcRoots = mavenProject.getCompileSourceRoots();
		URL[] urls = new URL[srcRoots.size()];
		for (int i=0; i < srcRoots.size(); i++) {
			try {
				urls[i] = (new File(srcRoots.get(i))).toURI().toURL();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		File file = new File(projectBuildDir);
		
//		if (jar == null)
//			throw new MojoExecutionException("No jar found. Make sure that a jar is built before running this goal directly.");
		
		Set<Method> methods = null;
		Reflections reflections = null;
		
		//			URLClassLoader child = new URLClassLoader(
		//			        new URL[] {jar.toURI().toURL()},
		//			        this.getClass().getClassLoader()
		//			);
		//			System.out.println(file.toURI().toURL());
		for (URL url : urls) {
			System.out.println(url.toString());
		}
		URLClassLoader child = new URLClassLoader(
				urls, 
				this.getClass().getClassLoader());
		reflections = new Reflections(new ConfigurationBuilder().setUrls(
				ClasspathHelper
				.forClassLoader(child))
				.addClassLoader(child)
				.addScanners(
						new MethodAnnotationsScanner(),
						new SubTypesScanner(false)));
		methods = reflections.getMethodsAnnotatedWith(Fuzz.class);
		for (Method meth : methods) {
			System.out.println(meth.getName());
		}
		
		Engine.run(methods, reflections);
    }
}