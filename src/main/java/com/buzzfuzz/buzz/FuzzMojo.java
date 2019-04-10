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
		URL[] urls = null;
		try {
			urls = new URL[] {
				Paths.get(projectTestClassDir).toFile().toURI().toURL(),
				Paths.get(projectClassDir).toFile().toURI().toURL()
			};
		} catch (MalformedURLException e2){
            e2.printStackTrace();
            return;
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
						new com.buzzfuzz.rog.CarefulMethodParameterScanner()));

        Set<Method> methods = reflections.getMethodsAnnotatedWith(Fuzz.class);

//		for (Method meth : methods) {
//			System.out.println("Fuzzing method " + meth.getName());
//		}

        Engine.outputDir = projectOutputDir;
        Engine.rog = new FROG(reflections);

		Engine.fuzz(methods);
    }
}