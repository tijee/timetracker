package com.thomasgallinari.timetracker.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.thomasgallinari.timetracker.R;
import com.thomasgallinari.timetracker.domain.Task;
import com.thomasgallinari.timetracker.domain.TimeTable;
import com.thomasgallinari.timetracker.util.DateUtils;

public class TaskHistoryListFragment extends SherlockListFragment {

    class LoadHistoryTask extends AsyncTask<Task, Void, List<TaskHistoryItem>> {

	@Override
	protected List<TaskHistoryItem> doInBackground(Task... params) {
	    Task task = params[0];
	    ArrayList<TaskHistoryItem> items = new ArrayList<TaskHistoryItem>();
	    TaskHistoryItem item;
	    Calendar currentHeaderDay = null;
	    for (TimeTable timeTable : task.timeTables) {
		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		boolean running = task.running
			&& timeTable == task.timeTables.get(0);
		start.setTimeInMillis(timeTable.start);
		end.setTimeInMillis(running ? new Date().getTime()
			: timeTable.end);
		if (DateUtils.isSameDay(start.getTime(), end.getTime())) {
		    if (currentHeaderDay == null
			    || !DateUtils.isSameDay(start.getTime(),
				    currentHeaderDay.getTime())) {
			item = new TaskHistoryItem();
			item.start = start.getTimeInMillis();
			item.header = true;
			items.add(item);
			currentHeaderDay = start;
		    }
		    item = new TaskHistoryItem();
		    item.start = start.getTimeInMillis();
		    item.end = end.getTimeInMillis();
		    item.running = running;
		    items.add(item);
		} else {
		    Calendar currentDay = end;
		    Calendar dayStart;
		    Date previousDay;
		    boolean lastDay = true;
		    do {
			previousDay = DateUtils.previousDay(currentDay
				.getTime());
			dayStart = Calendar.getInstance();
			dayStart.setTime(previousDay);
			dayStart.add(Calendar.MILLISECOND, 1);
			if (currentHeaderDay == null
				|| !DateUtils.isSameDay(dayStart.getTime(),
					currentHeaderDay.getTime())) {
			    item = new TaskHistoryItem();
			    item.start = dayStart.getTimeInMillis();
			    item.header = true;
			    items.add(item);
			    currentHeaderDay = dayStart;
			}
			item = new TaskHistoryItem();
			item.start = dayStart.getTimeInMillis();
			item.end = currentDay.getTimeInMillis();
			item.running = running && lastDay;
			items.add(item);
			currentDay.setTime(previousDay);
			lastDay = false;
		    } while (!DateUtils.isSameDay(currentDay.getTime(),
			    start.getTime()));
		    if (currentDay.getTimeInMillis() - start.getTimeInMillis() > 0) {
			if (currentHeaderDay == null
				|| !DateUtils.isSameDay(start.getTime(),
					currentHeaderDay.getTime())) {
			    item = new TaskHistoryItem();
			    item.start = start.getTimeInMillis();
			    item.header = true;
			    items.add(item);
			    currentHeaderDay = start;
			}
			item = new TaskHistoryItem();
			item.start = start.getTimeInMillis();
			item.end = currentDay.getTimeInMillis();
			items.add(item);
		    }
		}
	    }
	    return items;
	}

	@Override
	protected void onPostExecute(List<TaskHistoryItem> result) {
	    history.clear();
	    history.addAll(result);
	    historyAdapter.notifyDataSetChanged();
	}
    }

    class TaskHistoryAdapter extends ArrayAdapter<TaskHistoryItem> {

	private static final String TAG_HEADER = "header";
	private static final String TAG_ITEM = "item";

	public TaskHistoryAdapter(Context context, List<TaskHistoryItem> history) {
	    super(context, 0, history);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    final TaskHistoryItem historyItem = getItem(position);
	    View view = null;
	    if (historyItem.header) {
		if (convertView != null
			&& convertView.getTag().equals(TAG_HEADER)) {
		    view = convertView;
		} else {
		    view = ((LayoutInflater) getContext().getSystemService(
			    Context.LAYOUT_INFLATER_SERVICE)).inflate(
			    R.layout.list_header, null);
		    view.setTag(TAG_HEADER);
		}
	    } else {
		if (convertView != null
			&& convertView.getTag().equals(TAG_ITEM)) {
		    view = convertView;
		} else {
		    view = ((LayoutInflater) getContext().getSystemService(
			    Context.LAYOUT_INFLATER_SERVICE)).inflate(
			    R.layout.task_history_item, null);
		    view.setTag(TAG_ITEM);
		}
	    }
	    if (historyItem != null) {
		if (historyItem.header) {
		    TextView headerView = (TextView) view
			    .findViewById(R.id.header);
		    Date today = new Date();
		    Date itemDate = new Date(historyItem.start);
		    if (DateUtils.isSameDay(itemDate, today)) {
			headerView.setText(R.string.today);
		    } else if (DateUtils.isSameDay(itemDate,
			    DateUtils.previousDay(today))) {
			headerView.setText(R.string.yesterday);
		    } else {
			headerView
				.setText(android.text.format.DateUtils
					.formatDateTime(
						getContext(),
						historyItem.start,
						android.text.format.DateUtils.FORMAT_SHOW_YEAR));
		    }
		} else {
		    TextView durationView = (TextView) view
			    .findViewById(R.id.task_history_duration);
		    TextView rangeView = (TextView) view
			    .findViewById(R.id.task_history_range);
		    durationView.setText(DateUtils
			    .formatElapsedTime(historyItem.getDuration()));
		    rangeView
			    .setText(android.text.format.DateUtils
				    .formatDateRange(
					    getContext(),
					    historyItem.start,
					    historyItem.running ? new Date()
						    .getTime()
						    : historyItem.end,
					    android.text.format.DateUtils.FORMAT_SHOW_TIME));
		    view.setBackgroundColor(getResources().getColor(
			    historyItem.running ? R.color.tertiary
				    : android.R.color.transparent));
		}
	    }
	    return view;
	}

	@Override
	public boolean isEnabled(int position) {
	    return false;
	}
    }

    class TaskHistoryItem {

	public long start;
	public long end;
	public boolean running;
	public boolean header;

	public long getDuration() {
	    return (running ? new Date().getTime() : end) - start;
	}
    }

    public static final String ARG_TASK = "task";

    private Task task;
    private ArrayList<TaskHistoryItem> history;
    private ArrayAdapter<TaskHistoryItem> historyAdapter;
    private Handler timer;
    private boolean runTimer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	task = (Task) getArguments().getSerializable(ARG_TASK);

	history = new ArrayList<TaskHistoryItem>();
	historyAdapter = new TaskHistoryAdapter(getActivity(), history);

	timer = new Handler();

	new LoadHistoryTask().execute(task);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
	View v = inflater.inflate(R.layout.fragment_task_history, container,
		false);
	setListAdapter(historyAdapter);
	return v;
    }

    @Override
    public void onPause() {
	super.onPause();
	stopTimer();
    }

    @Override
    public void onResume() {
	super.onResume();
	if (task != null && task.running) {
	    startTimer();
	}
    }

    public void refresh(Task task) {
	this.task = task;
	if (task != null && task.running) {
	    startTimer();
	} else {
	    stopTimer();
	}
	new LoadHistoryTask().execute(task);
    }

    private void startTimer() {
	runTimer = true;
	timer.post(new Runnable() {

	    @Override
	    public void run() {
		historyAdapter.notifyDataSetChanged();
		if (runTimer) {
		    timer.postDelayed(this, 1000);
		}
	    }
	});
    }

    private void stopTimer() {
	runTimer = false;
    }
}
