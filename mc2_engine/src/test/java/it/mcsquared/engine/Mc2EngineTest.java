package it.mcsquared.engine;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.junit.Test;

import it.mcsquared.engine.test.GenericTest;

public class Mc2EngineTest extends GenericTest {

	private Mc2Engine engine = getEngine();

	@Test
	public void initTest() throws Exception {
		assertNotNull(engine);
		assertTrue(engine.isLocalEnv());
		assertTrue(engine.isTestEnv());
		assertFalse(engine.isProductionEnv());
		assertNotNull(engine.getConfDir());
		assertNotNull(engine.getConfigurationManager());
		assertNotNull(engine.getCurrentEnvironment());
		// assertNotNull(engine.getDBManager());//not testable here
		assertNotNull(engine.newLabelsManager());
		assertNotNull(engine.getLdapManager());
		assertNotNull(engine.getMailManager());
		assertNotNull(engine.getSystemProperty("env"));
		assertNotNull(engine.getSystemProperties("env"));
	}

	@Test
	public void initValidationTest() throws Exception {

		boolean pass = false;
		try {
			Mc2Engine.init("test", null);
		} catch (IllegalStateException e) {
			pass = true;
		}

		assertTrue(pass);

		pass = false;
		try {
			Mc2Engine.init("test", "");
		} catch (IllegalStateException e) {
			pass = true;
		}

		pass = false;
		try {
			Mc2Engine.init("test", "aaa");
		} catch (FileNotFoundException e) {
			pass = true;
		}

		assertTrue(pass);
	}

}
