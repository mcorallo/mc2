package it.mcsquared.engine.manager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.manager.database.ConnectionPool;
import it.mcsquared.engine.manager.database.DBConfiguration;
import it.mcsquared.engine.manager.database.QueryHelper;

public class DBManager {
	private static Logger logger = LoggerFactory.getLogger(DBManager.class);

	private final Map<String, DBConfiguration> dbConfigs = new HashMap<String, DBConfiguration>();
	private Map<String, ConnectionPool> pools = new HashMap<String, ConnectionPool>();
	private boolean logQueries;

	public DBManager(Mc2Engine engine) throws Exception {

		logQueries = !engine.isProductionEnv();

		Map<String, DBConfiguration> tempMap = new HashMap<String, DBConfiguration>();

		String databaseProperties = "database.properties";
		FileInputStream stream;
		try {
			stream = engine.getConfigurationManager().getFileInputStream(databaseProperties);
		} catch (FileNotFoundException e) {
			logger.error(databaseProperties + " file not found");
			return;
		}
		PropertiesHandler dbPropertiesHandler = new PropertiesHandler(stream);

		for (String s : dbPropertiesHandler.getProperties("databases")) {
			DBConfiguration dbConfiguration = DBConfiguration.getFromProperties(dbPropertiesHandler, s);
			Class.forName(dbConfiguration.getDriverClassName());
			ConnectionPool pool = new ConnectionPool(dbConfiguration);
			pools.put(s, pool);
			tempMap.put(s, dbConfiguration);
		}
		String[] array = tempMap.keySet().toArray(new String[] {});
		Arrays.sort(array);
		List<String> keys = Arrays.asList(array);

		for (String i : keys) {
			dbConfigs.put(i, tempMap.get(i));
			logger.debug("Added db configuration: " + tempMap.get(i).getName());
		}

	}

	public QueryHelper getQueryHelper(String db) throws SQLException {
		return new QueryHelper(pools.get(db), false, logQueries, dbConfigs.get(db).getDialect());
	}

	//
	//	public Map<String, DBConfiguration> getDbConfigs() {
	//		return dbConfigs;
	//	}

	public QueryHelper getTransactionQueryHelper(String db) throws SQLException {
		return new QueryHelper(pools.get(db), true, logQueries, dbConfigs.get(db).getDialect());
	}

	//
	// public Connection getConnection() throws SQLException {
	// return pool.getConnection();
	// }
	//
	// public void closeConnection(Connection conn) {
	// if (conn != null) {
	// try {
	// conn.close();
	// } catch (SQLException e) {
	// // do nothing
	// }
	// }
	// }

	public boolean isLogQueries() {
		return logQueries;
	}

	public void setLogQueries(boolean logQueries) {
		this.logQueries = logQueries;
	}

	public Map<String, DBConfiguration> getDbConfigs() {
		return dbConfigs;
	}
}
