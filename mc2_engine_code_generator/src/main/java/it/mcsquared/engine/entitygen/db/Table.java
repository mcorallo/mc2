package it.mcsquared.engine.entitygen.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import it.mcsquared.engine.entitygen.Generator;
import it.mcsquared.engine.manager.database.Record;

public class Table implements Serializable {

	private static final long serialVersionUID = -4251078502829707832L;

	private String name;
	private String database;
	private List<Column> columns;

	private String packageName;
	private List<String> imports = new ArrayList<>();
	private String className;
	private List<String[]> fields = new ArrayList<>();

	private StringBuilder sb;

	public Table(String packageName) {
		this.packageName = packageName;
	}

	public void generate() {
		try {
			Random random = new Random();
			className = Generator.capitalize(name, false);

			imports.add(Record.class.getCanonicalName());
			imports.add(Serializable.class.getCanonicalName());
			imports.add(it.mcsquared.engine.manager.database.annotation.Column.class.getCanonicalName());
			imports.add(it.mcsquared.engine.manager.database.annotation.Table.class.getCanonicalName());

			sb = new StringBuilder();
			sb.append("package ").append(packageName).append(";\n");
			sb.append("_IMPORTS_");
			sb.append("\n");
			sb.append("\n@Table(name=\"" + name + "\")\n");
			sb.append("public class ").append(className).append("  implements Serializable {\n\n");
			sb.append("\tprivate static final long serialVersionUID = ").append(random.nextLong()).append("L;\n\n");

			// fields
			String fieldName;
			String fieldType;
			String nullValue;
			for (Column c : columns) {

				if (!c.getName().endsWith("_id")) {
					nullValue = null;
					fieldType = c.getTypeString();
					fieldName = Generator.capitalize(c.getName(), true);
				} else {
					nullValue = "null";
					fieldType = c.getName();
					fieldType = Generator.capitalize(fieldType.substring(0, fieldType.lastIndexOf('_')), false);
					fieldName = c.getName();
					fieldName = Generator.capitalize(fieldName.substring(0, fieldName.lastIndexOf('_')), true);
				}
				sb.append("\n@Column(name=\"" + c.getName() + "\")\n");
				sb.append(c.getAccess()).append(" ").append(fieldType).append(" ").append(fieldName).append(";\n");
				fields.add(new String[] {
						fieldName,
						c.getName(),
						fieldType,
						nullValue });

			}
			sb.append("\n\n");
			for (String[] f : fields) {
				// geatter
				fieldName = f[0];
				fieldType = f[2];
				sb.append("\tpublic ").append(fieldType).append(" get").append(Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1)).append("(){\n");
				sb.append("\t\treturn ").append(fieldName).append(";\n");
				sb.append("\t}\n");
				sb.append("\n");
				sb.append("\tpublic void set").append(Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1)).append("(").append(fieldType).append(" ").append(fieldName).append("){\n");
				sb.append("\t\tthis.").append(fieldName).append("=").append(fieldName).append(";\n");
				sb.append("\t}\n");
				sb.append("\n");
			}

			setNewInstance();
			setToString();
			setSelectQuery();
			setInsertQuery();
			setUpdateQuery();
			// end class
			sb.append("}\n");
			String imps = "";
			for (String i : imports) {
				String item = "import " + i + ";\n";
				if (!imps.contains(item)) {
					imps += item;
				}
			}
			imps = sb.toString().replace("_IMPORTS_", imps);
			sb = new StringBuilder();
			sb.append(imps);
		} catch (Exception e) {
			System.out.println(this);
			throw e;
		}
	}

	@Override
	public String toString() {
		return sb.toString();
	}

	private void setToString() {

		sb.append("\t@Override\n");
		sb.append("\tpublic String toString() {\n");
		sb.append("\t\tStringBuilder builder = new StringBuilder();\n");
		sb.append("\t\tbuilder.append(\"").append(className).append(" [\");\n");
		String value;
		for (String[] s : fields) {

			if (s.length > 3 && "null".equals(s[3])) {
				value = "(" + s[0] + " != null ? " + s[0] + ".getId() : null)";
			} else {
				value = s[0];
			}
			sb.append("\t\tbuilder.append(\"").append(s[0]).append("=\").append(").append(value).append(").append(\", \");\n");
		}
		sb.append("\t\tbuilder.append(\"]\");\n");
		sb.append("\t\treturn builder.toString();\n");
		sb.append("\t\t}\n");
	}

	private void setNewInstance() {
		sb.append("\tpublic static ").append(className).append(" getFromDb(Record r){\n");
		sb.append("\t\t").append("if (r == null){return null;}\n");
		sb.append("\t\t").append(className).append(" instance = new ").append(className).append("();\n");
		for (String[] f : fields) {
			sb.append("\t\tinstance.").append(f[0]).append(" = ");
			if (f[3] == null) {
				sb.append("((" + f[2] + ")r.getValue(\"").append(f[1].toUpperCase()).append("\"));\n");
			} else {
				sb.append("null; //").append(f[1].toUpperCase()).append("\n");
			}
		}
		sb.append("\t\treturn instance;\n");
		sb.append("\t}\n");
		sb.append("\n");
	}

	private void setInsertQuery() {
		String temp1 = "";
		String temp2 = "";
		String temp3 = "";
		int count = 0;
		for (String[] f : fields) {
			String f1 = f[1];
			if (f1.equals("id")) {
				continue;
			}
			temp1 += f1 + ",";
			temp2 += "?,";
			temp3 += "//params.add(instance." + f[0] + ");\n";
			count++;
		}
		if (!StringUtils.isBlank(temp1)) {
			sb.append("//insert into ").append(name).append(" (");
			temp1 = temp1.substring(0, temp1.length() - 1);
			sb.append(temp1).append(") values (");
			temp2 = temp2.substring(0, temp2.length() - 1);
			sb.append(temp2).append(") -- ").append(count).append("\n");
			sb.append(temp3).append("\n");
		}
	}

	private void setSelectQuery() {
		sb.append("//select * from ").append(name).append("\n");
	}

	private void setUpdateQuery() {
		sb.append("//update ").append(name).append(" set ");
		int count = 0;
		int max = fields.size() - 2;
		for (String[] f : fields) {
			String f1 = f[1];
			if (f1.equals("id")) {
				continue;
			}
			sb.append(f1).append(" = ?");
			if (count < max) {
				sb.append(",");
			}
			count++;
		}
		sb.append(" where id = ? --" + count + "\n");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public List<String> getImports() {
		return imports;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

}
