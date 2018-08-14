package ca.antany.common.property;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLDecoder;
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
		
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		
		URL.setURLStreamHandlerFactory(new RsrcURLStreamHandlerFactory(cl));
		
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
		
		ClassLoader classLoader = new URLClassLoader(urls,getParentClassLoader());
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
	
	private static ClassLoader getParentClassLoader() throws InvocationTargetException, IllegalAccessException {
		// On Java8, it is ok to use a null parent class loader, but, starting with Java 9,
		// we need to provide one that has access to the restricted list of packages that
		// otherwise would produce a SecurityException when loaded
		try {
			// We use reflection here because the method ClassLoader.getPlatformClassLoader()
			// is only present starting from Java 9
			Method platformClassLoader = ClassLoader.class.getMethod("getPlatformClassLoader", (Class[])null); //$NON-NLS-1$
			return (ClassLoader) platformClassLoader.invoke(null, (Object[]) null);
		} catch (NoSuchMethodException e) {
			// This is a safe value to be used on Java 8 and previous versions
			return null;
		}
	}
}



class JarsClassLoaderURLStreamFactory implements URLStreamHandlerFactory{

	@Override
	public URLStreamHandler createURLStreamHandler(String protocol) {
		// TODO Auto-generated method stub
		return null;
	}
	
}

class RsrcURLStreamHandler extends java.net.URLStreamHandler {

	private ClassLoader classLoader;
	
	public RsrcURLStreamHandler(ClassLoader classLoader) {
    	this.classLoader = classLoader;
	}

	protected java.net.URLConnection openConnection(URL u) throws IOException {
    	return new RsrcURLConnection(u, classLoader);
    }

    protected void parseURL(URL url, String spec, int start, int limit) {
    	String file;
    	if (spec.startsWith(JIJConstants.INTERNAL_URL_PROTOCOL_WITH_COLON))  
    		file = spec.substring(7);
    	else if (url.getFile().equals(JIJConstants.CURRENT_DIR))
    		file = spec;
    	else if (url.getFile().endsWith(JIJConstants.PATH_SEPARATOR)) 
    		file = url.getFile() + spec;
		else if (JIJConstants.RUNTIME.equals(spec))
    		file = url.getFile();
    	else 
    		file = spec;
    	setURL(url, JIJConstants.INTERNAL_URL_PROTOCOL, "", -1, null, null, file, null, null);	 //$NON-NLS-1$ 
    }

}

class RsrcURLStreamHandlerFactory implements URLStreamHandlerFactory {

	private ClassLoader classLoader;
	private URLStreamHandlerFactory chainFac;
	
	public RsrcURLStreamHandlerFactory(ClassLoader cl) {
		this.classLoader = cl;
	}

	public URLStreamHandler createURLStreamHandler(String protocol) {
		if (JIJConstants.INTERNAL_URL_PROTOCOL.equals(protocol)) 
			return new RsrcURLStreamHandler(classLoader);
		if (chainFac != null)
			return chainFac.createURLStreamHandler(protocol);
		return null;
	}
	
	/**
	 * Allow one other URLStreamHandler to be added.
	 * URL.setURLStreamHandlerFactory does not allow
	 * multiple factories to be added.
	 * The chained factory is called for all other protocols,
	 * except "rsrc". Use null to clear previously set Handler. 
	 * @param fac another factory to be chained with ours.
	 */
	public void setURLStreamHandlerFactory(URLStreamHandlerFactory fac) {
		chainFac = fac;
	}
	
}

class RsrcURLConnection extends URLConnection {

	private ClassLoader classLoader;

	public RsrcURLConnection(URL url, ClassLoader classLoader) {
		super(url);
		this.classLoader= classLoader;
	}

	public void connect() throws IOException {
	}

	public InputStream getInputStream() throws IOException {
		String file= URLDecoder.decode(url.getFile(), JIJConstants.UTF8_ENCODING);
		InputStream result= classLoader.getResourceAsStream(file);
		if (result == null) {
			throw new MalformedURLException("Could not open InputStream for URL '" + url + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return result;
	}


}

class JIJConstants {
	
	static final String REDIRECTED_CLASS_PATH_MANIFEST_NAME  = "Rsrc-Class-Path";  //$NON-NLS-1$
	static final String REDIRECTED_MAIN_CLASS_MANIFEST_NAME  = "Rsrc-Main-Class";  //$NON-NLS-1$
	static final String DEFAULT_REDIRECTED_CLASSPATH         = "";  //$NON-NLS-1$
	static final String MAIN_METHOD_NAME                     = "main";  //$NON-NLS-1$
	static final String JAR_INTERNAL_URL_PROTOCOL_WITH_COLON = "jar:antlib:";  //$NON-NLS-1$
	static final String JAR_INTERNAL_SEPARATOR               = "!/";  //$NON-NLS-1$
	static final String INTERNAL_URL_PROTOCOL_WITH_COLON     = "antlib:";  //$NON-NLS-1$
	static final String INTERNAL_URL_PROTOCOL                = "antlib";  //$NON-NLS-1$
	static final String PATH_SEPARATOR                       = "/";  //$NON-NLS-1$
	static final String CURRENT_DIR                          = "./";  //$NON-NLS-1$
	static final String UTF8_ENCODING                        = "UTF-8";  //$NON-NLS-1$
	static final String RUNTIME                              = "#runtime";  //$NON-NLS-1$
}
