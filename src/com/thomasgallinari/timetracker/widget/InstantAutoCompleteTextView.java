package com.thomasgallinari.timetracker.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class InstantAutoCompleteTextView extends AutoCompleteTextView {

    public InstantAutoCompleteTextView(Context context) {
	super(context);
	init();
    }

    public InstantAutoCompleteTextView(Context context, AttributeSet attrs) {
	super(context, attrs);
	init();
    }

    public InstantAutoCompleteTextView(Context context, AttributeSet attrs,
	    int defStyle) {
	super(context, attrs, defStyle);
	init();
    }

    @Override
    public boolean enoughToFilter() {
	return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
	    Rect previouslyFocusedRect) {
	super.onFocusChanged(focused, direction, previouslyFocusedRect);
	if (focused) {
	    performFiltering(getText(), 0);
	}
    }

    private void init() {
	setTextColor(getResources()
		.getColor(android.R.color.primary_text_light));
    }
}
