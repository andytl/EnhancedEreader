package com.example.testerapplication;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

public class ReaderFragment extends Fragment implements View.OnTouchListener {

	private int moveCount;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_reader,
				container, false);
		moveCount = 0;
		displayText(rootView);
		return rootView;
	}
	
	
	private void displayText(View rootView) {
		ScrollView sv = (ScrollView) rootView.findViewById(R.id.scroll_view);
		sv.setOnTouchListener(this);
		TextView tv = new TextView(getActivity());
		String text = "";
		for(int i = 0; i < 1000; i++) { 
			text += i + "\n";
		}
		tv.setText(text);
		tv.setTextSize(20);
		sv.addView(tv);
	}
	

	public void centerScreen(int x, int y) {
		ScrollView sv = (ScrollView) getView().findViewById(R.id.scroll_view);
		int left = 	sv.getLeft();
		int top = sv.getTop();
		int right = sv.getRight();
		int bottom = sv.getBottom();
		int centerX = (right-left)/2;
		int centerY = (bottom-top)/2;
		int diffX = x-centerX;
		diffX = 0;
		int diffY = y-centerY;
		sv.scrollBy(diffX, diffY);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int id = v.getId();
		int action = event.getAction();
		if (id == R.id.scroll_view) {
			if (action == MotionEvent.ACTION_UP) {
				if (moveCount < 5) {
	 				PointerCoords pc0 = new PointerCoords();
					event.getPointerCoords(0, pc0);
					centerScreen((int)pc0.x, (int)pc0.y);
				}
				moveCount = 0;
			} else if (action == MotionEvent.ACTION_MOVE){
				moveCount++;
			}
		}
		return false;
	}
	
	
}
