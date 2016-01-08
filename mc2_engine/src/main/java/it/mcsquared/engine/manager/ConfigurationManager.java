package it.mcsquared.engine.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ConfigurationManager {

	private String confDir;

	public ConfigurationManager(String confDir) {
		this.confDir = confDir;
	}

	public FileInputStream getFileInputStream(String relativePath) throws FileNotFoundException {
		File file = new File(confDir, relativePath);
		InputStream stream = new FileInputStream(file);
		return (FileInputStream) stream;
	}

	public File getConfigurationFile(String relativePath) throws FileNotFoundException {
		File file = new File(confDir, relativePath);
		return file;
	}
}
