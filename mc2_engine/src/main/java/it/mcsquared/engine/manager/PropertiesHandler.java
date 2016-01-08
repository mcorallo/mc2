package it.mcsquared.engine.manager;

import java.io.FileInputStream;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesHandler {

	private static Logger logger = LoggerFactory.getLogger(PropertiesHandler.class);

	private PropertiesConfiguration conf;
	private static String env;

	/**
	 * Loads a properties file from the given stream and wraps the object with utility methods.
	 * 
	 */
	public PropertiesHandler(FileInputStream propertiesStream) throws Exception {
		conf = new PropertiesConfiguration();
		conf.load(propertiesStream);
	}

	public String getProperty(String key) {
		String value = null;
		String k = key;
		if (env != null) {
			k = env + "." + key;
			value = conf.getString(k);
			value = checkUnresolved(key, value);
		}

		if (value == null) {
			value = conf.getString(key);
			value = checkUnresolved(key, value);
		}
		logger.debug("Richiesta property {} ({}): {}", key, k, value);
		return value;
	}

	public String[] getProperties(String key) {
		String[] value = null;
		String k = key;
		if (env != null) {
			k = env + "." + key;
			if (conf.containsKey(k)) {
				value = conf.getStringArray(k);
			}
		}

		if (value == null) {
			value = conf.getStringArray(key);
		}
		logger.debug("Richiesta property {} ({}): {}", key, k, value);
		return value;
	}

	private String checkUnresolved(String property, String value) {
		if (value != null && value.contains("${")) {
			String placeHolder = value.substring(value.indexOf("${"), value.indexOf("}") + 1);
			String placeHolderName = placeHolder.substring(2, placeHolder.length() - 1);
			String newValue = StringUtils.replace(value, placeHolder, System.getProperty(placeHolderName));

			conf.setProperty(property, newValue);
			System.err.println("replaced unresolved placeholder " + value + " with: " + newValue);
			value = newValue;
		}
		return value;
	}

	public static String getEnv() {
		return env;
	}

	public static void setEnv(String env) {
		PropertiesHandler.env = env;
	}
}
