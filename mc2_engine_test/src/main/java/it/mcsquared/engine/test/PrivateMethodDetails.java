package it.mcsquared.engine.test;

import java.util.ArrayList;
import java.util.List;

public class PrivateMethodDetails {
	private String name;
	private Class<?> clazz;
	private List<PrivateMethodParam> params = new ArrayList<PrivateMethodParam>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<PrivateMethodParam> getParams() {
		return params;
	}

	public void addParam(Class<?> paramClass, Object value) {
		this.params.add(new PrivateMethodParam(paramClass, value));
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public static class PrivateMethodParam {
		private Class<?> clazz;
		private Object value;

		public PrivateMethodParam(Class<?> clazz, Object value) {
			super();
			this.clazz = clazz;
			this.value = value;
		}

		public Class<?> getClazz() {
			return clazz;
		}

		public void setClazz(Class<?> clazz) {
			this.clazz = clazz;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}
	}
}