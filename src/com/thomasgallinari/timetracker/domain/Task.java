package com.thomasgallinari.timetracker.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Task extends DomainObject implements Comparable<Task> {

    private static final long serialVersionUID = -154268298546389712L;

    public String name;
    public String project;
    public long creationDate;
    public boolean running;
    public List<TimeTable> timeTables;

    public Task() {
	creationDate = new Date().getTime();
	timeTables = new ArrayList<TimeTable>();
    }

    @Override
    public int compareTo(Task another) {
	return (int) (creationDate - another.creationDate);
    }

    public long getStartTimeIfRunning() {
	long time = 0;
	for (TimeTable timeTable : timeTables) {
	    if (timeTable.end == 0) {
		time = timeTable.start;
		break;
	    }
	}
	return time;
    }

    public long getTimeSpent() {
	long time = 0;
	for (TimeTable timeTable : timeTables) {
	    if (timeTable.end > 0) {
		time += (timeTable.end - timeTable.start);
	    }
	}
	return time;
    }
}
