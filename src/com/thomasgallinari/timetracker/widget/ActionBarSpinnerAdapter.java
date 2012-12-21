package com.thomasgallinari.timetracker.widget;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.thomasgallinari.timetracker.R;

public class ActionBarSpinnerAdapter extends ArrayAdapter<String> {

    public ActionBarSpinnerAdapter(Context context, int textViewResourceId,
	    List<String> objects) {
	super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	// hack to set the drop down text color
	View view = super.getView(position, convertView, parent);
	((TextView) view.findViewById(android.R.id.text1))
		.setTextColor(getContext().getResources().getColor(
			R.color.abs__primary_text_holo_dark));
	return view;
    }
}