package com.thomasgallinari.timetracker.util;

import java.util.Calendar;

public class DateUtils {

    public static boolean isSameDay(Calendar date1, Calendar date2) {
	return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR)
		&& date1.get(Calendar.DAY_OF_YEAR) == date2
			.get(Calendar.DAY_OF_YEAR);
    }

    public static Calendar previousDay(Calendar date) {
	Calendar previousDay = Calendar.getInstance();
	previousDay.setTime(date.getTime());
	previousDay.clear(Calendar.HOUR_OF_DAY);
	previousDay.clear(Calendar.MINUTE);
	previousDay.clear(Calendar.SECOND);
	previousDay.clear(Calendar.MILLISECOND);
	previousDay.add(Calendar.MILLISECOND, -1);
	return previousDay;
    }
}
