package it.mcsquared.engine.manager.database;

import java.util.LinkedHashMap;
import java.util.Map;

public class Record {

    private Map<String, Object> cells = new LinkedHashMap<String, Object>();

    public Map<String, Object> getCells() {
        return cells;
    }

    @Override
    public String toString() {
        return "Record [cells=" + cells + "]";
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String column) {
        return (T) cells.get(column.toUpperCase());
    }

}
