package it.mcsquared.engine.manager;

import static org.junit.Assert.*;
import it.mcsquared.engine.EngineTest;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class ConfigurationManagerTest extends EngineTest {

	@Test
	public void getFileTest() throws Exception {
		String fileName = "test.conf.file";
		File cf = new File(confDir, fileName);
		if (!cf.exists()) {
			cf.createNewFile();
			cf.deleteOnExit();
		}
		File configurationFile = engine.getConfigurationManager().getConfigurationFile(fileName);
		assertNotNull(configurationFile);
		assertTrue(configurationFile.exists());
	}

	@Test
	public void getInputStreamTest() throws Exception {
		String fileName = "test.conf.file.2";
		File cf = new File(confDir, fileName);
		if (!cf.exists()) {
			cf.createNewFile();
			cf.deleteOnExit();
		}
		FileInputStream configurationFileStrem = engine.getConfigurationManager().getFileInputStream(fileName);
		assertNotNull(configurationFileStrem);
		assertEquals("", IOUtils.toString(configurationFileStrem));
	}
}
