package com.example.testerapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper{
	public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "EnhancedEreader.db";
    public static final String USER_TABLE_NAME = "USER";
    public static final String USER_ID = "uid";
    public static final String SQL_CREATE_USERS_TABLE = createUsersTable();

    private static String createUsersTable() {
    	String result = "CREATE TABLE " + USER_TABLE_NAME + " (";
		result += USER_ID + " TEXT PRIMARY KEY)";
	    return result;
	}
    
    public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		System.out.println("database oncreate");
		db.execSQL(SQL_CREATE_USERS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {		
	}
    
}
