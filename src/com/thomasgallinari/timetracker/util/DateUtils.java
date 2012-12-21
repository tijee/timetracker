package com.thomasgallinari.timetracker.util;

import android.annotation.SuppressLint;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    @SuppressLint("DefaultLocale")
    public static String formatElapsedTime(long millis) {
	long hours = millis / 3600000;
	long minutes = (millis % 3600000) / 60000;
	long seconds = (millis % 60000) / 1000;
	return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    public static boolean isSameDay(Date date1, Date date2) {
	Calendar c1 = Calendar.getInstance();
	Calendar c2 = Calendar.getInstance();
	c1.setTime(date1);
	c2.setTime(date2);
	return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
		&& c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    public static Date previousDay(Date date) {
	Calendar previousDay = Calendar.getInstance();
	previousDay.setTime(date);
	previousDay.set(Calendar.HOUR_OF_DAY, 0);
	previousDay.set(Calendar.MINUTE, 0);
	previousDay.set(Calendar.SECOND, 0);
	previousDay.set(Calendar.MILLISECOND, 0);
	previousDay.add(Calendar.MILLISECOND, -1);
	return previousDay.getTime();
    }
}
