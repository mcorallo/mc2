package it.mcsquared.engine.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationManager {
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

	private String confDir;

	public ConfigurationManager(String confDir) {
		this.confDir = confDir;
		logger.info("ConfigurationManager initialized: " + new File(confDir).getAbsolutePath());
	}

	public FileInputStream getFileInputStream(String relativePath) throws FileNotFoundException {
		File file = new File(confDir, relativePath);
		logger.debug("loading input stream: " + file.getAbsolutePath());
		InputStream stream = new FileInputStream(file);
		return (FileInputStream) stream;
	}

	public File getConfigurationFile(String relativePath) throws FileNotFoundException {
		File file = new File(confDir, relativePath);
		logger.debug("loading file: " + file.getAbsolutePath());
		return file;
	}
}
