package it.mcsquared.engine.manager;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.manager.database.NoSingleResultException;
import it.mcsquared.engine.manager.database.QueryBuilder;
import it.mcsquared.engine.manager.database.QueryHelper;
import it.mcsquared.engine.manager.database.Record;
import it.mcsquared.engine.test.GenericTest;

public class DBManagerTest extends GenericTest {

	private static final String TESTDB = "testdb";

	private DBManager dbManager;
	private Mc2Engine engine = getEngine();

	@Test
	public void simpleQueryTest() throws Exception {
		QueryHelper qh = dbManager.getQueryHelper(TESTDB);
		QueryBuilder qb = new QueryBuilder();
		qb.addToken("select * from test1");
		Record r = qh.selectSingleRecord(qb);
		assertNotNull(r);
		assertEquals("test-name", r.getValue("NAME"));
	}

	@Test
	public void simpleInsertTest() throws Exception {
		QueryHelper qh = dbManager.getQueryHelper(TESTDB);
		QueryBuilder qb = new QueryBuilder();

		qb.addToken("insert into test1 (name) values (?)");
		qh.insert(qb, "test-name-2");

		qb.clear();
		qb.addToken("select * from test1");
		List<Record> rs = qh.selectRecords(qb);
		assertNotNull(rs);
		assertEquals(2, rs.size());
	}

	@Test
	public void noSingleResultTest() throws Exception {
		QueryHelper qh = dbManager.getQueryHelper(TESTDB);
		QueryBuilder qb = new QueryBuilder();

		qb.addToken("insert into test1 (name) values (?)");
		qh.insert(qb, "test-name-2");

		qb.clear();
		qb.addToken("select * from test1");
		boolean pass = false;
		try {
			qh.selectSingleRecord(qb);
		} catch (NoSingleResultException e) {
			pass = true;
		}

		assertTrue(pass);
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

		qb.clear();
		qb.addToken("insert into test1 (name) values (?)");
		qh.insert(qb, "test-name");
	}

	@Override
	protected void localTearDown() throws Exception {
		QueryHelper qh = dbManager.getQueryHelper(TESTDB);
		QueryBuilder qb = new QueryBuilder();
		qb.addToken("drop table test1");
		qh.executeUpdate(qb);
	}
}
