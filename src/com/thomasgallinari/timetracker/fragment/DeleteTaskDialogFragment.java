package com.thomasgallinari.timetracker.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.thomasgallinari.timetracker.App;
import com.thomasgallinari.timetracker.R;
import com.thomasgallinari.timetracker.domain.Task;

public class DeleteTaskDialogFragment extends DialogFragment implements
	DialogInterface.OnClickListener {

    public interface OnTaskDeletedListener {
	void taskDeleted(Task task);
    }

    class DeleteTaskTask extends AsyncTask<Task, Void, Task> {

	@Override
	protected Task doInBackground(Task... params) {
	    Task task = params[0];
	    ((App) getActivity().getApplication()).getTaskDao().delete(task);
	    return task;
	}

	@Override
	protected void onPostExecute(Task result) {
	    listener.taskDeleted(result);
	}
    }

    class HideTaskTask extends AsyncTask<Task, Void, Task> {

	@Override
	protected Task doInBackground(Task... params) {
	    Task task = params[0];
	    return ((App) getActivity().getApplication()).getTaskDao().hide(
		    task);
	}

	@Override
	protected void onPostExecute(Task result) {
	    if (listener != null) {
		listener.taskDeleted(result);
	    }
	}
    }

    public static final String ARG_TASK = "task";

    private Task task;
    private OnTaskDeletedListener listener;

    @Override
    public void onClick(DialogInterface dialog, int id) {
	switch (id) {
	case DialogInterface.BUTTON_POSITIVE:
	    new DeleteTaskTask().execute(task);
	    break;
	case DialogInterface.BUTTON_NEUTRAL:
	    new HideTaskTask().execute(task);
	    break;
	}
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	task = (Task) getArguments().getSerializable(ARG_TASK);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setMessage(R.string.delete_message)
		.setPositiveButton(R.string.delete_button_yes, this)
		.setNegativeButton(R.string.delete_button_cancel, null)
		.setNeutralButton(R.string.delete_button_no, this);
	return builder.create();
    }

    public void setOnTaskDeletedListener(OnTaskDeletedListener listener) {
	this.listener = listener;
    }
}
