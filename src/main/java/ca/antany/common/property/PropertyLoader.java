package ca.antany.common.property;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyLoader extends Properties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Logger logger = Logger.getLogger(PropertyLoader.class.getName());

	private static PropertyLoader pl = null;

	private PropertyLoader() {
		super();
	}

	public void addPropertyFile(String resourcePath, PropertyType propertyType) {
		Properties props = new Properties();
		switch (propertyType) {
		case CLASSPATH:
			genenratePropertyFromClassPath(resourcePath);
			break;
		case FILE:
			genenratePropertyFromLocalFile(resourcePath);
			break;
		default:
			genenratePropertyFromClassPath(resourcePath);
			genenratePropertyFromLocalFile(resourcePath);
		}
	}

	public static PropertyLoader getInstance() {
		if (pl == null) {
			pl = new PropertyLoader();
		}
		return pl;
	}

	private void genenratePropertyFromClassPath(String resourcePath) {
		logger.info("Loading property from classpath " + resourcePath);
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try (InputStream resourceStream = loader.getResourceAsStream(resourcePath)) {
			this.load(resourceStream);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error Processing classpath Property", e);
		}
	}

	private void genenratePropertyFromLocalFile(String resourcePath) {
		logger.info("Loading property from file " + resourcePath);
		try {
			this.load(new FileInputStream(resourcePath));
		}catch (FileNotFoundException foe) {
			logger.log(Level.INFO, "Local file not found {0}", resourcePath);
		}catch (Exception e) {
			logger.log(Level.SEVERE, "Error Processing Local File Property", e);
		}
	}
}
