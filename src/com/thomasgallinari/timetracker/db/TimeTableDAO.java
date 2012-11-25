package com.thomasgallinari.timetracker.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

import com.thomasgallinari.timetracker.App;
import com.thomasgallinari.timetracker.domain.Task;
import com.thomasgallinari.timetracker.domain.TimeTable;

public class TimeTableDAO extends DAO<TimeTable> {

    public static final String TABLE_NAME = "time_table";

    public static final String KEY_START = "start";
    public static final String KEY_END = "end";
    public static final String KEY_TASK_ID = "task_id";

    private static final int INDEX_START = 1;
    private static final int INDEX_END = 2;
    private static final int INDEX_TASK_ID = 3;

    public TimeTableDAO(App app) {
	super(app);
    }

    public List<TimeTable> getByTask(Task task) {
	Cursor cursor = app.getDb().query(getTableName(), null,
		KEY_TASK_ID + " = ?", new String[] { String.valueOf(task.id) },
		null, null, KEY_START + " DESC");
	ArrayList<TimeTable> timeTables = new ArrayList<TimeTable>();
	cursor.moveToFirst();
	while (!cursor.isAfterLast()) {
	    timeTables.add(objectFromCursor(cursor));
	    cursor.moveToNext();
	}
	return timeTables;
    }

    @Override
    protected String getTableName() {
	return TABLE_NAME;
    }

    @Override
    protected TimeTable objectFromCursor(Cursor cursor) {
	TimeTable timeTable = new TimeTable();
	timeTable.id = cursor.getLong(INDEX_ID);
	timeTable.start = cursor.getLong(INDEX_START);
	timeTable.end = cursor.getLong(INDEX_END);
	timeTable.taskId = cursor.getLong(INDEX_TASK_ID);
	return timeTable;
    }

    @Override
    protected ContentValues valuesFromObject(TimeTable timeTable) {
	ContentValues values = new ContentValues();
	values.put(KEY_START, timeTable.start);
	values.put(KEY_END, timeTable.end);
	values.put(KEY_TASK_ID, timeTable.taskId);
	return values;
    }
}
