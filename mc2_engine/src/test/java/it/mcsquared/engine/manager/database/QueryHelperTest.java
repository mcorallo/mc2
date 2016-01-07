package it.mcsquared.engine.manager.database;

import static org.junit.Assert.*;

import org.junit.Test;

import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.manager.DBManager;
import it.mcsquared.engine.test.GenericTest;

public class QueryHelperTest extends GenericTest {

	private static final String TESTDB = "testdb";

	private DBManager dbManager;
	private Mc2Engine engine = getEngine();

	@Test
	public void wrongDbTest() throws Exception {
		boolean ok = false;
		try {
			@SuppressWarnings("unused")
			QueryHelper qh = dbManager.getQueryHelper("xxx");
		} catch (Exception e) {
			ok = true;
		}
		assertTrue(ok);
	}

	@Test
	public void simpleQueryTest() throws Exception {
		QueryHelper qh = dbManager.getQueryHelper(TESTDB);
		assertNotNull(qh);
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
