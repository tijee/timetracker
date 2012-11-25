package com.thomasgallinari.timetracker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DbOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "simpletimetracker.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_TABLE_TASK = "CREATE TABLE "
	    + TaskDAO.TABLE_NAME + " (" + BaseColumns._ID
	    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TaskDAO.KEY_NAME
	    + " TEXT, " + TaskDAO.KEY_PROJECT + " TEXT, "
	    + TaskDAO.KEY_CREATION_DATE + " INTEGER, " + TaskDAO.KEY_RUNNING
	    + " INTEGER);";
    private static final String CREATE_TABLE_TIME_TABLE = "CREATE TABLE "
	    + TimeTableDAO.TABLE_NAME + " (" + BaseColumns._ID
	    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TimeTableDAO.KEY_START
	    + " INTEGER, " + TimeTableDAO.KEY_END + " INTEGER, "
	    + TimeTableDAO.KEY_TASK_ID + " INTEGER);";

    public DbOpenHelper(Context context) {
	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
	db.execSQL(CREATE_TABLE_TASK);
	db.execSQL(CREATE_TABLE_TIME_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	// nothing for now
    }
}
