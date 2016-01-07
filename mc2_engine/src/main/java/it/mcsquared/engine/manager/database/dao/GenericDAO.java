package it.mcsquared.engine.manager.database.dao;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.mcsquared.engine.manager.DBManager;
import it.mcsquared.engine.manager.database.QueryBuilder;
import it.mcsquared.engine.manager.database.QueryHelper;
import it.mcsquared.engine.manager.database.Record;
import it.mcsquared.engine.manager.database.annotation.Column;
import it.mcsquared.engine.manager.database.annotation.Table;

public abstract class GenericDAO {

	private static final Logger logger = LoggerFactory.getLogger(GenericDAO.class);
	private static DBManager dbManager;
	private String defaultDb;

	public static void init(DBManager dbManager) {
		GenericDAO.dbManager = dbManager;
	}

	public GenericDAO(String db) throws SQLException {
		this.defaultDb = db;
		getQueryHelper();//just a test to check the db name
	}

	protected QueryHelper getQueryHelper() throws SQLException {
		return getQueryHelper(this.defaultDb);
	}

	protected QueryHelper getQueryHelper(String db) throws SQLException {
		QueryHelper qh = dbManager.getQueryHelper(db);
		return qh;
	}

	protected QueryHelper getTransactionQueryHelper() throws SQLException {
		QueryHelper qh = dbManager.getTransactionQueryHelper(this.defaultDb);
		return qh;
	}

	protected QueryHelper getTransactionQueryHelper(String db) throws SQLException {
		QueryHelper qh = dbManager.getTransactionQueryHelper(db);
		return qh;
	}

	protected String getSearchLikeParam(String key) {
		String keyParam = key;
		if (keyParam == null || keyParam.isEmpty()) {
			keyParam = "%";
		} else {
			keyParam = "%" + keyParam.toUpperCase() + "%";
		}
		return keyParam;
	}

	@SuppressWarnings("unchecked")
	protected <T> List<T> getList(QueryBuilder qb, List<Object> params, Class<T> clazz) throws SQLException {
		try {
			List<Record> rs = getQueryHelper().selectRecords(qb, params);
			List<T> ts = new ArrayList<>();
			T t;
			Method m = clazz.getMethod("getFromDb", Record.class);
			for (Record r : rs) {
				t = (T) m.invoke(null, r);
				ts.add(t);
			}
			return ts;
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}

	protected Boolean executeTransaction(Batch batch) throws SQLException {
		QueryHelper tqh = getTransactionQueryHelper();
		try {
			return batch.execute(tqh);
		} catch (Exception e) {
			logger.error("", e);
			try {
				tqh.rollback();
			} catch (Exception e1) {
				logger.error("", e);
			}
			throw new SQLException(e);
		} finally {
			tqh.commit();
		}
	}

	private Map<Class<?>, Map<String, Boolean>> fieldLikes = new ConcurrentHashMap<Class<?>, Map<String, Boolean>>();

	@SuppressWarnings("unchecked")
	public <T> List<T> selectRecords(Class<?> modelClass, Integer start, Integer length, Map<String, String> filters) throws Exception {
		if ((start != null && length == null) || (start == null && length != null)) {
			throw new IllegalArgumentException("start and length parameters not correctly set");
		}

		QueryBuilder qb = new QueryBuilder();
		qb.addToken("select * from").addToken(modelClass.getAnnotation(Table.class).name());
		List<Object> params = new ArrayList<Object>();
		if (filters != null && !filters.isEmpty()) {
			qb.addToken("where 1=1");

			Map<String, Boolean> flm = fieldLikes.get(modelClass);
			if (flm == null) {
				flm = new HashMap<String, Boolean>();
				fieldLikes.put(modelClass, flm);
				for (Field f : modelClass.getDeclaredFields()) {
					Column ca = f.getAnnotation(Column.class);
					if (ca != null) {
						String name = ca.name();
						flm.put(name, String.class.isAssignableFrom(f.getType()));
					}
				}
			}

			for (Entry<String, String> e : filters.entrySet()) {
				if (e.getValue() == null || e.getValue().isEmpty()) {
					continue;
				}
				if (flm.get(e.getKey())) {
					qb.addToken("and").addToken(e.getKey().toUpperCase()).addToken("like ?");
					params.add(getSearchLikeParam(e.getValue()));
				} else {
					qb.addToken("and").addToken(e.getKey().toUpperCase()).addToken("= ?");
					params.add(e.getValue());
				}
			}
		}

		QueryHelper qh = getQueryHelper();
		if (start != null) {
			String dialect = qh.getDialect();
			if (StringUtils.isBlank(dialect) || dialect.equals("mysql")) {
				qb.addToken("limit").addToken(length.toString());
				qb.addToken("offset").addToken(start.toString());
			} else if (dialect.equals("derby")) {
				qb.addToken("{");
				qb.addToken("limit").addToken(length.toString());
				qb.addToken("offset").addToken(start.toString());
				qb.addToken("}");
			}
		}

		List<Record> rs = qh.selectRecords(qb, params);
		List<T> result = new ArrayList<T>();
		Method getFromRecordMethod = modelClass.getDeclaredMethod("getFromDb", Record.class);
		for (Record r : rs) {
			T t = (T) getFromRecordMethod.invoke(modelClass, r);
			result.add(t);
		}
		return result;
	}

	public long getTotRecords(Class<?> modelClass) throws Exception {
		QueryBuilder qb = new QueryBuilder();
		qb.addToken("select count(*) tot from").addToken(modelClass.getAnnotation(Table.class).name());

		QueryHelper qh = getQueryHelper();
		Record r = qh.selectSingleRecord(qb);
		Number n = r.getValue("TOT");//needed to allow cross database compliance
		return n.longValue();
	}

	public long getFilteredRecords(Class<?> modelClass, Map<String, String> filters) throws Exception {
		QueryBuilder qb = new QueryBuilder();
		qb.addToken("select count(*) tot from").addToken(modelClass.getAnnotation(Table.class).name());
		List<Object> params = new ArrayList<Object>();
		if (filters != null && !filters.isEmpty()) {
			qb.addToken("where 1=1");

			Map<String, Boolean> flm = fieldLikes.get(modelClass);
			if (flm == null) {
				flm = new HashMap<String, Boolean>();
				fieldLikes.put(modelClass, flm);
				for (Field f : modelClass.getDeclaredFields()) {
					Column ca = f.getAnnotation(Column.class);
					if (ca != null) {
						String name = ca.name();
						flm.put(name, String.class.isAssignableFrom(f.getType()));
					}
				}
			}

			for (Entry<String, String> e : filters.entrySet()) {
				if (e.getValue() == null || e.getValue().isEmpty()) {
					continue;
				}
				if (flm.get(e.getKey())) {
					qb.addToken("and").addToken(e.getKey().toUpperCase()).addToken("like ?");
					params.add(getSearchLikeParam(e.getValue()));
				} else {
					qb.addToken("and").addToken(e.getKey().toUpperCase()).addToken("= ?");
					params.add(e.getValue());
				}
			}
		}

		QueryHelper qh = getQueryHelper();
		Record r = qh.selectSingleRecord(qb, params);
		Number n = r.getValue("TOT");//needed to allow cross database compliance
		return n.longValue();
	}
}
