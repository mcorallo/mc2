package it.mcsquared.engine.entitygen;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import it.mcsquared.engine.Mc2EngineTest;

@Ignore
public class DbAnalyzerTest extends Mc2EngineTest {

	@BeforeClass
	public static void setupClass() throws Exception {
//		testRootPath = "target";
//		GenericTest.setupClass();
	}

	@Override
	protected void localSetup() throws Exception {
//
//		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
//		Class.forName(driver);
//		String jdbcUrl = engine.getSystemProperty("jdbc.url");
//
//		System.setProperty("jdbc.driver", driver);
//		Class.forName(driver);
//
//		try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
//			Statement stmnt;
//			QueryBuilder qb = new QueryBuilder();
//
//			qb.clear();
//			qb.addToken("create schema test");
//			stmnt = conn.createStatement();
//			stmnt.executeUpdate(qb.getQuery());
//
//			qb.clear();
//			qb.addToken("CREATE TABLE test.TEST_TABLE(");
//			qb.addToken("	COL1 INT NOT NULL,");
//			qb.addToken("	COL2 CHAR(25),");
//			qb.addToken("	COL3 VARCHAR(25),");
//			qb.addToken("	COL4 DECIMAL(10,2) NOT NULL,");
//			qb.addToken("	COL5 DATE,");
//			qb.addToken("	PRIMARY KEY (COL1)");
//			qb.addToken(")");
//
//			stmnt = conn.createStatement();
//			stmnt.executeUpdate(qb.getQuery());
//
//			qb.clear();
//			qb.addToken("CREATE TABLE test.client(");
//			qb.addToken("	id integer primary key,");
//			qb.addToken("	name varchar(255) NOT NULL,");
//			qb.addToken("	method varchar(255) NOT NULL,");
//			qb.addToken("	baseUrl varchar(500) NOT NULL,");
//			qb.addToken("	path varchar(500) NOT NULL");
//			qb.addToken(")");
//
//			stmnt = conn.createStatement();
//			stmnt.executeUpdate(qb.getQuery());
//		}
	}

	@Override
	protected void localTearDown() throws Exception {
		//		FileUtils.deleteQuietly(new File(Generator.getGeneratedFolder()));
	}

	@Test
	public void generationTest() throws Exception {
		String configurationFolder = "src/test/resources/conf";
		Generator.generate(configurationFolder);

		File f = new File(Generator.getGeneratedFolder(), "java/it/tests/model/TESTTABLE.java");

		assertTrue(f.exists());

	}

}
