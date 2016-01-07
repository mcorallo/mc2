package it.mcsquared.engine.manager.database;

import java.sql.SQLException;

public class NoInsertQueryException extends SQLException {

	private static final long serialVersionUID = -2724529129989477537L;

	public NoInsertQueryException(String msg) {
		super(msg);
	}
}
