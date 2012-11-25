package com.thomasgallinari.timetracker.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.thomasgallinari.timetracker.App;
import com.thomasgallinari.timetracker.R;
import com.thomasgallinari.timetracker.domain.Task;

public class TaskEditActivity extends SherlockActivity {

    class SaveTask extends AsyncTask<Task, Void, Task> {

	@Override
	protected Task doInBackground(Task... params) {
	    Task task = params[0];
	    if (task.id == 0) {
		return ((App) getApplication()).getTaskDao().insert(params[0]);
	    } else {
		return ((App) getApplication()).getTaskDao().update(params[0]);
	    }
	}

	@Override
	protected void onCancelled() {
	    setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	protected void onPostExecute(Task result) {
	    setSupportProgressBarIndeterminateVisibility(false);
	    setResult(RESULT_OK, new Intent().putExtra(
		    TaskHistoryActivity.EXTRA_TASK, result));
	    finish();
	}

	@Override
	protected void onPreExecute() {
	    setSupportProgressBarIndeterminateVisibility(true);
	}
    }

    public static final String EXTRA_TASK = "task";
    public static final String EXTRA_PROJECT = "project";
    public static final String EXTRA_PROJECTS = "projects";

    private EditText taskInput;
    private AutoCompleteTextView projectInput;
    private Task task;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getSupportMenuInflater();
	inflater.inflate(R.menu.activity_task_edit, menu);
	return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
	switch (item.getItemId()) {
	case R.id.menu_save:
	    String name = taskInput.getText().toString();
	    String project = projectInput.getText().toString();
	    if (task == null) {
		task = new Task();
	    }
	    task.name = name;
	    task.project = project;
	    new SaveTask().execute(task);
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
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	setContentView(R.layout.activity_task_edit);

	taskInput = (EditText) findViewById(R.id.task_input);
	projectInput = (AutoCompleteTextView) findViewById(R.id.project_input);

	projectInput.setAdapter(new ArrayAdapter<String>(this,
		android.R.layout.simple_spinner_dropdown_item, getIntent()
			.getStringArrayListExtra(EXTRA_PROJECTS)));

	ActionBar actionBar = getSupportActionBar();
	actionBar.setDisplayHomeAsUpEnabled(true);

	if (getIntent().hasExtra(EXTRA_TASK)) {
	    task = (Task) getIntent().getSerializableExtra(EXTRA_TASK);
	    actionBar.setTitle(task.name);
	    actionBar.setSubtitle(task.project);
	    taskInput.setText(task.name);
	    projectInput.setText(task.project);
	} else {
	    actionBar.setTitle(R.string.new_task);
	    if (getIntent().hasExtra(EXTRA_PROJECT)) {
		projectInput.setText(getIntent().getStringExtra(EXTRA_PROJECT));
	    }
	}
    }
}
