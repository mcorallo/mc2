package it.mcsquared.engine.manager;

import static org.junit.Assert.*;

import org.junit.Test;

import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.test.GenericTest;

public class PropertiesHandlerTest extends GenericTest {
	private Mc2Engine engine = getEngine();

	@Test
	public void checkUnresolvedTest() throws Exception {
		PropertiesHandler propertiesHandler = new PropertiesHandler(engine.getConfigurationManager().getFileInputStream("system.properties"));
		String property = propertiesHandler.getProperty("unresolved");
		assertEquals("${test-unresolved}", property);
		System.setProperty("test-unresolved", "resolved");

		property = propertiesHandler.getProperty("unresolved");
		assertEquals("resolved", property);
	}
}
