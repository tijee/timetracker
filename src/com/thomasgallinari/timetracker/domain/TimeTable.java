package com.thomasgallinari.timetracker.domain;

import java.util.Date;

public class TimeTable extends DomainObject implements Comparable<TimeTable> {

    private static final long serialVersionUID = -6411588532776423307L;

    public long start;
    public long end;
    public long taskId;
    public Task task;

    public TimeTable() {
	start = new Date().getTime();
    }

    public long getDuration() {
	return end - start;
    }

    @Override
    public int compareTo(TimeTable another) {
	return (int) (start - another.start);
    }
}
