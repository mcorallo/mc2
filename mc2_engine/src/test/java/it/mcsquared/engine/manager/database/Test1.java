package it.mcsquared.engine.manager.database;

import it.mcsquared.engine.manager.database.annotation.Column;
import it.mcsquared.engine.manager.database.annotation.Table;

@Table(name = "test1")
public class Test1 {
	@Column(name = "id")
	private int id;
	@Column(name = "name")
	private String name;

	public static Test1 getFromDb(Record r) {
		if (r == null) {
			return null;
		}
		Test1 instance = new Test1();
		instance.id = ((Integer) r.getValue("ID"));
		instance.name = ((String) r.getValue("NAME"));
		return instance;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}