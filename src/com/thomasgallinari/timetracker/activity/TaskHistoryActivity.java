package com.thomasgallinari.timetracker.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.thomasgallinari.timetracker.App;
import com.thomasgallinari.timetracker.R;
import com.thomasgallinari.timetracker.domain.Task;
import com.thomasgallinari.timetracker.domain.TimeTable;
import com.thomasgallinari.timetracker.util.DateUtils;

public class TaskHistoryActivity extends SherlockListActivity {

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
		    view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
			    .inflate(R.layout.list_header, null);
		    view.setTag(TAG_HEADER);
		}
	    } else {
		if (convertView != null
			&& convertView.getTag().equals(TAG_ITEM)) {
		    view = convertView;
		} else {
		    view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
			    .inflate(R.layout.task_history_item, null);
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
						TaskHistoryActivity.this,
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
					    TaskHistoryActivity.this,
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

    class ToggleTaskRunningTask extends AsyncTask<Task, Void, Task> {

	@Override
	protected Task doInBackground(Task... params) {
	    return ((App) getApplication()).getTaskDao().toggleRunning(
		    params[0]);
	}

	@Override
	@SuppressLint("NewApi")
	protected void onPostExecute(Task result) {
	    taskChanged = true;
	    task = result;
	    if (task != null && task.running) {
		startTimer();
	    } else {
		stopTimer();
	    }
	    new LoadHistoryTask().execute(task);
	    invalidateOptionsMenu();
	}
    }

    public static final int REQUEST_EDIT_TASK = 0;
    public static final String EXTRA_TASK = "task";
    public static final String EXTRA_PROJECTS = "projects";

    private Task task;
    private ArrayList<String> projects;
    private boolean taskChanged;
    private ArrayList<TaskHistoryItem> history;
    private ArrayAdapter<TaskHistoryItem> historyAdapter;
    private Handler timer;
    private boolean runTimer;

    @Override
    public void onBackPressed() {
	setResult(RESULT_OK, new Intent().putExtra(
		TaskListActivity.EXTRA_TASK_CHANGED, taskChanged));
	finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getSupportMenuInflater();
	inflater.inflate(R.menu.activity_task_history, menu);
	return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
	switch (item.getItemId()) {
	case R.id.menu_edit:
	    ArrayList<String> projectsParam = new ArrayList<String>(
		    projects.subList(Math.min(projects.size(), 1),
			    projects.size()));
	    startActivityForResult(new Intent(this, TaskEditActivity.class)
		    .putExtra(TaskEditActivity.EXTRA_TASK, task)
		    .putStringArrayListExtra(EXTRA_PROJECTS, projectsParam),
		    REQUEST_EDIT_TASK);
	    return true;
	case R.id.menu_start_stop:
	    new ToggleTaskRunningTask().execute(task);
	    return true;
	default:
	    return super.onMenuItemSelected(featureId, item);
	}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case android.R.id.home:
	    onBackPressed();
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
	MenuItem startStopMenuItem = menu.getItem(0);
	if (task.running) {
	    startStopMenuItem.setIcon(R.drawable.ic_menu_pause);
	    startStopMenuItem.setTitle(R.string.stop);
	} else {
	    startStopMenuItem.setIcon(R.drawable.ic_menu_play);
	    startStopMenuItem.setTitle(R.string.start);
	}
	return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	if (resultCode == RESULT_OK) {
	    switch (requestCode) {
	    case REQUEST_EDIT_TASK:
		Task updatedTask = (Task) data.getSerializableExtra(EXTRA_TASK);
		if (!updatedTask.name.equals(task.name)
			|| !updatedTask.project.equals(task.project)) {
		    taskChanged = true;
		}
		task = updatedTask;
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(task.name);
		actionBar.setSubtitle(task.project);
		break;
	    }
	}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_task_history);

	task = (Task) getIntent().getSerializableExtra(EXTRA_TASK);
	projects = getIntent().getStringArrayListExtra(EXTRA_PROJECTS);

	history = new ArrayList<TaskHistoryActivity.TaskHistoryItem>();
	historyAdapter = new TaskHistoryAdapter(this, history);

	ActionBar actionBar = getSupportActionBar();
	actionBar.setDisplayHomeAsUpEnabled(true);
	actionBar.setTitle(task.name);
	actionBar.setSubtitle(task.project);

	setListAdapter(historyAdapter);

	new LoadHistoryTask().execute(task);

	timer = new Handler();
    }

    @Override
    protected void onPause() {
	super.onPause();
	stopTimer();
    }

    @Override
    protected void onResume() {
	super.onResume();
	if (task != null && task.running) {
	    startTimer();
	}
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
