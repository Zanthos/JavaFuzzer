package rng_fuzzing.java_fuzzer;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * 
 * @author Rockett
 */
@Mojo( name = "integration-test", defaultPhase = LifecyclePhase.INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.RUNTIME )
public class FuzzMojo  extends AbstractMojo
{
	
	@Parameter( defaultValue = "${project}", readonly = true, required = true )
	private MavenProject mavenProject;
	
	public void execute() throws MojoExecutionException
    {
		System.out.println("getting jar");
		File jar = mavenProject.getArtifact().getFile();
		System.out.println("Got the jar");
		
		Set<Method> methods = null;
		
		try {
			URLClassLoader child = new URLClassLoader(
			        new URL[] {jar.toURI().toURL()},
			        this.getClass().getClassLoader()
			);
			Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(
					ClasspathHelper
					.forClassLoader(child))
					.addClassLoader(child)
					.addScanners(new MethodAnnotationsScanner()));
			methods = reflections.getMethodsAnnotatedWith(Fuzz.class);
			for (Method meth : methods) {
				System.out.println(meth.getName());
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(System.getProperty("java.class.path"));
		
		Engine.run(methods);
    }
}