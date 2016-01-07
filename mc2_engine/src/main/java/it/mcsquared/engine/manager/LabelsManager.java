package it.mcsquared.engine.manager;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.mcsquared.engine.Mc2Engine;

public class LabelsManager {
	private static final Logger logger = LoggerFactory.getLogger(LabelsManager.class);

	private static final String LABELS_FOLDER = "labels/";
	public static final String VALUES_SEPARATOR = ";";
	public static final Locale DEFAULT_LOCALE = Locale.US;
	public static final String DEFAULT_LOCALE_STRING = DEFAULT_LOCALE.toString();// default locale: en_US

	private static Map<String, Map<String, String>> labelsCache = new HashMap<>();
	private static Map<String, PropertiesHandler> labelsProperties = new ConcurrentHashMap<>();

	private Locale locale = DEFAULT_LOCALE;

	private boolean testEnv = false;

	private Mc2Engine engine;

	public LabelsManager(Mc2Engine engine) {
		this.engine = engine;
		testEnv = engine.isTestEnv();
	}

	private String getLabelString(String localeString, String key) {
		String label;
		PropertiesHandler l = getLabels(localeString);
		if (l == null) {
			label = null;
		} else {
			label = l.getProperty(key);
		}

		if (label == null && !locale.toString().equals(DEFAULT_LOCALE_STRING)) {
			// if the label is not found in the required language, returns the label for the default one
			l = labelsProperties.get(DEFAULT_LOCALE_STRING);
			label = l.getProperty(key);
		}

		return label;
	}

	public String getLabelFormatted(String key, Object... params) {
		return String.format(locale, get(key), params);
	}

	public String get(String key) {
		return getLabel(locale.toString(), key);
	}

	public String getEscapedHtml(String key) {
		return getEscapedHtml(locale.toString(), key);
	}

	public String[] getLabelValues(String key) {
		return getLabelValues(locale.toString(), key);
	}

	public String[] getLabelValues(String localeString, String key) {
		PropertiesHandler l = getLabels(localeString);
		if (l == null) {
			l = labelsProperties.get(DEFAULT_LOCALE_STRING);
		}

		String[] labels = l.getProperties(key);

		if (labels == null && !locale.equals(DEFAULT_LOCALE_STRING)) {
			// if the label is not found in the required language, returns the label for the default one
			l = labelsProperties.get(DEFAULT_LOCALE_STRING);
			labels = l.getProperties(key);
		}
		return labels;
	}

	public String getEscapedHtml(String locale, String key) {
		return StringEscapeUtils.escapeHtml4(getLabel(locale, key)).replaceAll("\\'", "&apos;");
	}

	public String getLabel(String locale, String key) {

		String label = null;
		synchronized (labelsCache) {
			Map<String, String> localeCache = labelsCache.get(locale);
			if (localeCache == null) {
				localeCache = new HashMap<>();
				labelsCache.put(locale, localeCache);
			}
			if (!testEnv && localeCache.containsKey(key)) {
				label = localeCache.get(key);
			} else {
				label = getLabelString(locale, key);
				localeCache.put(key, label);
			}
		}

		if (label == null) {
			label = "NOT FOUND: " + key + " [" + locale + "]";
		} else if (label.isEmpty() && testEnv) {
			label = "EMPTY: " + key + " [" + locale + "]";
		}

		return label;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setDefaultLocale() {
		setLocale(DEFAULT_LOCALE);
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
		String l = locale.toString();

		synchronized (labelsCache) {
			if (!labelsCache.containsKey(l)) {
				labelsCache.put(l, new HashMap<String, String>());
			}
		}
	}

	@SuppressWarnings("serial")
	public Map<String, String> getLabels() {
		Map<String, String> result = new HashMap<String, String>() {
			@Override
			public String get(Object key) {
				return getLabel(locale.toString(), (String) key);
			}
		};

		return result;
	}

	private PropertiesHandler getLabels(String localeString) {
		PropertiesHandler l = labelsProperties.get(localeString);
		if (l == null) {
			try {
				InputStream stream = engine.getConfigurationManager().getFileInputStream(LABELS_FOLDER + "labels_" + localeString + ".properties");
				if (stream != null) {
					l = new PropertiesHandler((FileInputStream) stream);
					labelsProperties.put(localeString, l);
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return l;
	}
}
