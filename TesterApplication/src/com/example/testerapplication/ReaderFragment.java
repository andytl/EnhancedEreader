package com.example.testerapplication;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ReaderFragment extends Fragment implements View.OnTouchListener {

	private static final int GRADIENT_SIZE = 10;
	private static final int FOCUS_SIZE = 100;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_reader,
				container, false);
		displayText(rootView);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		final View rootView = getView();
		rootView.post(new Runnable() {
			@Override
			public void run() {
				createOverlay(rootView);
			}
		});
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
		int dx = x-centerX;
		dx = 0;
		int dy = y-centerY;
		sv.smoothScrollBy(dx, dy);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int id = v.getId();
		int action = event.getAction();
		if (id == R.id.scroll_view) {

			int[] location = new int[2];
			int[] locationRoot = new int[2];
			v.getLocationInWindow(location);
			getView().getLocationInWindow(locationRoot);
			PointerCoords pc0 = new PointerCoords();
			event.getPointerCoords(0, pc0);
			colorScreen((int)pc0.x+(location[0]-locationRoot[0]), (int)pc0.y+(location[1]-locationRoot[1]));

		}
		return false;
	}
	
	private void createOverlay(View rootView) {
		int top = rootView.getTop();
		int bottom = rootView.getBottom();
		int left = rootView.getLeft();
		int right = rootView.getRight();
		Context context = getActivity();
		TableLayout table = (TableLayout)rootView.findViewById(R.id.overlay);
		for (int i = 0; i < (bottom -top)/GRADIENT_SIZE; i++) {
			TableRow tr = new TableRow(context);
			tr.setLayoutParams(new TableRow.LayoutParams(right-left, GRADIENT_SIZE));
			tr.setBackgroundColor(0x00000000);
			TextView tv = new TextView(context);
			tv.setText("");
			tr.addView(tv);
			table.addView(tr);
		}
	}
	
	public void colorScreen(int x, int y) {
		View rootView = getView();
		int top = rootView.getTop();
		int bottom = rootView.getBottom();
		int upper = Math.max(y - FOCUS_SIZE, 0);
		int lower = Math.min(y + FOCUS_SIZE, bottom);
		colorRange(top, upper, 0x0000FF00, 0x00, 0x44);
		colorRange(lower, bottom, 0x000FF000, 0x44, 0x00);
		colorRange(upper, lower, 0x00000000, 0x00, 0x00);
	}

	private void colorRange(int upper, int lower, int color, int alphaBottom, int alphaTop) {
		View rootView = getView();
		TableLayout table = (TableLayout) rootView.findViewById(R.id.overlay);
		int rowSize = table.getChildAt(0).getHeight();
		int upperRow = upper/rowSize;
		int lowerRow = lower/rowSize;
		for (int i = upperRow; i < lowerRow; i++) {
			int alpha;
			if (alphaBottom < alphaTop) {
				alpha = (((int)((alphaTop - alphaBottom) * (lowerRow - i) * 100/(lowerRow - upperRow)))/ 100);
			} else if (alphaBottom > alphaTop) {
				alpha = (alphaBottom - alphaTop) - (((int)((alphaBottom - alphaTop) * (lowerRow - i) * 100/(lowerRow - upperRow)))/ 100);
				
			} else {
				alpha = alphaTop;
			}
			alpha = alpha << 24;
			TableRow tr = (TableRow)table.getChildAt(i);
			System.out.println(Integer.toHexString(alpha));
			tr.setBackgroundColor(color | alpha);	
		}
	}
}
