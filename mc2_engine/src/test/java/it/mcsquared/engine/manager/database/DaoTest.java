package it.mcsquared.engine.manager.database;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.manager.DBManager;
import it.mcsquared.engine.manager.database.dao.GenericDAO;
import it.mcsquared.engine.test.GenericTest;

public class DaoTest extends GenericTest {

	private static final String TESTDB = "testdb";

	private DBManager dbManager;
	private Mc2Engine engine = getEngine();

	@Test
	public void wrongDbTest() throws Exception {
		boolean ok = false;
		try {
			@SuppressWarnings("unused")
			Test1Dao td = new Test1Dao("xxx");
		} catch (Exception e) {
			ok = true;
		}
		assertTrue(ok);
	}

	@Test
	public void simpleSelectTest() throws Exception {
		Test1Dao td = new Test1Dao(TESTDB);
		Test1 test1 = td.getTest1("test-name-1");
		assertEquals("test-name-1", test1.getName());
	}

	@Test
	public void selectRecordsTest() throws Exception {
		Test1Dao td = new Test1Dao(TESTDB);
		List<Test1> test1 = td.selectRecords(Test1.class, 0, 10, null);
		assertEquals(10, test1.size());

		test1 = td.selectRecords(Test1.class, 0, 50, null);
		assertEquals(50, test1.size());
	}

	@Test
	public void selectRecordsFilteredTest() throws Exception {
		Test1Dao td = new Test1Dao(TESTDB);
		Map<String, String> filter = new HashMap<>();
		filter.put("name", "-1");
		List<Test1> test1 = td.selectRecords(Test1.class, 0, 10000, filter);
		assertEquals(11, test1.size());

		filter.clear();
		filter.put("name", "-99");
		test1 = td.selectRecords(Test1.class, 0, 10000, filter);
		assertEquals(1, test1.size());

		filter.clear();
		filter.put("name", "-0");
		test1 = td.selectRecords(Test1.class, 0, 10000, filter);
		assertEquals(1, test1.size());
	}

	@Test
	public void getTotTest() throws Exception {
		Test1Dao td = new Test1Dao(TESTDB);
		assertEquals(100, td.getTotRecords());
	}

	@Test
	public void derbyLimitTest() throws Exception {
		DBConfiguration dbConfiguration = dbManager.getDbConfigs().get(TESTDB);
		String dialect = dbConfiguration.getDialect();
		if ("derby".equals(dialect)) {
			String url = dbConfiguration.getUrl();

			try (Connection conn = DriverManager.getConnection(url)) {
				PreparedStatement ps = conn.prepareStatement("SELECT * FROM test1 ORDER BY name { LIMIT 2 OFFSET 10 }");
				ResultSet rs = ps.executeQuery();
				int count = 0;
				while (rs.next()) {
					count++;
				}
				assertEquals(2, count);
			}

			try (Connection conn = DriverManager.getConnection(url)) {
				PreparedStatement ps = conn.prepareStatement("SELECT * FROM test1 ORDER BY name { LIMIT 10}");
				ResultSet rs = ps.executeQuery();
				int count = 0;
				while (rs.next()) {
					count++;
				}
				assertEquals(10, count);
			}

			try (Connection conn = DriverManager.getConnection(url)) {
				PreparedStatement ps = conn.prepareStatement("SELECT * FROM test1 ORDER BY id { LIMIT 1 OFFSET 2}");
				ResultSet rs = ps.executeQuery();
				rs.next();
				assertEquals("test-name-2", rs.getString("name"));
			}
		}
	}

	@Test
	public void derbyLikeTest() throws Exception {
		DBConfiguration dbConfiguration = dbManager.getDbConfigs().get(TESTDB);
		String dialect = dbConfiguration.getDialect();
		if ("derby".equals(dialect)) {
			String url = dbConfiguration.getUrl();

			try (Connection conn = DriverManager.getConnection(url)) {
				PreparedStatement ps = conn.prepareStatement("select * from test1 where 1=1 and NAME like ? order by id { limit 10 offset 0 }");
				ps.setString(1, "%1%");
				ResultSet rs = ps.executeQuery();
				rs.next();
				assertEquals("test-name-1", rs.getString("name"));
			}
		}
	}

	@Override
	protected void localSetup() throws Exception {
		dbManager = engine.getDBManager();

		QueryHelper qh = dbManager.getQueryHelper(TESTDB);
		QueryBuilder qb = new QueryBuilder();
		qb.addToken("create table test1 (");
		qb.addToken("	ID		INTEGER				NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),");
		qb.addToken("	NAME    VARCHAR(255)		NOT NULL");
		qb.addToken(")");
		qh.executeUpdate(qb);

		for (int i = 0; i < 100; i++) {
			qb.clear();
			qb.addToken("insert into test1 (name) values (?)");
			qh.insert(qb, "test-name-" + i);
		}
	}

	@Override
	protected void localTearDown() throws Exception {
		QueryHelper qh = dbManager.getQueryHelper(TESTDB);
		QueryBuilder qb = new QueryBuilder();
		qb.addToken("drop table test1");
		qh.executeUpdate(qb);
	}
}

class Test1Dao extends GenericDAO {
	public Test1Dao(String db) throws SQLException {
		super(db);
	}

	public Test1 getTest1(String name) throws SQLException {
		QueryBuilder qb = new QueryBuilder();
		qb.addToken("select * from test1 where name = ?");
		Record r = getQueryHelper().selectSingleRecord(qb, name);
		return Test1.getFromDb(r);
	}

	public long getTotRecords() throws Exception {
		return super.getTotRecords(Test1.class);
	}
};