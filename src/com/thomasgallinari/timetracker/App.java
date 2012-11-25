package com.thomasgallinari.timetracker;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.thomasgallinari.timetracker.db.DbOpenHelper;
import com.thomasgallinari.timetracker.db.TaskDAO;
import com.thomasgallinari.timetracker.db.TimeTableDAO;

public class App extends Application {

    private DbOpenHelper dbHelper;
    private SQLiteDatabase db;

    private TaskDAO taskDao;
    private TimeTableDAO timeTableDao;

    public SQLiteDatabase getDb() {
	return db;
    }

    public TaskDAO getTaskDao() {
	return taskDao;
    }

    public TimeTableDAO getTimeTableDao() {
	return timeTableDao;
    }

    @Override
    public void onCreate() {
	super.onCreate();

	dbHelper = new DbOpenHelper(this);
	db = dbHelper.getWritableDatabase();

	taskDao = new TaskDAO(this);
	timeTableDao = new TimeTableDAO(this);
    }

    @Override
    public void onTerminate() {
	db.close();
	dbHelper.close();

	super.onTerminate();
    }
}
