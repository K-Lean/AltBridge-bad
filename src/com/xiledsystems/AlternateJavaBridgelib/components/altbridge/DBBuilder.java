package com.xiledsystems.AlternateJavaBridgelib.components.altbridge;

import java.util.ArrayList;

import com.xiledsystems.AlternateJavaBridgelib.components.altbridge.collect.DoubleList;

public class DBBuilder {

	private ArrayList<String> tables;
	private ArrayList<String> columns;
	private ArrayList<String> datatypes;
	private ArrayList<DoubleList> bigColumns;
	private int dbVersion = 1;
	private String dbName;
	
	public DBBuilder() {
		tables = new ArrayList<String>();
		columns = new ArrayList<String>();
		datatypes = new ArrayList<String>();
		bigColumns = new ArrayList<DoubleList>();
	}
	
	public void addTable(String tableName, String[] columnNames, String[] dataTypes) {
		tables.add(tableName);
		columns.clear();
		datatypes.clear();
		int cols = columnNames.length;
		for (int i = 0; i < cols; i++) {
			columns.add(columnNames[i]);
		}
		cols = dataTypes.length;
		for (int i = 0; i < cols; i++) {
			String type;
			String s = dataTypes[i];
			if (s.equals("float") || s.equals("double")) {
				type = "REAL";
			} else if (s.equals("int") || s.equals("byte") || s.equals("short") || s.equals("long")) {
				type = "INTEGER";
			} else if (s.equalsIgnoreCase("text") || s.equalsIgnoreCase("real") || 
					s.equalsIgnoreCase("integer")) {
				type = s;
			} else {
				type = "TEXT";
			}			
			datatypes.add(type);
		}
		bigColumns.add(new DoubleList(new ArrayList<Object>(columns), new ArrayList<Object>(datatypes)));
		
	}
	
	public boolean removeTable(String table) {
		if (tables.contains(table)) {
			int id = TableId(table);
			tables.remove(id);
			bigColumns.remove(id);
			return true;
		} else {
			return false;
		}
	}
	
	public int Version() {
		return dbVersion;
	}
	
	public String DBName() {
		return dbName;
	}
	
	public void DBName(String name) {
		dbName = name;
	}
	
	public void Version(int version) {
		dbVersion= version;
	}
	
	public int TableCount() {
		return tables.size();
	}
	
	public int TableId(String table) {
		return tables.indexOf(table);
	}
	
	public int ColumnCount(String table) {
		int position = tables.indexOf(table);
		return bigColumns.get(position).size();
	}
	
	public String Table(int position) {
		return tables.get(position);
	}
	
	public int ColumnPosition(String table, String column) {
		int tbl = tables.indexOf(table);
		return bigColumns.get(tbl).getList(1).indexOf(column);
	}
	
	public String[] Column(int position) {
		int size = bigColumns.get(position).size();
		String[] tmp = new String[size];
		for (int i = 0; i < size; i++) {
			tmp[i] = bigColumns.get(position).get(i)[0].toString();
		}
		return tmp;
	}
	
	public String[] DataTypes(int position) {
		int size = bigColumns.get(position).size();
		String[] tmp = new String[size];
		for (int i = 0; i < size; i++) {
			tmp[i] = bigColumns.get(position).get(i)[1].toString();
		}
		return tmp;
	}
	
}
