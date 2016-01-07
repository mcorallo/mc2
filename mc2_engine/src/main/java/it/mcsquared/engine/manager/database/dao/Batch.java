package it.mcsquared.engine.manager.database.dao;

import it.mcsquared.engine.manager.database.QueryHelper;

import java.sql.SQLException;

public interface Batch {
	public Boolean execute(QueryHelper transactionQueryHelper) throws SQLException;
}