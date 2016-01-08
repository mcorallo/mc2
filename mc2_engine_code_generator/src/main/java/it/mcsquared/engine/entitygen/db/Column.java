package it.mcsquared.engine.entitygen.db;

import java.io.Serializable;

public class Column implements Serializable {

	private static final long serialVersionUID = -5072534239697639945L;

	private String access = "private";
	private String type;
	private String name;
	private int size;

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTypeString() {
		String result = type;
		if (result.contains(".")) {
			result = result.substring(result.lastIndexOf('.') + 1);
		}
		return result;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
}
