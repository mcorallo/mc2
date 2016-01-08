package it.mcsquared.engine.test;

import it.mcsquared.engine.test.PrivateMethodDetails.PrivateMethodParam;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.Wiser;

public class GenericTest {
	private static final String APP_NAME = "test";
	private static Logger logger;
	private static Object engine;
	protected static String confDir;
	protected static String sqliteDbFile;

	protected Wiser mailServer;

	static {
		try {
			String parentTarget = System.getProperty("parent.target.dir");
			String relativePath;
			if (parentTarget == null) {
				relativePath = "../";
			} else {
				relativePath = "../../";
			}
			String mc2EngineTestPath = new File(parentTarget, relativePath + "mc2_engine_test").getAbsolutePath();
			System.err.println(mc2EngineTestPath);
			confDir = mc2EngineTestPath + "/src/main/resources/conf";
			sqliteDbFile = confDir + "/../temp.sqlite";
			System.setProperty("test.db.file", sqliteDbFile);

			Class<?> mc2EngineClass = Class.forName("it.mcsquared.engine.Mc2Engine");
			Method initMethod = mc2EngineClass.getMethod("init", String.class, String.class);
			engine = initMethod.invoke(null, APP_NAME, confDir);

			logger = LoggerFactory.getLogger(GenericTest.class);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Rule
	public TestRule watcher = new TestWatcher() {
		@Override
		protected void starting(Description description) {
			logger.info(description.getMethodName() + " test started.");
		}

		@Override
		protected void succeeded(Description description) {
			logger.info(description.getMethodName() + " test succeded.");
		}
	};

	protected void startMailServer() {
		try {
			Class<?> mc2EngineClass = Class.forName("it.mcsquared.engine.Mc2Engine");
			Method getSystemPropertyMethod = mc2EngineClass.getMethod("getSystemProperty", String.class);
			String smtpPort = (String) getSystemPropertyMethod.invoke(engine, "mail.smtp.port");

			mailServer = new Wiser();
			mailServer.setPort(Integer.parseInt(smtpPort));
			mailServer.setHostname("localhost");
			mailServer.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T getEngine() {
		return (T) engine;
	}

	@Before
	public void setup() throws Exception {

		localSetup();
	}

	@After
	public void tearDown() throws Exception {
		localTearDown();
		if (mailServer != null) {
			mailServer.stop();
		}
	}

	/**
	 * Override to implement custom setup
	 */
	protected void localSetup() throws Exception {
		// do nothing
	}

	/**
	 * Override to implement custom teardown
	 */
	protected void localTearDown() throws Exception {
		// do nothing
	}

	@SuppressWarnings("unchecked")
	protected <T> T invokePrivateMethod(Object instance, PrivateMethodDetails details) throws Exception {
		List<PrivateMethodParam> params = details.getParams();

		List<Class<?>> paramClasses = new ArrayList<Class<?>>();
		List<Object> paramValues = new ArrayList<Object>();
		for (PrivateMethodParam p : params) {
			paramClasses.add(p.getClazz());
			paramValues.add(p.getValue());
		}

		Method m = details.getClazz().getDeclaredMethod(details.getName(), paramClasses.toArray(new Class<?>[] {}));
		m.setAccessible(true);
		return (T) m.invoke(instance, paramValues.toArray(new Object[] {}));
	}

	protected void injectMockedInstance(Object parentInstance, String fieldName, Object object) throws Exception {
		Field f = parentInstance.getClass().getDeclaredField(fieldName);
		f.setAccessible(true);
		f.set(parentInstance, object);
	}
}
