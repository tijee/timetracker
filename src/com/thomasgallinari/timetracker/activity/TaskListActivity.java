package com.thomasgallinari.timetracker.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.thomasgallinari.timetracker.App;
import com.thomasgallinari.timetracker.R;
import com.thomasgallinari.timetracker.domain.Task;
import com.thomasgallinari.timetracker.util.DateUtils;
import com.thomasgallinari.timetracker.widget.ActionBarSpinnerAdapter;

public class TaskListActivity extends SherlockListActivity implements
	ActionBar.OnNavigationListener, View.OnClickListener {

    class LoadProjectsTask extends AsyncTask<Void, Void, List<String>> {

	@Override
	protected List<String> doInBackground(Void... params) {
	    List<String> projects = ((App) getApplication()).getTaskDao()
		    .getAllProjects();
	    return projects;
	}

	@Override
	protected void onPostExecute(List<String> result) {
	    projects.clear();
	    projects.addAll(result);
	    Collections.sort(projects);
	    projects.add(0, getString(R.string.all_projects));
	    projectAdapter.notifyDataSetChanged();
	    int selectedProjectIndex = 0;
	    if (selectedProject != null) {
		selectedProjectIndex = Math.max(0,
			projects.indexOf(selectedProject));
	    }
	    getSupportActionBar().setSelectedNavigationItem(
		    selectedProjectIndex);
	}
    }

    class LoadTasksTask extends AsyncTask<Bundle, Void, List<Task>> {

	private static final String PARAM_PROJECT = "project";

	@Override
	protected List<Task> doInBackground(Bundle... params) {
	    String project = (String) params[0].get(PARAM_PROJECT);
	    return ((App) getApplication()).getTaskDao().getByProject(project,
		    false);
	}

	@Override
	protected void onPostExecute(List<Task> result) {
	    Collections.sort(result, Collections.reverseOrder());
	    tasks.clear();
	    tasks.addAll(result);
	    taskAdapter.notifyDataSetChanged();
	}
    }

    class TaskAdapter extends ArrayAdapter<Task> {

	public TaskAdapter(Context context, List<Task> tasks) {
	    super(context, 0, tasks);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    View view = convertView;
	    if (view == null) {
		view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
			.inflate(R.layout.task_item, null);
	    }
	    final Task task = getItem(position);
	    if (task != null) {
		TextView taskView = (TextView) view.findViewById(R.id.task);
		TextView projectView = (TextView) view
			.findViewById(R.id.project);
		TextView timeSpentView = (TextView) view
			.findViewById(R.id.time_spent);
		TextView totalTimeSpentView = (TextView) view
			.findViewById(R.id.total_time_spent);
		final ImageView imageView = (ImageView) view
			.findViewById(R.id.task_icon);
		taskView.setText(task.name);
		projectView.setText(task.project);
		long totalTimeSpent = task.getTimeSpent();
		if (task.running) {
		    long timeSpent = new Date().getTime()
			    - task.getStartTimeIfRunning();
		    totalTimeSpent += timeSpent;
		    timeSpentView.setText(DateUtils
			    .formatElapsedTime(timeSpent));
		}
		RelativeLayout.LayoutParams totalTimeSpentViewLayoutParams = (RelativeLayout.LayoutParams) totalTimeSpentView
			.getLayoutParams();
		timeSpentView.setVisibility(task.running ? View.VISIBLE
			: View.GONE);
		totalTimeSpentViewLayoutParams.addRule(
			RelativeLayout.CENTER_VERTICAL, task.running ? 0
				: RelativeLayout.TRUE);
		totalTimeSpentView
			.setLayoutParams(totalTimeSpentViewLayoutParams);
		totalTimeSpentView.setText(DateUtils
			.formatElapsedTime(totalTimeSpent));
		imageView.setTag(task);
		imageView.setImageDrawable(getResources().getDrawable(
			task.running ? R.drawable.ic_media_pause
				: R.drawable.ic_media_play));
		imageView
			.setContentDescription(getText(task.running ? R.string.stop
				: R.string.start));
		imageView.setOnClickListener(TaskListActivity.this);
		view.setBackgroundColor(getResources().getColor(
			task.running ? R.color.tertiary
				: android.R.color.transparent));
	    }
	    return view;
	}
    }

    class ToggleTaskRunningTask extends AsyncTask<Task, Void, Task> {

	@Override
	protected Task doInBackground(Task... params) {
	    return ((App) getApplication()).getTaskDao().toggleRunning(
		    params[0]);
	}

	@Override
	protected void onPostExecute(Task result) {
	    for (Task task : tasks) {
		if (task.id == result.id) {
		    task = result;
		    break;
		}
	    }
	    taskAdapter.notifyDataSetChanged();
	}
    }

    public static final int REQUEST_NEW_TASK = 0;
    public static final int REQUEST_TASK_HISTORY = 1;

    public static final String EXTRA_TASK_CHANGED = "task_changed";

    private static final String STATE_SELECTED_PROJECT = "selected_project";

    private ArrayList<String> projects;
    private ArrayList<Task> tasks;
    private String selectedProject;
    private ArrayAdapter<String> projectAdapter;
    private ArrayAdapter<Task> taskAdapter;
    private Handler timer;
    private boolean runTimer;

    @Override
    public void onClick(View v) {
	if (v instanceof ImageView) {
	    new ToggleTaskRunningTask().execute((Task) v.getTag());
	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getSupportMenuInflater();
	inflater.inflate(R.menu.activity_task_list, menu);
	return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
	ArrayList<String> projectsParam;
	Intent intent;
	switch (item.getItemId()) {
	case R.id.menu_add:
	    projectsParam = new ArrayList<String>(projects.subList(
		    Math.min(projects.size(), 1), projects.size()));
	    intent = new Intent(this, TaskEditActivity.class)
		    .putStringArrayListExtra(TaskEditActivity.EXTRA_PROJECTS,
			    projectsParam);
	    if (selectedProject != null) {
		intent.putExtra(TaskEditActivity.EXTRA_PROJECT, selectedProject);
	    }
	    startActivityForResult(intent, REQUEST_NEW_TASK);
	    return true;
	case R.id.menu_history:
	    projectsParam = projects;
	    intent = new Intent(this, HistoryActivity.class)
		    .putStringArrayListExtra(HistoryActivity.EXTRA_PROJECTS,
			    projectsParam);
	    if (selectedProject != null) {
		intent.putExtra(HistoryActivity.EXTRA_PROJECT, selectedProject);
	    }
	    startActivity(intent);
	    return true;
	}
	return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
	selectedProject = itemPosition > 0 ? projects.get(itemPosition) : null;
	loadTasks();
	return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	switch (requestCode) {
	case REQUEST_NEW_TASK:
	    if (resultCode == RESULT_OK) {
		loadProjects();
		loadTasks();
	    }
	    break;
	case REQUEST_TASK_HISTORY:
	    if (data.getBooleanExtra(EXTRA_TASK_CHANGED, false)) {
		loadProjects();
		loadTasks();
	    }
	    break;
	}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_task_list);

	projects = new ArrayList<String>();
	tasks = new ArrayList<Task>();

	projectAdapter = new ActionBarSpinnerAdapter(getSupportActionBar()
		.getThemedContext(), R.layout.sherlock_spinner_dropdown_item,
		projects);
	taskAdapter = new TaskAdapter(this, tasks);

	ActionBar actionBar = getSupportActionBar();
	actionBar.setDisplayShowTitleEnabled(false);
	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
	actionBar.setListNavigationCallbacks(projectAdapter, this);

	setListAdapter(taskAdapter);

	loadProjects();
	loadTasks();

	timer = new Handler();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
	startActivityForResult(
		new Intent(this, TaskHistoryActivity.class).putExtra(
			TaskHistoryActivity.EXTRA_TASK, tasks.get(position))
			.putStringArrayListExtra(
				TaskHistoryActivity.EXTRA_PROJECTS, projects),
		REQUEST_TASK_HISTORY);
    }

    @Override
    protected void onPause() {
	super.onPause();
	runTimer = false;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
	super.onRestoreInstanceState(savedInstanceState);
	selectedProject = savedInstanceState.getString(STATE_SELECTED_PROJECT);
	loadTasks();
    }

    @Override
    protected void onResume() {
	super.onResume();
	runTimer = true;
	timer.post(new Runnable() {

	    @Override
	    public void run() {
		taskAdapter.notifyDataSetChanged();
		if (runTimer) {
		    timer.postDelayed(this, 1000);
		}
	    }
	});
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
	outState.putString(STATE_SELECTED_PROJECT, selectedProject);
    }

    private void loadProjects() {
	new LoadProjectsTask().execute();
    }

    private void loadTasks() {
	Bundle params = new Bundle();
	params.putString(LoadTasksTask.PARAM_PROJECT, selectedProject);
	new LoadTasksTask().execute(params);
    }
}
