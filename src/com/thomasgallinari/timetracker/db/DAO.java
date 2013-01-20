package com.thomasgallinari.timetracker.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.thomasgallinari.timetracker.App;
import com.thomasgallinari.timetracker.domain.DomainObject;

public abstract class DAO<T extends DomainObject> {

    protected static final int INDEX_ID = 0;

    protected App app;

    public DAO(App app) {
	this.app = app;
    }

    public void delete(T object) {
	app.getDb().delete(getTableName(), BaseColumns._ID + " = ?",
		new String[] { String.valueOf(object.id) });
    }

    public List<T> getAll() {
	Cursor cursor = app.getDb().query(getTableName(), null, null, null,
		null, null, null);
	ArrayList<T> objects = new ArrayList<T>();
	cursor.moveToFirst();
	while (!cursor.isAfterLast()) {
	    objects.add(objectFromCursor(cursor));
	    cursor.moveToNext();
	}
	return objects;
    }

    public T insert(T object) {
	object.id = app.getDb().insert(getTableName(), null,
		valuesFromObject(object));
	return object;
    }

    public T update(T object) {
	app.getDb().update(getTableName(), valuesFromObject(object),
		BaseColumns._ID + " = ?",
		new String[] { String.valueOf(object.id) });
	return object;
    }

    protected abstract String getTableName();

    protected abstract T objectFromCursor(Cursor cursor);

    protected abstract ContentValues valuesFromObject(T object);
}
