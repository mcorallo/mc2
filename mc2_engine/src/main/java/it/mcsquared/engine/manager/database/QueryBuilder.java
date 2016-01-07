package it.mcsquared.engine.manager.database;

public class QueryBuilder {

	private StringBuilder builder = new StringBuilder();

	public QueryBuilder addToken(String token) {
		builder.append(" ").append(token);
		return this;
	}

	public String getQuery() {
		String query = builder.toString();
		return query;
	}

	@Override
	public String toString() {
		return builder.toString();
	}

	public void clear() {
		this.builder = new StringBuilder();
	}
}
