package it.mcsquared.engine.manager.database;

import java.sql.SQLException;

public class NoSingleResultException extends SQLException {

    private static final long serialVersionUID = 5966278324584411823L;

    public NoSingleResultException(String msg) {
        super(msg);
    }

}
