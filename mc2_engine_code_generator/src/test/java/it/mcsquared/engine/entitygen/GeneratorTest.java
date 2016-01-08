package it.mcsquared.engine.entitygen;

import static org.junit.Assert.*;

import org.junit.Test;

public class GeneratorTest {

	@Test
	public void capitalizeTest() throws Exception {
		String capitalized = Generator.capitalize("xxx", false);
		assertEquals("Xxx", capitalized);
		capitalized = Generator.capitalize("xxx", true);
		assertEquals("xxx", capitalized);
		capitalized = Generator.capitalize("xxx_yyy", false);
		assertEquals("XxxYyy", capitalized);
		capitalized = Generator.capitalize("xxx__yyy", false);
		assertEquals("XxxYyy", capitalized);
	}

}
