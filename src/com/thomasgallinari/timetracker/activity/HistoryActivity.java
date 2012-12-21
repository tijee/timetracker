package com.thomasgallinari.timetracker.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.thomasgallinari.timetracker.App;
import com.thomasgallinari.timetracker.R;
import com.thomasgallinari.timetracker.db.TaskDAO;
import com.thomasgallinari.timetracker.db.TimeTableDAO;
import com.thomasgallinari.timetracker.domain.Task;
import com.thomasgallinari.timetracker.domain.TimeTable;
import com.thomasgallinari.timetracker.util.DateUtils;

public class HistoryActivity extends SherlockListActivity {

    class LoadHistoryTask extends AsyncTask<Object, Void, List<HistoryItem>> {

	private TaskDAO taskDao;
	private TimeTableDAO timeTableDao;

	public LoadHistoryTask() {
	    taskDao = ((App) getApplication()).getTaskDao();
	    timeTableDao = ((App) getApplication()).getTimeTableDao();
	}

	@Override
	protected List<HistoryItem> doInBackground(Object... params) {
	    String project = (String) params[0];
	    // TODO Date range
	    ArrayList<HistoryItem> items = new ArrayList<HistoryItem>();
	    HistoryItem item;
	    Calendar currentHeaderDay = null;
	    List<TimeTable> timeTables = new ArrayList<TimeTable>();
	    List<Task> tasks = taskDao.getByProject(project);
	    for (Task task : tasks) {
		List<TimeTable> taskTimeTables = timeTableDao.getByTask(task);
		task.timeTables = taskTimeTables;
		for (TimeTable timeTable : taskTimeTables) {
		    timeTable.task = task;
		}
		timeTables.addAll(taskTimeTables);
	    }
	    Collections.sort(timeTables, Collections.reverseOrder());
	    for (TimeTable timeTable : timeTables) {
		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		boolean running = timeTable.task.running
			&& timeTable == timeTable.task.timeTables.get(0);
		start.setTimeInMillis(timeTable.start);
		end.setTimeInMillis(running ? new Date().getTime()
			: timeTable.end);
		if (DateUtils.isSameDay(start.getTime(), end.getTime())) {
		    if (currentHeaderDay == null
			    || !DateUtils.isSameDay(start.getTime(),
				    currentHeaderDay.getTime())) {
			item = new HistoryItem();
			item.start = start.getTimeInMillis();
			item.header = true;
			items.add(item);
			currentHeaderDay = start;
		    }
		    item = new HistoryItem();
		    item.task = timeTable.task.name;
		    item.project = timeTable.task.project;
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
			    item = new HistoryItem();
			    item.start = dayStart.getTimeInMillis();
			    item.header = true;
			    items.add(item);
			    currentHeaderDay = dayStart;
			}
			item = new HistoryItem();
			item.task = timeTable.task.name;
			item.project = timeTable.task.project;
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
			    item = new HistoryItem();
			    item.start = start.getTimeInMillis();
			    item.header = true;
			    items.add(item);
			    currentHeaderDay = start;
			}
			item = new HistoryItem();
			item.task = timeTable.task.name;
			item.project = timeTable.task.project;
			item.start = start.getTimeInMillis();
			item.end = currentDay.getTimeInMillis();
			items.add(item);
		    }
		}
	    }
	    return items;
	}

	@Override
	protected void onCancelled() {
	    setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	protected void onPostExecute(List<HistoryItem> result) {
	    history.clear();
	    history.addAll(result);
	    historyAdapter.notifyDataSetChanged();
	    setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	protected void onPreExecute() {
	    setSupportProgressBarIndeterminateVisibility(true);
	}
    }

    class HistoryAdapter extends ArrayAdapter<HistoryItem> {

	public HistoryAdapter(Context context, List<HistoryItem> history) {
	    super(context, 0, history);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    final HistoryItem historyItem = getItem(position);
	    View view = null;
	    if (historyItem.header) {
		view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
			.inflate(R.layout.list_header, null);
	    } else {
		view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
			.inflate(R.layout.history_item, null);
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
						HistoryActivity.this,
						historyItem.start,
						android.text.format.DateUtils.FORMAT_SHOW_YEAR));
		    }
		} else {
		    TextView taskView = (TextView) view
			    .findViewById(R.id.history_task);
		    TextView projectView = (TextView) view
			    .findViewById(R.id.history_project);
		    TextView durationView = (TextView) view
			    .findViewById(R.id.history_duration);
		    TextView rangeView = (TextView) view
			    .findViewById(R.id.history_range);
		    taskView.setText(historyItem.task);
		    projectView.setText(historyItem.project);
		    durationView.setText(DateUtils
			    .formatElapsedTime(historyItem.getDuration()));
		    rangeView
			    .setText(android.text.format.DateUtils
				    .formatDateRange(
					    HistoryActivity.this,
					    historyItem.start,
					    historyItem.running ? new Date()
						    .getTime()
						    : historyItem.end,
					    android.text.format.DateUtils.FORMAT_SHOW_TIME));
		}
	    }
	    return view;
	}

	@Override
	public boolean isEnabled(int position) {
	    return false;
	}
    }

    class HistoryItem {

	public String task;
	public String project;
	public long start;
	public long end;
	public boolean running;
	public boolean header;

	public long getDuration() {
	    return (running ? new Date().getTime() : end) - start;
	}
    }

    public static final String EXTRA_PROJECTS = "projects";
    public static final String EXTRA_PROJECT = "project";

    private String project;
    private ArrayList<String> projects;
    private ArrayList<HistoryItem> history;
    private ArrayAdapter<HistoryItem> historyAdapter;
    private Handler timer;
    private boolean runTimer;

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
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	setContentView(R.layout.activity_history);

	projects = getIntent().getStringArrayListExtra(EXTRA_PROJECTS);
	project = getIntent().getStringExtra(EXTRA_PROJECT);

	history = new ArrayList<HistoryActivity.HistoryItem>();
	historyAdapter = new HistoryAdapter(this, history);

	ActionBar actionBar = getSupportActionBar();
	actionBar.setDisplayHomeAsUpEnabled(true);
	actionBar.setTitle(R.string.history);

	setListAdapter(historyAdapter);

	new LoadHistoryTask().execute(project);

	timer = new Handler();
    }

    @Override
    protected void onPause() {
	super.onPause();
	runTimer = false;
    }

    @Override
    protected void onResume() {
	super.onResume();
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
}
