package it.mcsquared.engine.manager;

import static org.junit.Assert.*;
import it.mcsquared.engine.EngineTest;

import org.junit.Test;

public class PropertiesHandlerTest extends EngineTest {
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
