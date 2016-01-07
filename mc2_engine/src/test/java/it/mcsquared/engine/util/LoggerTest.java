package it.mcsquared.engine.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerTest {
	@Test
	public void patternTest() throws Exception {
		Logger logger = LoggerFactory.getLogger(getClass());
		logger.error("X");
		logger.info("Y");
		logger.debug("Z");
	}
}
