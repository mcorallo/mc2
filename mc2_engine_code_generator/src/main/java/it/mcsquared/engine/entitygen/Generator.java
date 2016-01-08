package it.mcsquared.engine.entitygen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.handlebars.internal.Files;

import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.entitygen.db.Tables;
import it.mcsquared.engine.manager.PropertiesHandler;

public class Generator {

	private static PropertiesHandler generationProperties;
	private static Logger logger;
	private static Mc2Engine engine;

	public static void main(String[] args) throws Exception {
		//		String configurationFolder = "src/test/resources/conf";
		String configurationFolder = "src/test/resources/controllers-test/conf";
		generate(configurationFolder);
	}

	public static String init(String configurationFolder) throws Exception, FileNotFoundException {
		engine = Mc2Engine.init("generator", configurationFolder);
		Mc2Engine.init("generator", configurationFolder);
		logger = LoggerFactory.getLogger(Generator.class);

		File configurationFile = engine.getConfigurationManager().getConfigurationFile("generation.properties");
		generationProperties = new PropertiesHandler(new FileInputStream(configurationFile));

		String folder = generationProperties.getProperty("generated.root.folder");
		FileUtils.deleteQuietly(new File(folder));
		return folder;
	}

	public static void generate(String configurationFolder) throws Exception {

		String folder = init(configurationFolder);

		File generatedHtmlFolder = new File(folder, "html/");
		if (!generatedHtmlFolder.exists()) {
			generatedHtmlFolder.mkdirs();
		}

		File mainHtmlFile = new File(generatedHtmlFolder, "main.html");
		if (mainHtmlFile.exists()) {
			mainHtmlFile.delete();
		}
		mainHtmlFile.createNewFile();

		File templatesFolder = Generator.getConfigurationFile("templates");
		String homeTemplate = Files.read(new File(templatesFolder, "html/main.html"));
		// String homeContent = TemplateUtils.renderSingle(homeTemplate,
		// "navigation-items", navGenerated);

		try (FileWriter fw = new FileWriter(mainHtmlFile, true)) {
			fw.append(homeTemplate);
		}

		Tables.generate();
	}

	public static String getGeneratedFolder() {
		return getGenerationProperty("generated.root.folder");
	}

	public static String getGenerationProperty(String property) {
		return generationProperties.getProperty(property);
	}

	public static String[] getGenerationProperties(String property) {
		return generationProperties.getProperties(property);
	}

	public static File getConfigurationFile(String relativePath) throws FileNotFoundException {
		return engine.getConfigurationManager().getConfigurationFile(relativePath);
	}

	public static String capitalize(String s, boolean lowerFirst) {
		String n = WordUtils.capitalize(s.toLowerCase(), new char[] {
				'_'
		}).replaceAll("_", "");
		if (lowerFirst) {
			n = Character.toLowerCase(n.charAt(0)) + n.substring(1);
		}
		return n;
	}

}
