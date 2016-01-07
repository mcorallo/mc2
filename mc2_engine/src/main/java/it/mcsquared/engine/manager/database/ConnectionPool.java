package it.mcsquared.engine.manager.database;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionPool {
	private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

	private BasicDataSource pool;
	private String name;
	private DBConfiguration dbConfiguration;

	public ConnectionPool(DBConfiguration dbConfiguration) {
		this.dbConfiguration = dbConfiguration;
		name = dbConfiguration.getName();
		init();
	}

	private void init() {
		pool = new BasicDataSource();
		pool.setDriverClassName(dbConfiguration.getDriverClassName());
		pool.setUrl(dbConfiguration.getUrl());
		pool.setUsername(dbConfiguration.getUsername());
		pool.setPassword(dbConfiguration.getPassword());
		pool.setInitialSize(dbConfiguration.getInitialSize());
		pool.setMaxTotal(dbConfiguration.getMaxActive());
		pool.setMinIdle(dbConfiguration.getMinIdle());
		pool.setTestWhileIdle(dbConfiguration.isTestWhileIdle());
		pool.setValidationQuery(dbConfiguration.getValidationQuery());
	}

	public Connection getConnection() throws SQLException {
		return pool.getConnection();
	}

	void returnConnection(Connection conn) {
		try {
			conn.close();
		} catch (SQLException e) {
			logger.error("", e);
			try {
				reset(false);
			} catch (SQLException e1) {
				logger.error("", e);
				pool = null;
			}
		}
	}

	private void reset(boolean commit) throws SQLException {
		pool.close();
		init();
	}

	public String getName() {
		return name;
	}

	public BasicDataSource getDataSource() {
		return pool;
	}

	public DBConfiguration getDbConfiguration() {
		return dbConfiguration;
	}

}
