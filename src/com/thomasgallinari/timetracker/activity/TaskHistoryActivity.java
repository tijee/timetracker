package com.thomasgallinari.timetracker.activity;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.thomasgallinari.timetracker.App;
import com.thomasgallinari.timetracker.R;
import com.thomasgallinari.timetracker.domain.Task;
import com.thomasgallinari.timetracker.fragment.DeleteTaskDialogFragment;
import com.thomasgallinari.timetracker.fragment.TaskHistoryListFragment;

public class TaskHistoryActivity extends SherlockFragmentActivity implements
	DeleteTaskDialogFragment.OnTaskDeletedListener {

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
	    listFragment.refresh(task);
	    invalidateOptionsMenu();
	}
    }

    public static final int REQUEST_EDIT_TASK = 0;

    public static final String EXTRA_TASK = "task";
    public static final String EXTRA_PROJECTS = "projects";

    private static final String TAG_LIST_FRAGMENT = "list_fragment";
    private static final String DIALOG_DELETE_TASK = "delete_task";

    private Task task;
    private ArrayList<String> projects;
    private boolean taskChanged;
    private TaskHistoryListFragment listFragment;

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
	case R.id.menu_start_stop:
	    new ToggleTaskRunningTask().execute(task);
	    return true;
	case R.id.menu_edit:
	    ArrayList<String> projectsParam = new ArrayList<String>(
		    projects.subList(Math.min(projects.size(), 1),
			    projects.size()));
	    startActivityForResult(new Intent(this, TaskEditActivity.class)
		    .putExtra(TaskEditActivity.EXTRA_TASK, task)
		    .putStringArrayListExtra(EXTRA_PROJECTS, projectsParam),
		    REQUEST_EDIT_TASK);
	    return true;
	case R.id.menu_delete:
	    Bundle args = new Bundle();
	    args.putSerializable(DeleteTaskDialogFragment.ARG_TASK, task);
	    DeleteTaskDialogFragment deleteDialogFragment = new DeleteTaskDialogFragment();
	    deleteDialogFragment.setArguments(args);
	    deleteDialogFragment.setOnTaskDeletedListener(this);
	    deleteDialogFragment.show(getSupportFragmentManager(),
		    DIALOG_DELETE_TASK);
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
    public void taskDeleted(Task task) {
	taskChanged = true;
	onBackPressed();
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

	ActionBar actionBar = getSupportActionBar();
	actionBar.setDisplayHomeAsUpEnabled(true);
	actionBar.setTitle(task.name);
	actionBar.setSubtitle(task.project);

	if (savedInstanceState != null) {
	    listFragment = (TaskHistoryListFragment) getSupportFragmentManager()
		    .findFragmentByTag(TAG_LIST_FRAGMENT);
	} else {
	    Bundle args = new Bundle();
	    args.putSerializable(TaskHistoryListFragment.ARG_TASK, task);
	    listFragment = new TaskHistoryListFragment();
	    listFragment.setArguments(args);
	    getSupportFragmentManager()
		    .beginTransaction()
		    .add(R.id.task_history_container, listFragment,
			    TAG_LIST_FRAGMENT).commit();
	}
    }
}
