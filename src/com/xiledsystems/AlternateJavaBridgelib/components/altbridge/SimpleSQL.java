package com.xiledsystems.AlternateJavaBridgelib.components.altbridge;

import java.lang.reflect.Array;
import java.util.ArrayList;

import android.R.color;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.IsolatedContext;
import android.util.Log;

public class SimpleSQL extends AndroidNonvisibleComponent implements OnDestroySvcListener, OnResumeListener, 
				OnStopListener {

	private String DATABASE_NAME;
	private int DATABASE_VERSION = 1;
	private static String TABLE_NAME = "table1";
	private String COLUMN_ID = "_id";
	private String COLUMN_NAME = "column1";
	private String DATABASE_CREATE; 
	private SQLiteDatabase db;
	private BigDBSqlOpenHelper dbHelper;
	private DBBuilder builder;
	private String[] whereArgs;
	private boolean inService;
			
	@Override
	public void onStop() {
		db.close();
		db = null;
	}

	@Override
	public void onResume() {
		if (db == null) {
			db = dbHelper.getWritableDatabase();
		}
		
	}
	
	public SimpleSQL(Form form, DBBuilder builder) {
		super(form);
		DATABASE_NAME = form.getApplication().getPackageName() + "-SQL.db";		
		this.builder = builder;
		if (builder.Version() != 1) {
			DATABASE_VERSION = builder.Version();
		}
		if (builder.DBName() != null) {
			DATABASE_NAME = builder.DBName();
		}
		form.registerForOnResume(this);
		form.registerForOnStop(this);
		dbHelper = new BigDBSqlOpenHelper(form.$context());			
		db = dbHelper.getWritableDatabase();	
		
	}
	
	public SimpleSQL(FormService formservice, DBBuilder builder) {
		super(formservice);
		DATABASE_NAME = formservice.getApplicationContext().getPackageName() + "-SQL.db";
		this.builder = builder;
		if (builder.Version() != 1) {
			DATABASE_VERSION = builder.Version();
		}
		if (builder.DBName() != null) {
			DATABASE_NAME = builder.DBName();
		}
		dbHelper = new BigDBSqlOpenHelper(formservice.$context());
		db = dbHelper.getWritableDatabase();	
		inService = true;
	}
	
	private void genDatabaseCreateStmt(int table, int cnt, DBBuilder builder) {
		
		if (cnt==1) {
			DATABASE_CREATE = " create table if not exists " + TABLE_NAME + " (" + COLUMN_ID + 
				" integer primary key autoincrement, " + builder.Column(table)[0] +
				" "+builder.DataTypes(table)[0];
			int cols = builder.ColumnCount(TABLE_NAME);
			for (int i = 1; i < cols; i++) {
				DATABASE_CREATE = DATABASE_CREATE + ", " + builder.Column(table)[i] + " "+builder.DataTypes(table)[i];
			}
			DATABASE_CREATE = DATABASE_CREATE + ")";
		} else {
			DATABASE_CREATE = " create table if not exists " + TABLE_NAME + " (" + COLUMN_ID + 
					" integer primary key autoincrement, " + builder.Column(table)[0] +
					" "+builder.DataTypes(table)[0];
			int cols = builder.ColumnCount(TABLE_NAME);
			for (int i = 1; i < cols; i++) {
				DATABASE_CREATE = DATABASE_CREATE + ", " + builder.Column(table)[i] + " "+builder.DataTypes(table)[i];
			}
			DATABASE_CREATE = DATABASE_CREATE + ")";			
		}
	}
	
	public String getDBPath() {
		return db.getPath();
	}
	
				
	/**
	 * This is for advanced users! This method returns a Cursor
	 * object, which you'll have to deal with. Make sure it is
	 * closed when you are done, or you'll run into memory leaks.
	 * @param queryString
	 * @return the cursor object with the data returned
	 */
	public Cursor AdvancedQuery(String queryString, String[] selectionArgs) {
		return db.rawQuery(queryString, selectionArgs);
	}
	
	/**
	 * Insert data into the database in the specified table
	 * and column name. The method returns the rowId resulting
	 * from the add to the database.
	 * @param table - The table to add data to
	 * @param columnName - The column to add data into
	 * @param data - The data to put into the database
	 * @return returns a long of the rowId
	 */
	
	public long Insert(String table, String... items) {
		int amt = items.length;
		int tableid = builder.TableId(table);
		ContentValues values = new ContentValues();
		for (int i = 0; i < amt; i++) {
			values.put(builder.Column(tableid)[i], items[i]);
		}
		try {
			long rowId = db.insert(table, null, values);
			return 	rowId;
		} catch (SQLException e) {
			Log.e("SimpleSQL", "Unable to insert data. Either the table doesn't exist, or incorrect amount of data items." + table);
			e.printStackTrace();
			return -1;
		}		
	}
	
	public boolean ClearTable(String table) {
		if (builder.removeTable(table)) {
			db.execSQL("DROP TABLE IF EXISTS "+table);
			db.close();
			db = null;
			db = dbHelper.getWritableDatabase();
			return true;
		} else {
			return false;
		}
	}
			
	public void Update(String table, String column, long rowid, String data) {
		
		ContentValues values = new ContentValues();
		values.put(column, data);
		String where = "_id=?";
		String[] whereArgs = { String.valueOf(rowid) } ;
		db.update(table, values, where, whereArgs);		
	}
	
	public ArrayList<ArrayList<String>> Query(String table, String whereStatement, String... ColumnsToReturn) {
		
		
		ArrayList<ArrayList<String>> biglist = new ArrayList<ArrayList<String>>();
		ArrayList<String> smallist = new ArrayList<String>();
		if (ColumnsToReturn == null || ColumnsToReturn.length < 1) {
			int tble = builder.TableId(table);
			ColumnsToReturn = builder.Column(tble).clone();
		} 
			
		whereStatement = parseWhereStmt(whereStatement);		
		Class<?> clazz = inService ? formservice.getClass() : form.getClass();
		synchronized (clazz) {
			Cursor cursor = db.query(table, ColumnsToReturn, whereStatement, whereArgs, null, null, null);
			if (cursor.moveToFirst()) {
				int length = cursor.getColumnCount();
				do {
					smallist.clear();
					for (int i = 0; i < length; i++) {
						smallist.add(cursor.getString(i));
					}
					biglist.add(new ArrayList<String>(smallist));
					
				} while (cursor.moveToNext());
			}
		}				
		return biglist;
	}
	
	private String parseWhereStmt(String whereStatement) {
		// parse the where statement, and pass back the modified
		// where statement
		String firstcol="";
		String secondcol="";
		if (whereStatement.contains("AND")) {			
			String andFirst = whereStatement.split("AND")[0];
			String andSecond = whereStatement.split("AND")[1];
			whereArgs = new String[2];
			firstcol = processFirstSecond(andFirst, andSecond)[0];
			secondcol = processFirstSecond(andFirst, andSecond)[1];			
			whereStatement = firstcol + " AND " + secondcol;
		} else if (whereStatement.contains("OR")) {
			String andFirst = whereStatement.split("OR")[0];
			String andSecond = whereStatement.split("OR")[1];
			whereArgs = new String[2];
			firstcol = processFirstSecond(andFirst, andSecond)[0];
			secondcol = processFirstSecond(andFirst, andSecond)[1];			
			whereStatement = firstcol + " OR " + secondcol;
		} else {
			whereStatement = processOne(whereStatement);
		}
		return whereStatement;
	}
	
	public String processOne(String stmt) {
		String firstcol="";
		whereArgs = new String[1];
		if (stmt.contains("=")) {
			firstcol = stmt.split("=")[0]+"=?";
			whereArgs[0] = stmt.split("=")[1];				
		}
		if (stmt.contains("!=")) {
			firstcol = stmt.split("!=")[0]+"!=?";
			whereArgs[0] = stmt.split("!=")[1];				
		}
		if (stmt.contains("<")) {
			firstcol = stmt.split("\\<")[0]+"<?";
			whereArgs[0] = stmt.split("\\<")[1];				
		}
		if (stmt.contains("<=")) {
			firstcol = stmt.split("\\<=")[0]+"<=?";
			whereArgs[0] = stmt.split("\\<=")[1];				
		}
		if (stmt.contains(">")) {
			firstcol = stmt.split("\\>")[0]+">?";
			whereArgs[0] = stmt.split("\\>")[1];				
		}
		if (stmt.contains(">=")) {
			firstcol = stmt.split("\\>=")[0]+">=?";
			whereArgs[0] = stmt.split("\\>=")[1];				
		}
		if (stmt.contains("<>")) {
			firstcol = stmt.split("\\<\\>")[0]+"<>?";
			whereArgs[0] = stmt.split("\\<\\>")[1];				
		}
		return firstcol;
	}
	
	public String[] processFirstSecond(String andFirst, String andSecond) {
		String firstcol="";
		String secondcol="";
		if (andFirst.contains("=")) {
			firstcol = andFirst.split("=")[0]+"=?";
			whereArgs[0] = andFirst.split("=")[1];				
		}
		if (andSecond.contains("=")) {
			secondcol = andSecond.split("=")[0]+"=?";
			whereArgs[1] = andSecond.split("=")[1];
		}
		if (andFirst.contains("!=")) {
			firstcol = andFirst.split("!=")[0]+"!=?";
			whereArgs[0] = andFirst.split("!=")[1];				
		}
		if (andSecond.contains("!=")) {
			secondcol = andSecond.split("!=")[0]+"!=?";
			whereArgs[1] = andSecond.split("!=")[1];
		}
		if (andFirst.contains("<")) {
			firstcol = andFirst.split("\\<")[0]+"<?";
			whereArgs[0] = andFirst.split("\\<")[1];				
		}
		if (andSecond.contains("<")) {
			secondcol = andSecond.split("\\<")[0]+"<?";
			whereArgs[1] = andSecond.split("\\<")[1];
		}
		if (andFirst.contains("<=")) {
			firstcol = andFirst.split("\\<=")[0]+"<=?";
			whereArgs[0] = andFirst.split("\\<=")[1];				
		}
		if (andSecond.contains("<=")) {
			secondcol = andSecond.split("\\<=")[0]+"<=?";
			whereArgs[1] = andSecond.split("\\<=")[1];
		}
		if (andFirst.contains(">")) {
			firstcol = andFirst.split("\\>")[0]+">?";
			whereArgs[0] = andFirst.split("\\>")[1];				
		}
		if (andSecond.contains(">")) {
			secondcol = andSecond.split("\\>")[0]+">?";
			whereArgs[1] = andSecond.split("\\>")[1];
		}
		if (andFirst.contains(">=")) {
			firstcol = andFirst.split("\\>=")[0]+">=?";
			whereArgs[0] = andFirst.split("\\>=")[1];				
		}
		if (andSecond.contains(">=")) {
			secondcol = andSecond.split("\\>=")[0]+">=?";
			whereArgs[1] = andSecond.split("\\>=")[1];
		}
		if (andFirst.contains("<>")) {
			firstcol = andFirst.split("\\<\\>")[0]+"<>?";
			whereArgs[0] = andFirst.split("\\<\\>")[1];				
		}
		if (andSecond.contains("<>")) {
			secondcol = andSecond.split("\\<\\>")[0]+"<>?";
			whereArgs[1] = andSecond.split("\\<\\>")[1];
		}
		return new String[] { firstcol, secondcol };
	}

		
	public ArrayList<ArrayList<String>> Query3(String table, String[] columns, String selection, 
			String[] selectionArgs, String groupBy, String having, String orderBy) {
		ArrayList<ArrayList<String>> biglist = new ArrayList<ArrayList<String>>();
		ArrayList<String> list = new ArrayList<String>();
		
		Class<?> clazz = inService ? formservice.getClass() : form.getClass();
		synchronized (clazz) {
			Cursor cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
			int cols = cursor.getColumnCount();			
			if (cursor.moveToFirst()) {
				int tableid = builder.TableId(table);
				do {				
					list.clear();
					for (int i = 0; i < cols; i++) {
						if (columns == null) {
							if (i==0) {
								
							} else {
								if (builder.DataTypes(tableid)[i-1].equalsIgnoreCase("integer")) {
									list.add(String.valueOf(cursor.getInt(i)));
								} else if (builder.DataTypes(tableid)[i-1].equalsIgnoreCase("real")) {
									list.add(String.valueOf(cursor.getLong(i)));
								} else {
									list.add(cursor.getString(i));
								}
							}
						} else {
							if (builder.DataTypes(tableid)[i].equalsIgnoreCase("integer")) {
								list.add(String.valueOf(cursor.getInt(i)));
							} else if (builder.DataTypes(tableid)[i].equalsIgnoreCase("real")) {
								list.add(String.valueOf(cursor.getLong(i)));
							} else {
								list.add(cursor.getString(i));
							}
						}
					}
					biglist.add(new ArrayList<String>(list));
				} while (cursor.moveToNext());
			}
		}		
		return biglist;
	}
	
	public void RemoveRow(String table, long id) {
		db.delete(table, "_id=?", new String[] { String.valueOf(id) });
	}
	
	/**
	 * Helper method to see if some data is actually in the
	 * table provided.
	 * @param table Table to check data in
	 * @param data The data to check
	 * @return true if the data is in the database, false if not
	 */
	
	public boolean InTable(String table, Object data) {		
		String querystring = "select * from " + table;		
		Class<?> clazz = inService ? formservice.getClass() : form.getClass();
		synchronized (clazz) {
			Cursor cursor = db.rawQuery(querystring, null);
			int length = cursor.getColumnCount();			
			if (cursor.moveToFirst()) {		
				int tableid = builder.TableId(table);
				do {				
					for (int i = 0; i < length; i++) {
						if (i==0) {
							
						} else {
							if (builder.DataTypes(tableid)[i-1].equalsIgnoreCase("integer")) {
								if (((Integer) cursor.getInt(i)).equals(data)) {
									return true;
								}
							} else if (builder.DataTypes(tableid)[i-1].equalsIgnoreCase("real")) {
								if (((Long) cursor.getLong(i)).equals(data)) {
									return true;
								}
							} else {
								if (cursor.getString(i).equals(data)) {
									return true;
								}	
							}
						}
					}				
				} while (cursor.moveToNext());
			}
		}				
		return false;		
	}
	
	public int GetRowCount(String table) {
		Cursor cursor = db.rawQuery("select * from "+ table, null);
		return cursor.getCount();
	}
	
	public ArrayList<String> GetRow(String table, long rowId) {
		Class<?> clazz = inService ? formservice.getClass() : form.getClass();
		ArrayList<String> list = new ArrayList<String>();
		synchronized (clazz) {
			Cursor cursor = db.query(table, null, COLUMN_ID + "=" + rowId, null, null, null, null);			
			if (cursor.moveToFirst()) {
				int tableid = builder.TableId(table);
				int count = cursor.getColumnCount();
				for (int i = 0; i < count; i++) {
					if (i==0) {						
					} else {
						if (builder.DataTypes(tableid)[i-1].equalsIgnoreCase("integer")) {
							list.add(String.valueOf(cursor.getInt(i)));
						} else if (builder.DataTypes(tableid)[i-1].equalsIgnoreCase("real")) {
							list.add(String.valueOf(cursor.getLong(i)));
						} else {
							list.add(cursor.getString(i));
						}
					}
				}			
			}
		}
		
		return list;		
	}
	
	public long GetRowID(String table, String column, String data) {
		Class<?> clazz = inService ? formservice.getClass() : form.getClass();
		synchronized (clazz) {
			Cursor cursor = db.rawQuery("select rowid,* from "+ table, null);
			if (cursor.getCount() > 0) {
				String[] cols = cursor.getColumnNames();
				int num = cols.length;
				int colnum=0;
				for (int i = 0; i <	num; i++) {
					if (cols[i].equals(column)) {
						colnum = i;
						break;
					}
				}
				if (colnum==0) {
					cursor.close();
					return -1;
				}	
				int rows = cursor.getCount();
				if (cursor.moveToFirst()) {
				
					do {
						for (int i = 0; i < rows; i++) {
							if (cursor.getString(colnum).equals(data)) {
								return cursor.getLong(0);
							}
						}
					} while (cursor.moveToNext());			
				}
			}
			cursor.close();			
		}
		return -1;
	}
	
	public String GetValue(String table, String columnName, long id) {
		Class<?> clazz = inService ? formservice.getClass() : form.getClass();
		String rtn;
		synchronized (clazz) {
			String[] column = { COLUMN_ID, columnName };
			Cursor cursor = db.query(table, column, COLUMN_ID + "=" + id, null, null, null, null);
			cursor.moveToFirst();		
			int colid = builder.ColumnPosition(table, columnName);
			int tableid = builder.TableId(table);
			
			if (builder.DataTypes(tableid)[colid].equalsIgnoreCase("integer")) {
				rtn = String.valueOf(cursor.getInt(1));
			} else if (builder.DataTypes(tableid)[colid].equalsIgnoreCase("real")) {
				rtn = String.valueOf(cursor.getLong(1));
			} else {
				rtn = cursor.getString(1);
			}
		}		
		return rtn;
	}
		
			
	private class BigDBSqlOpenHelper extends SQLiteOpenHelper {

		
		
		public BigDBSqlOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			if (builder == null) {
				throw new RuntimeException("Table name list is empty! Can't create database.");
			}			
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {			
			int cnt = builder.TableCount();
			for (int i = 0; i < cnt; i ++) {
				TABLE_NAME = builder.Table(i);
				COLUMN_NAME = builder.Column(i)[0];
				genDatabaseCreateStmt(i, cnt, builder);
				db.execSQL(DATABASE_CREATE);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(BigDBSqlOpenHelper.class.getName(),
					"Upgrading database from version " + oldVersion + " to "
							+ newVersion + ", which will destroy all old data");
			int tblcnt = builder.TableCount();
			for (int i = 0; i < tblcnt; i++) {
				db.execSQL("DROP TABLE IF EXISTS " + builder.Table(i));
			}
			onCreate(db);			
		}		
	}


	@Override
	public void onDestroy() {
		db.close();		
	}
	
}
