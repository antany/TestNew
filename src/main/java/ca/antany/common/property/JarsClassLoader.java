package ca.antany.common.property;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JarsClassLoader {

	public static void main(String[] args) throws Exception{
		Manifest manifest = getManifest();
		if(manifest==null) {
			System.err.println("No manifest file found");
			System.exit(1);
		}
		
		URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
			
			@Override
			public URLStreamHandler createURLStreamHandler(String protocol) {
				System.out.println(protocol);
				return null;
			}
		});
		
		URL[] urls = null;
		String mainClass = manifest.getMainAttributes().getValue("app-main-class");
		String jars = manifest.getMainAttributes().getValue("inside-jars");
		if(jars!=null && !jars.trim().equals("")) {
			String[] jarArray = jars.split(" ");
			urls = new URL[jarArray.length];
			for(int i=0;i<jarArray.length;i++) {
				urls[i]  = new URL("antlib:"+jarArray[i]);
			}
		}
		
		ClassLoader classLoader = new URLClassLoader(urls);
		Thread.currentThread().setContextClassLoader(classLoader);
		Class c = Class.forName(mainClass, true, classLoader);
		Method main = c.getMethod("main", new Class[]{args.getClass()}); 
		main.invoke((Object)null, new Object[]{args});
		
	}
	
	private static Manifest getManifest() throws IOException{
		Manifest manifest = null;
		Enumeration<URL> classes = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
		if(classes.hasMoreElements()) {
			URL manifestURL = classes.nextElement();
			InputStream is = manifestURL.openStream();
			manifest = new Manifest(is);
		}
		
		return manifest;
	}
}
