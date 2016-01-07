package it.mcsquared.engine.manager.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryHelper {

	private static final Logger logger = LoggerFactory.getLogger(QueryHelper.class);

	private ConnectionPool pool;
	private Connection connection;

	private boolean transaction;

	private String dbName;
	private String dialect;
	private boolean logQueries = false;

	public QueryHelper(ConnectionPool pool, boolean transaction, boolean logQueries, String dialect) throws SQLException {
		this.logQueries = logQueries;
		this.dialect = dialect;
		this.dbName = pool.getName();
		this.pool = pool;
		this.transaction = transaction;
		if (transaction) {
			connection = this.pool.getConnection();
			connection.setAutoCommit(!transaction);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		}
	}

	private void setInternalConnection() throws SQLException {
		if (transaction) {
			// in caso di transazione verifica se la connection e' gia' stata inizializzata, altrimenti la inizializza
			if (connection == null) {
				connection = this.pool.getConnection();
				connection.setAutoCommit(!transaction);
				connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			}
		} else {
			// in caso la transazione non sia richiesta, ritorna una nuova conenction presa dal pool
			connection = this.pool.getConnection();
		}
	}

	/**
	 * Returns the first record found in the query results.
	 * 
	 * @param database
	 * @param queryBuilder
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public Record selectFirstRecord(QueryBuilder queryBuilder, Object... params) throws SQLException {
		logQuery(dbName, queryBuilder, params);
		setInternalConnection();
		try {
			PreparedStatement ps = connection.prepareStatement(queryBuilder.getQuery());
			if (params != null) {
				for (int i = 1; i <= params.length; i++) {
					ps.setObject(i, params[i - 1]);
				}
			}
			ResultSet rs = ps.executeQuery();
			Record r = null;
			if (rs.next()) {
				r = getRecord(rs);
			}
			logQueryResult(dbName, queryBuilder, r);
			return r;
		} finally {
			resetConnection();
		}
	}

	/**
	 * Fails if the query returns 0 or more than 1 results.
	 * 
	 * @param queryBuilder
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public Record selectSingleRecord(QueryBuilder queryBuilder, Object... params) throws SQLException {
		List<Record> records = selectRecords(queryBuilder, params);

		if (records.size() == 0) {
			return null;
		} else if (records.size() == 1) {
			return records.get(0);
		} else {
			throw new NoSingleResultException("The query returned " + records.size() + " records. Expected 1.");
		}
	}

	/**
	 * Fails if the query returns 0 or more than 1 results.
	 * 
	 * @param queryBuilder
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public Record selectSingleRecord(QueryBuilder queryBuilder, List<Object> params) throws SQLException {
		List<Record> records = selectRecords(queryBuilder, params);

		if (records.size() == 0) {
			return null;
		} else if (records.size() == 1) {
			return records.get(0);
		} else {
			throw new NoSingleResultException("The query returned " + records.size() + " records. Expected 1.");
		}
	}

	private Record getRecord(ResultSet rs) throws SQLException {
		Record r = new Record();
		Map<String, Object> cells = r.getCells();
		int columnCount = rs.getMetaData().getColumnCount();
		for (int i = 1; i <= columnCount; i++) {
			cells.put(rs.getMetaData().getColumnLabel(i).toUpperCase(), rs.getObject(i));
		}
		return r;
	}

	/**
	 * Returns the first value returned by the given query, as a string. <br>
	 * <br>
	 * No transaction needed, connection automatically closed.
	 * 
	 * @param queryBuilder
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public String getString(QueryBuilder queryBuilder, Object... params) throws SQLException {
		String result = null;
		Record r = selectFirstRecord(queryBuilder, params);
		if (r != null) {
			Collection<Object> values = r.getCells().values();
			if (!values.isEmpty()) {
				result = (String) values.iterator().next();
				return result;
			}
		}
		return null;
	}

	public List<Record> selectRecords(QueryBuilder queryBuilder, List<Object> params) throws SQLException {
		return selectRecords(queryBuilder, params != null ? params.toArray(new Object[] {}) : null);
	}

	/**
	 * Returns the resulting records list.<br>
	 * 
	 * @param queryBuilder
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public List<Record> selectRecords(QueryBuilder queryBuilder, Object... params) throws SQLException {
		logQuery(dbName, queryBuilder, params);
		setInternalConnection();
		List<Record> result;
		try {
			PreparedStatement ps = connection.prepareStatement(queryBuilder.getQuery());
			if (params != null) {
				for (int i = 1; i <= params.length; i++) {
					ps.setObject(i, params[i - 1]);
				}
			}
			ResultSet rs = ps.executeQuery();
			result = new ArrayList<Record>();
			if (rs.next()) {
				Record r;
				do {
					r = getRecord(rs);
					result.add(r);
				} while (rs.next());
			}
			logQueryResult(dbName, queryBuilder, result);
		} finally {
			resetConnection();
		}
		return result;
	}

	/**
	 * Executes an update query.<br>
	 * <br>
	 * If this QueryHelper is set to explicitly handle the db transactions, the connection will not be closed.
	 * 
	 * @param queryBuilder
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public int executeUpdate(QueryBuilder queryBuilder, List<Object> params) throws SQLException {
		return executeUpdate(queryBuilder, params != null ? params.toArray(new Object[] {}) : null);
	}

	/**
	 * Executes an update query.<br>
	 * <br>
	 * If this QueryHelper is set to explicitly handle the db transactions, the connection will not be closed.
	 * 
	 * @param queryBuilder
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public int executeUpdate(QueryBuilder queryBuilder, Object... params) throws SQLException {
		logQuery(dbName, queryBuilder, params);
		setInternalConnection();
		int result;
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(queryBuilder.getQuery());
			if (params != null) {
				for (int i = 1; i <= params.length; i++) {
					ps.setObject(i, params[i - 1]);
				}
			}
			result = ps.executeUpdate();
			logQueryResult(dbName, queryBuilder, result);
		} finally {
			if (ps != null) {
				ps.close();
			}
			resetConnection();
		}
		return result;
	}

	/**
	 * Executes the given insert query and returns the last inserted id.<br>
	 * <br>
	 * If this QueryHelper is set to explicitly handle the db transactions, the connection will not be closed.
	 * 
	 * @param queryBuilder
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public int insertAndGetId(QueryBuilder queryBuilder, List<Object> params) throws SQLException {
		return insertAndGetId(queryBuilder, params != null ? params.toArray(new Object[] {}) : null);
	}

	public int insertAndGetId(QueryBuilder queryBuilder, Object... params) throws SQLException {
		String query = queryBuilder.getQuery();
		logQuery(dbName, queryBuilder, params);
		if (!query.trim().toUpperCase().startsWith("INSERT ")) {
			throw new NoInsertQueryException("The requested query does not start with the words 'INSERT'");
		}

		setInternalConnection();
		int result;
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			if (params != null) {
				for (int i = 1; i <= params.length; i++) {
					ps.setObject(i, params[i - 1]);
				}
			}
			result = ps.executeUpdate();
			logQueryResult(dbName, queryBuilder, result);
			if (result > 0) {
				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next()) {
					result = rs.getInt(1);
					logQueryResult(dbName, queryBuilder, result);
				}
			}
		} finally {
			if (ps != null) {
				ps.close();
			}
			resetConnection();
		}
		return result;
	}

	public int insert(QueryBuilder queryBuilder, Object... params) throws SQLException {
		return insert(queryBuilder, Arrays.asList(params));
	}

	/**
	 * Executes the given insert query and returns the last inserted id.<br>
	 * <br>
	 * If this QueryHelper is set to explicitly handle the db transactions, the connection will not be closed.
	 * 
	 * @param queryBuilder
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public int insert(QueryBuilder queryBuilder, List<Object> params) throws SQLException {
		String query = queryBuilder.getQuery();
		logQuery(dbName, queryBuilder, params);
		if (!query.trim().toUpperCase().startsWith("INSERT ")) {
			throw new NoInsertQueryException("The requested query does not start with the words 'INSERT'");
		}

		setInternalConnection();
		int result;
		try {
			PreparedStatement ps = connection.prepareStatement(query);
			if (params != null) {
				for (int i = 1; i <= params.size(); i++) {
					ps.setObject(i, params.get(i - 1));
				}
			}
			result = ps.executeUpdate();
			logQueryResult(dbName, queryBuilder, result);
		} finally {
			resetConnection();
		}
		return result;
	}

	private void resetConnection() throws SQLException {
		if (!transaction) {
			pool.returnConnection(connection);
		}
	}

	public void commit() throws SQLException {
		if (!connection.isClosed()) {
			connection.commit();
			pool.returnConnection(connection);
		}
	}

	public void rollback() throws SQLException {
		if (!connection.isClosed()) {
			connection.rollback();
			pool.returnConnection(connection);
		}
	}

	private void logQuery(String database, QueryBuilder queryBuilder, Object... params) {
		if (logger.isDebugEnabled() && logQueries) {
			StringBuilder sb = new StringBuilder();
			sb.append(queryBuilder.hashCode()).append(": executing query (").append(database).append("): ").append(queryBuilder).append(" --[");
			if (params != null) {
				for (Object o : params) {
					sb.append(o).append(",");
				}
			}
			sb.append("]");
			logger.debug(sb.toString());
		}
	}

	private void logQueryResult(String database, QueryBuilder queryBuilder, Object result) {
		if (logger.isDebugEnabled() && logQueries) {
			// StringBuilder sb = new StringBuilder();
			// sb.append("[").append(Thread.currentThread().getId()).append(" - ").append(queryBuilder.hashCode()).append("] query result: ").append(result);
			// logger.debug(sb.toString());
		}
	}

	public String getDialect() {
		return dialect;
	}

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}
}
