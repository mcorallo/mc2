package it.mcsquared.engine;

import it.mcsquared.engine.manager.ConfigurationManager;
import it.mcsquared.engine.manager.DBManager;
import it.mcsquared.engine.manager.EmailManager;
import it.mcsquared.engine.manager.LDAPManager;
import it.mcsquared.engine.manager.LabelsManager;
import it.mcsquared.engine.manager.PropertiesHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mc2Engine {

	public static final String ENV = "env";
	public static final String UNIT_TESTS_ENV = "unit-tests";
	public static final String TEST_ENV = "test";
	public static final String LOCAL_ENV = "local";
	public static final String PROD_ENV = "prod";

	private String confDir;
	private LabelsManager labelsManager;
	private LDAPManager ldapManager;
	private EmailManager mailManager;
	private PropertiesHandler systemPropertiesHandler;
	private ConfigurationManager configurationManager;
	private DBManager dbManager;

	private String env;
	private static Map<String, Mc2Engine> engines = new HashMap<>();

	public static Mc2Engine init(String appName, String configurationFolder) throws Exception {
		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");// make all loggers asynchronous

		Mc2Engine engine = new Mc2Engine(configurationFolder);
		engines.put(appName, engine);
		return engine;
	}

	public static Mc2Engine getInstance(String appName) throws Exception {
		return engines.get(appName);
	}

	private Mc2Engine(String configurationFolder) throws Exception {
		initConfDir(configurationFolder);

		// configuration manager
		configurationManager = new ConfigurationManager(confDir);

		// logger
		InputStream stream = configurationManager.getFileInputStream("log4j2.xml");
		ConfigurationSource source = new ConfigurationSource(stream);
		Configurator.initialize(null, source);

		Logger logger = LoggerFactory.getLogger(Mc2Engine.class);
		logger.error("---------------> log4j2 initialized [{}, {}]", new File(".").getAbsolutePath(), confDir);
		logger.info("---------------> log4j2 initialized [{}, {}]", new File(".").getAbsolutePath(), confDir);
		logger.debug("---------------> log4j2 initialized [{}, {}]", new File(".").getAbsolutePath(), confDir);

		// system properties
		FileInputStream propertiesStream = configurationManager.getFileInputStream("system.properties");
		systemPropertiesHandler = new PropertiesHandler(propertiesStream);
		env = systemPropertiesHandler.getProperty(ENV);
		System.err.println("******************************************************");
		System.err.println("******************************************************");
		System.err.println("******************************************************");
		System.err.println("******************************************************");
		System.err.println("******************************************************");
		System.err.println("******************************************************");
		System.err.println("ENVIRONMENT: " + env + " [" + System.getProperty("environment") + "]");
		System.err.println("******************************************************");
		System.err.println("******************************************************");
		System.err.println("******************************************************");
		System.err.println("******************************************************");
		System.err.println("******************************************************");

		PropertiesHandler.setEnv(env);

		this.labelsManager = new LabelsManager(this);
		this.ldapManager = new LDAPManager(this);
		this.mailManager = new EmailManager(this);
		this.dbManager = new DBManager(this);

		logger.error("Engine initialized on env " + env);
	}

	private void initConfDir(String configurationFolder) {
		confDir = configurationFolder;

		if (confDir == null || confDir.isEmpty()) {
			System.err.println("******************************************************");
			System.err.println("******************************************************");
			System.err.println("******************************************************");
			System.err.println("******************************************************");
			System.err.println("******************************************************");
			System.err.println("******************************************************");
			String error = "configuration folder " + configurationFolder + " does not exists";
			System.err.println(error);
			System.err.println("******************************************************");
			System.err.println("******************************************************");
			System.err.println("******************************************************");
			System.err.println("******************************************************");
			System.err.println("******************************************************");
			throw new IllegalStateException(error);
		}
	}

	public String getCurrentEnvironment() {
		return env;
	}

	public boolean isTestEnv() {
		return TEST_ENV.equals(getCurrentEnvironment()) || isLocalEnv();
	}

	public boolean isLocalEnv() {
		return LOCAL_ENV.equals(getCurrentEnvironment()) || UNIT_TESTS_ENV.equals(getCurrentEnvironment());
	}

	public boolean isProductionEnv() {
		return PROD_ENV.equals(getCurrentEnvironment());
	}

	public String getSystemProperty(String property) {
		return systemPropertiesHandler.getProperty(property);
	}

	public String[] getSystemProperties(String property) {
		return systemPropertiesHandler.getProperties(property);
	}

	public String getConfDir() {
		return confDir;
	}

	public LabelsManager getLabelsManager() {
		return labelsManager;
	}

	public LDAPManager getLdapManager() {
		return ldapManager;
	}

	public EmailManager getMailManager() {
		return mailManager;
	}

	public ConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

	public DBManager getDBManager() throws Exception {
		return dbManager;
	}

}
