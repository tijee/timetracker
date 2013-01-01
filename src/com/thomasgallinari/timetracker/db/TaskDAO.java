package com.thomasgallinari.timetracker.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

import com.thomasgallinari.timetracker.App;
import com.thomasgallinari.timetracker.domain.Task;
import com.thomasgallinari.timetracker.domain.TimeTable;

public class TaskDAO extends DAO<Task> {

    public static final String TABLE_NAME = "task";

    public static final String KEY_NAME = "name";
    public static final String KEY_PROJECT = "project";
    public static final String KEY_CREATION_DATE = "creation_date";
    public static final String KEY_RUNNING = "running";

    private static final int INDEX_NAME = 1;
    private static final int INDEX_PROJECT = 2;
    private static final int INDEX_CREATION_DATE = 3;
    private static final int INDEX_RUNNING = 4;

    public TaskDAO(App app) {
	super(app);
    }

    public List<Task> getByProject(String project) {
	String where = null;
	ArrayList<String> params = new ArrayList<String>();
	if (project != null) {
	    where = "project = ?";
	    params.add(project);
	}
	Cursor cursor = app.getDb().query(getTableName(), null, where,
		params.toArray(new String[params.size()]), null, null, null);
	ArrayList<Task> tasks = new ArrayList<Task>();
	cursor.moveToFirst();
	while (!cursor.isAfterLast()) {
	    tasks.add(objectFromCursor(cursor));
	    cursor.moveToNext();
	}
	return tasks;
    }

    public List<String> getAllProjects() {
	Cursor cursor = app.getDb().query(true, getTableName(),
		new String[] { KEY_PROJECT }, null, null, null, null,
		KEY_PROJECT, null);
	ArrayList<String> projects = new ArrayList<String>();
	cursor.moveToFirst();
	while (!cursor.isAfterLast()) {
	    projects.add(cursor.getString(0));
	    cursor.moveToNext();
	}
	return projects;
    }

    public Task toggleRunning(Task task) {
	if (task.running) {
	    for (TimeTable timeTable : task.timeTables) {
		if (timeTable.end == 0) {
		    timeTable.end = new Date().getTime();
		    app.getTimeTableDao().update(timeTable);
		    break;
		}
	    }
	} else {
	    TimeTable timeTable = new TimeTable();
	    timeTable.taskId = task.id;
	    task.timeTables.add(0, app.getTimeTableDao().insert(timeTable));
	}
	task.running = !task.running;
	return update(task);
    }

    @Override
    protected String getTableName() {
	return TABLE_NAME;
    }

    @Override
    protected Task objectFromCursor(Cursor cursor) {
	Task task = new Task();
	task.id = cursor.getLong(INDEX_ID);
	task.name = cursor.getString(INDEX_NAME);
	task.project = cursor.getString(INDEX_PROJECT);
	task.creationDate = cursor.getLong(INDEX_CREATION_DATE);
	task.running = cursor.getShort(INDEX_RUNNING) <= 0 ? false : true;

	task.timeTables = app.getTimeTableDao().getByTask(task);
	for (TimeTable timeTable : task.timeTables) {
	    timeTable.task = task;
	}

	return task;
    }

    @Override
    protected ContentValues valuesFromObject(Task task) {
	ContentValues values = new ContentValues();
	values.put(KEY_NAME, task.name);
	values.put(KEY_PROJECT, task.project);
	values.put(KEY_CREATION_DATE, task.creationDate);
	values.put(KEY_RUNNING, task.running ? 1 : 0);
	return values;
    }
}
