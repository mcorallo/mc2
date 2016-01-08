package it.mcsquared.engine.entitygen.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.mcsquared.engine.entitygen.Generator;

public class Tables {

	public static void generate() throws Exception {
		String packageName = Generator.getGenerationProperty("generated.sources.package") + ".model";

		String[] databases = Generator.getGenerationProperties("databases");

		for (String db : databases) {

			String jdbcDriver = Generator.getGenerationProperty(db + ".jdbc.driver");
			String jdbcCatalog = Generator.getGenerationProperty(db + ".jdbc.catalog");
			String jdbcUrl = Generator.getGenerationProperty(db + ".jdbc.url");
			String jdbcUsername = Generator.getGenerationProperty(db + ".jdbc.username");
			String jdbcPassword = Generator.getGenerationProperty(db + ".jdbc.password");
			String[] tablesToAnalyze = Generator.getGenerationProperties(db + ".tables");

			List<Table> tables2 = loadTables(db, jdbcDriver, jdbcUrl, jdbcCatalog, jdbcUsername, jdbcPassword, packageName, Arrays.asList(tablesToAnalyze), true);

			String outputFolder = Generator.getGenerationProperty("generated.root.folder") + "/java";
			File folder = new File(outputFolder, packageName.replaceAll("\\.", "/"));
			if (folder.exists()) {
				for (File f : folder.listFiles()) {
					f.delete();
				}
				folder.delete();
			}
			folder.mkdirs();
			generateModelClasses(tables2, folder);
			for (Table table : tables2) {
				System.out.println(table);
				System.out.println("\n--------------------------\n\n");
			}
		}
	}

	public static List<File> generateModelClasses(List<Table> tables, File folder) throws IOException, FileNotFoundException {
		List<File> result = new ArrayList<>();
		for (Table table : tables) {
			String className = table.getClassName();
			String javaFile = className + ".java";
			File f = new File(folder + "/" + javaFile);
			f.createNewFile();
			PrintWriter p = new PrintWriter(f);
			p.print(table);
			p.close();
			result.add(f);
		}
		return result;
	}

	public static List<Table> loadTables(String driver, String url, String catalog, String username, String password, String packageName) throws Exception {
		return loadTables(catalog, driver, url, catalog, username, password, packageName, null, false);
	}

	public static List<Table> loadTables(String db, String driver, String url, String catalog, String username, String password, String packageName, List<String> tablesToAnalyze, boolean generate) throws Exception, IOException, URISyntaxException {
		Class.forName(driver);
		Connection connection;
		if (username != null && !username.isEmpty()) {
			connection = DriverManager.getConnection(url, username, password);
		} else {
			connection = DriverManager.getConnection(url);
		}
		DatabaseMetaData metaData = connection.getMetaData();

		List<Table> tables2 = new ArrayList<>();
		if (tablesToAnalyze != null && tablesToAnalyze.size() > 0) {
			for (String t : tablesToAnalyze) {
				List<Table> analyzed = analyze(packageName, catalog, metaData, t, db, generate);
				tables2.addAll(analyzed);
			}
		} else {
			List<Table> analyzed = analyze(packageName, catalog, metaData, null, db, generate);
			tables2.addAll(analyzed);
		}
		return tables2;
	}

	private static List<Table> analyze(String packageName, String jdbcCatalog, DatabaseMetaData metaData, String tableName, String db, boolean generate) throws SQLException, IOException, URISyntaxException, FileNotFoundException {
		ResultSet tables = metaData.getTables(jdbcCatalog, null, tableName, new String[] {
				"TABLE"
		});
		List<Table> tables2 = new ArrayList<>();
		while (tables.next()) {
			Table t = new Table(packageName);
			t.setDatabase(db);
			List<Column> cs = new ArrayList<>();
			t.setName(tables.getString("TABLE_NAME"));
			ResultSet columns = metaData.getColumns(jdbcCatalog, null, t.getName(), null);
			Class<?> colClass;
			while (columns.next()) {
				Column c = new Column();
				c.setName(columns.getString("COLUMN_NAME"));
				int dt = columns.getInt("DATA_TYPE");
				switch (dt) {
				case Types.BOOLEAN:
				case Types.BIT: {
					colClass = Boolean.class;
					c.setType("Boolean");
					break;
				}
				case Types.INTEGER: {
					colClass = Integer.class;
					c.setType("Integer");
					break;
				}
				case Types.DOUBLE: {
					colClass = Double.class;
					c.setType("Double");
					break;
				}
				case Types.VARCHAR: {
					colClass = String.class;
					c.setType("String");
					break;
				}
				case Types.CHAR: {
					colClass = null;
					c.setType("String");
					break;
				}
				case Types.TIMESTAMP:
				case Types.DATE: {
					colClass = Date.class;
					c.setType("Date");
					break;
				}
				default: {
					System.err.println(dt + ": " + columns.getString("TYPE_NAME"));
					colClass = null;
					c.setType("Object");
					break;
				}
				}
				if (colClass != null) {
					t.getImports().add(colClass.getCanonicalName());
				}
				cs.add(c);
			}
			t.setColumns(cs);
			t.generate();
			tables2.add(t);

		}
		return tables2;
	}
}
