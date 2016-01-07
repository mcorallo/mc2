package it.mcsquared.engine.manager;

import static org.junit.Assert.assertEquals;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.junit.Test;

import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.test.GenericTest;

public class LabelsManagerTest extends GenericTest {
	private Mc2Engine engine = getEngine();

	@Test
	public void getLabelTest() throws Exception {
		LabelsManager labelsManager = engine.newLabelsManager();
		String label;

		//default locale not found test
		label = labelsManager.get("none");
		assertEquals("NOT FOUND: none [en_US]", label);

		//default locale empty test
		//used in test environments when no label value has been defined, but the label is declared. in production env the result would be an empty string
		label = labelsManager.get("test.empty");
		assertEquals("EMPTY: test.empty [en_US]", label);

		//default locale test
		label = labelsManager.get("test");
		assertEquals("test english", label);

		//specific locale test
		label = labelsManager.getLabel(Locale.ITALY.toString(), "test");
		assertEquals("test italiano", label);

		//other specific locale test
		label = labelsManager.getLabel(Locale.US.toString(), "test");
		assertEquals("test english", label);

		//default locale test
		label = labelsManager.get("test");
		assertEquals("test english", label);

		//change instance locale test
		labelsManager.setLocale(Locale.ITALY);
		label = labelsManager.get("test");
		assertEquals("test italiano", label);

		//fallback to default locale
		label = labelsManager.getLabel("FAKE", "test");
		assertEquals("test english", label);
	}

	@Test
	public void getLabelFormattedTest() throws Exception {
		LabelsManager labelsManager = engine.newLabelsManager();
		labelsManager.setDefaultLocale();
		String label;

		//formatted string test
		label = labelsManager.getLabelFormatted("test.formatted.string", "engine");
		assertEquals("test engine", label);

		//formatted integer test
		label = labelsManager.getLabelFormatted("test.formatted.integer", 100);
		assertEquals("test 100", label);

		//formatted decimal test
		label = labelsManager.getLabelFormatted("test.formatted.decimal", 100d);

		String c = System.getProperty("user.country");
		String l = System.getProperty("user.language");
		char decimalSeparator = new DecimalFormatSymbols(Locale.forLanguageTag(l + "-" + c)).getDecimalSeparator();

		assertEquals("test 100" + decimalSeparator + "000000", label);
	}

	@Test
	public void getHtmlTest() throws Exception {
		LabelsManager labelsManager = engine.newLabelsManager();
		labelsManager.setDefaultLocale();
		String label;

		//html string test
		label = labelsManager.getEscapedHtml("test.html");
		assertEquals("&lt;span&gt;this is an html test &amp;gt;&lt;/span&gt;", label);
	}

	@Test
	public void getLabelValuesTest() throws Exception {
		LabelsManager labelsManager = engine.newLabelsManager();
		labelsManager.setDefaultLocale();
		String[] labels;

		//default locale test
		labels = labelsManager.getLabelValues("test.values");
		assertEquals(2, labels.length);

		//specific locale test
		labels = labelsManager.getLabelValues(Locale.ITALY.toString(), "test.values");
		assertEquals(3, labels.length);

		//other specific locale test
		labels = labelsManager.getLabelValues(Locale.US.toString(), "test.values");
		assertEquals(2, labels.length);

		//default locale test
		labels = labelsManager.getLabelValues("test.values");
		assertEquals(2, labels.length);

		//change instance locale test
		labelsManager.setLocale(Locale.ITALY);
		labels = labelsManager.getLabelValues("test.values");
		assertEquals(3, labels.length);

		//fallback to default locale
		labels = labelsManager.getLabelValues("FAKE", "test.values");
		assertEquals(2, labels.length);
	}
}
