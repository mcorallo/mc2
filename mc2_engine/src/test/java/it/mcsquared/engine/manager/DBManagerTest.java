package it.mcsquared.engine.manager;

import static org.junit.Assert.*;
import it.mcsquared.engine.EngineTest;
import it.mcsquared.engine.manager.database.NoSingleResultException;
import it.mcsquared.engine.manager.database.QueryBuilder;
import it.mcsquared.engine.manager.database.QueryHelper;
import it.mcsquared.engine.manager.database.Record;

import java.util.List;

import org.junit.Test;

public class DBManagerTest extends EngineTest {

	private static final String TESTDB = "testdb";

	private DBManager dbManager;

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
		qb.addToken("	ID		INTEGER		PRIMARY KEY AUTOINCREMENT,");
		qb.addToken("	NAME    TEXT		NOT NULL");
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
		qb.addToken("drop table if exists test1");
		qh.executeUpdate(qb);
	}
}
