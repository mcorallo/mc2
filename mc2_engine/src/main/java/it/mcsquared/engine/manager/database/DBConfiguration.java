package it.mcsquared.engine.manager.database;

import it.mcsquared.engine.manager.PropertiesHandler;

public class DBConfiguration {

	private interface PropertiesKeys {
		public static final String PREFIX_JDBC = ".jdbc.";
		public static final String DRIVER = PREFIX_JDBC + "driver";
		public static final String DIALECT = PREFIX_JDBC + "dialect";
		public static final String URL = PREFIX_JDBC + "url";
		public static final String USER = PREFIX_JDBC + "user";
		public static final String PWD = PREFIX_JDBC + "pwd";
		public static final String POOL_INITIAL_SIZE = PREFIX_JDBC + "pool.initial.size";
		public static final String POOL_MIN_SIZE = PREFIX_JDBC + "pool.min.size";
		public static final String POOL_MAX_SIZE = PREFIX_JDBC + "pool.max.size";
		public static final String POOL_TEST_WHILE_IDLE = PREFIX_JDBC + "test.while.idle";
		public static final String POOL_VALIDATION_QUERY = PREFIX_JDBC + "validation.query";
	}

	private String name;
	private String driverClassName;
	private String dialect;
	private String url;
	private String username;
	private String password;

	private boolean testWhileIdle;
	private String validationQuery;

	private Integer initialSize;
	private Integer maxActive;
	private Integer minIdle;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isTestWhileIdle() {
		return testWhileIdle;
	}

	public void setTestWhileIdle(boolean testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}

	public String getValidationQuery() {
		return validationQuery;
	}

	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
	}

	public Integer getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(Integer initialSize) {
		this.initialSize = initialSize;
	}

	public Integer getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(Integer maxActive) {
		this.maxActive = maxActive;
	}

	public Integer getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(Integer minIdle) {
		this.minIdle = minIdle;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DBConfiguration [name=");
		builder.append(name);
		builder.append(", driverClassName=");
		builder.append(driverClassName);
		builder.append(", dialect=");
		builder.append(dialect);
		builder.append(", url=");
		builder.append(url);
		builder.append(", username=");
		builder.append(username);
		builder.append(", password=");
		builder.append(password);
		builder.append(", testWhileIdle=");
		builder.append(testWhileIdle);
		builder.append(", validationQuery=");
		builder.append(validationQuery);
		builder.append(", initialSize=");
		builder.append(initialSize);
		builder.append(", maxActive=");
		builder.append(maxActive);
		builder.append(", minIdle=");
		builder.append(minIdle);
		builder.append("]");
		return builder.toString();
	}

	public static DBConfiguration getFromProperties(PropertiesHandler propertiesHandler, String name) {
		DBConfiguration dc = new DBConfiguration();
		dc.name = name;
		dc.driverClassName = propertiesHandler.getProperty(name + PropertiesKeys.DRIVER);
		dc.dialect = propertiesHandler.getProperty(name + PropertiesKeys.DIALECT);
		dc.url = propertiesHandler.getProperty(name + PropertiesKeys.URL);
		dc.username = propertiesHandler.getProperty(name + PropertiesKeys.USER);
		dc.password = propertiesHandler.getProperty(name + PropertiesKeys.PWD);
		dc.initialSize = Integer.parseInt(propertiesHandler.getProperty(name + PropertiesKeys.POOL_INITIAL_SIZE));
		String minPoolSize = propertiesHandler.getProperty(name + PropertiesKeys.POOL_MIN_SIZE);
		if (minPoolSize == null) {
			minPoolSize = "1";
		}
		dc.minIdle = Integer.parseInt(minPoolSize);
		dc.maxActive = Integer.parseInt(propertiesHandler.getProperty(name + PropertiesKeys.POOL_MAX_SIZE));
		dc.testWhileIdle = Boolean.toString(true).equalsIgnoreCase(propertiesHandler.getProperty(name + PropertiesKeys.POOL_TEST_WHILE_IDLE));
		dc.validationQuery = propertiesHandler.getProperty(name + PropertiesKeys.POOL_VALIDATION_QUERY);
		return dc;
	}

	public String getDialect() {
		return dialect;
	}

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}
}
