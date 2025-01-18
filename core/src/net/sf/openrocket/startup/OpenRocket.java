package net.sf.openrocket.startup;

import net.sf.openrocket.startup.jij.*;
import net.sf.openrocket.utils.educoder.EduCoderService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.net.URL;

/**
 * First step in the OpenRocket startup sequence, responsible for
 * classpath setup.
 * 
 * The startup class sequence is the following:
 *   1. Startup
 *   2. SwingStartup
 * 
 * This class changes the current classpath to contain the jar-in-jar
 * library dependencies and plugins in the current classpath, and
 * then launches the next step of the startup sequence.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class OpenRocket {
	
	private static final String STARTUP_CLASS = "net.sf.openrocket.startup.SwingStartup";

	private static final Retrofit retrofit = new Retrofit.Builder()
			.baseUrl("http://127.0.0.1:8080/")
			.addConverterFactory(GsonConverterFactory.create())
			.build();
	public static final EduCoderService eduCoderService;

	static {
		eduCoderService = retrofit.create(EduCoderService.class);
	}
	
	public static void main(String[] args) {
		// This property works around some fundamental bugs in TimSort in the java library which has had known issues
		// since it was introduced in JDK 1.7.  In OpenRocket it manifests when you sort the motors in the motor chooser dialog
		// by designation.
		System.setProperty("java.util.Arrays.useLegacyMergeSort","true");
		addClasspathUrlHandler();
		JarInJarStarter.runMain(STARTUP_CLASS, args, new CurrentClasspathProvider(),
				new ManifestClasspathProvider(), new PluginClasspathProvider());
	}
	
	private static void addClasspathUrlHandler() {
		ConfigurableStreamHandlerFactory factory = new ConfigurableStreamHandlerFactory();
		factory.addHandler("classpath", new ClasspathUrlStreamHandler());
		URL.setURLStreamHandlerFactory(factory);
	}
	
}