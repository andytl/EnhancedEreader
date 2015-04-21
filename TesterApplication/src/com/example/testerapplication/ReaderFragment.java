package com.example.testerapplication;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.testerapplication.display.BlurDrawable;
import com.example.testerapplication.display.CircleView;

public class ReaderFragment extends Fragment implements View.OnTouchListener {

	private static final int GRADIENT_SIZE = 10;
	private static final int FOCUS_SIZE = 100;
	
	private static final double AVG_UPDATE_FACTOR = 0.8;
	private static final double ERROR_UPDATE_FACTOR = 0.92;
	private static final double MAX_VALID_DX = 1000;
	private static final double MAX_VALID_DY = 200;
	
	public static final String KEEP_TOP = "TOP";
	public static final String KEEP_CENTER = "CENTER";
	public static final String FULL_PAGE = "FULL";
	
	public static String curMode = KEEP_TOP;
	
	private long down = -1;
	private long moveCount = 0;
	
	private double avgX = -1;
	private double avgY = -1;
	private double curX = -1;
	private double curY = -1;
	private double outX = -1;
	private double outY = -1;
	private int outCount = 0;
	private double validRate = 1;
	private int waitCount = 0;
	
	
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
				createOverlay(rootView, R.id.color_overlay);
				createOverlay(rootView, R.id.blur_overlay);
			}
		});
	}
		
	private void displayText(View rootView) {
		ScrollView sv = (ScrollView) rootView.findViewById(R.id.scroll_view);
		sv.setOnTouchListener(this);
		TextView tv = new TextView(getActivity());
		String text = "";
		for(int i = 0; i < 1000; i++) { 
			text += i + "XXXXXXXXXXX" + "\n";
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
		PointerCoords pc0 = new PointerCoords();
		event.getPointerCoords(0, pc0);
		drawCircle((int)pc0.x, (int)pc0.y, 30, true);
		if (id == R.id.scroll_view) {
			newReadPosition(pc0.x, pc0.y);
		}
		return true;
	}
	
	

	
	
	private void newReadPosition(double x, double y) {
		if (waitCount > 0) {
			//recently scrolled. want to wait for the eys to adjust
			waitCount--;
			return;
		}
		
		// Check if first read point. Call has side effects!
		if (!initialize(x, y)) {
			return;
		}
		// check if point is too far away from average read position
		if (Math.abs(x-avgX) > MAX_VALID_DX || Math.abs(y-avgY) > MAX_VALID_DY) {
			if (Math.abs(outX-x) < MAX_VALID_DX && Math.abs(y-outY) < MAX_VALID_DY) {
				outCount++;
			} else {
				outCount = 0;
			}
			outX = x;
			outY = y;
			if (outCount > 10) {
				avgX = outX;
				avgY = outY;
				validRate = 1;
			} else {
				validRate = validRate * ERROR_UPDATE_FACTOR;
			}
		} else {
			validRate = validRate * ERROR_UPDATE_FACTOR + (1-ERROR_UPDATE_FACTOR);
			avgX = x * AVG_UPDATE_FACTOR + (1-AVG_UPDATE_FACTOR) * avgX;
			avgY = y * AVG_UPDATE_FACTOR + (1-AVG_UPDATE_FACTOR) * avgY;
		}
		//TODO: figure out double vs int
		drawCircle((int)avgX, (int)avgY, 20, 0xFFFF0000, false);
		updateScroll();
		updateBlur();
		System.out.println("ValidRate: " + validRate);

	}
	
	private void updateBlur() {
		System.out.println("udpateBlur");
		View rootView = getView();
		int top = rootView.getTop();
		int bottom = rootView.getBottom();
		int center = (top + bottom)/2;
		if (validRate > 0.7) {
			focusRange(top, bottom);
			return;
		} 
		if (KEEP_TOP.equals(curMode)) {
			colorScreen(bottom, center/2);
		}
	}
	
	private void updateScroll() {
		ScrollView sv = (ScrollView)getView().findViewById(R.id.scroll_view);
		if (KEEP_TOP.equals(curMode)) {
			int center = (sv.getBottom() + sv.getTop())/2;
			System.out.println("Center: " + center + "\tAvg: " + avgY);
			if (center < avgY) {
				sv.smoothScrollBy(0, center/6);
				clearReadData();
				waitCount = 6;
			}
		} else if (KEEP_CENTER.equals(curMode)) {
			
		} else if (FULL_PAGE.equals(curMode)) {
			
		}
	}
	
	private void clearReadData() {
		avgX = -1;
		avgY = -1;
		curX = -1;
		curY = -1;
		outX = -1;
		outY = -1;
		outCount = 0;
	}
	
	// if uninitialized, uses x, y as initial values. Returns false if uninitialized when called
	private boolean initialize(double x, double y) {
		if (curX == -1 || curY == -1 || avgX == -1 || avgY == -1) {
			curX = x;
			avgX = x;
			curY = y;
			avgY = y;
			validRate = 1;
			return false;
		}
		return true;
	}
	
	private void createOverlay(View rootView, int tableId) {
		int top = rootView.getTop();
		int bottom = rootView.getBottom();
		int left = rootView.getLeft();
		int right = rootView.getRight();
		Context context = getActivity();
		TableLayout table = (TableLayout)rootView.findViewById(tableId);
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
	
	public void blurScreen(int x, int y) {
		View rootView = getView();
		int top = rootView.getTop();
		int bottom = rootView.getBottom();
		int upper = Math.max(y - FOCUS_SIZE, 0);
		int lower = Math.min(y + FOCUS_SIZE, bottom);
		blurRange(top, upper);
		blurRange(lower, bottom);
		focusRange(upper, lower);
	}

	private void blurRange(int upper, int lower) {
		View rootView = getView();
		TableLayout color_table = (TableLayout) rootView.findViewById(R.id.color_overlay);
		TableLayout blur_table = (TableLayout) rootView.findViewById(R.id.blur_overlay);
		int rowSize = color_table.getChildAt(0).getHeight();
		int upperRow = upper/rowSize;
		int lowerRow = lower/rowSize;
		for (int i = upperRow; i < lowerRow; i++) {
			TableRow tr_color = (TableRow)color_table.getChildAt(i);
			TableRow tr_blur = (TableRow)blur_table.getChildAt(i);
			BlurDrawable bd = new BlurDrawable(tr_color, 10);
			tr_blur.setBackground(bd);
			
		}
	}
	
	private void focusRange(int upper, int lower) {
		View rootView = getView();
		TableLayout blur_table = (TableLayout) rootView.findViewById(R.id.color_overlay);
		int rowSize = blur_table.getChildAt(0).getHeight();
		int upperRow = upper/rowSize;
		int lowerRow = lower/rowSize;
		for (int i = upperRow; i < lowerRow; i++) {
			TableRow tr_blur = (TableRow)blur_table.getChildAt(i);
			tr_blur.setBackgroundColor(0x00000000);
			
		}
	}
	
	private void colorRange(int upper, int lower, int color, int alphaBottom, int alphaTop) {
		View rootView = getView();
		TableLayout table = (TableLayout) rootView.findViewById(R.id.color_overlay);
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
			tr.setBackgroundColor(color | alpha);	
		}
	}
	
	private void drawCircle(int x, int y, int radius, boolean clearAll) {
		drawCircle(x, y, radius, 0x88000000, clearAll);
	}
	
	private void drawCircle(int x, int y, int radius, int color, boolean clearAll) {
		RelativeLayout overlay = (RelativeLayout)getView().findViewById(R.id.circle_overlay);
		if (clearAll) {
			overlay.removeAllViews();
		}
		CircleView cv = new CircleView(getActivity(), x, y, radius, color);
		overlay.addView(cv);
	}
	
	
	
	
	
	
	
	
	
//	
//	public boolean onTouch(View v, MotionEvent event) {
//		int id = v.getId();
//		int action = event.getAction();
//		PointerCoords pc0 = new PointerCoords();
//		event.getPointerCoords(0, pc0);
//		drawCircle((int)pc0.x, (int)pc0.y, 20);
//		if (id == R.id.scroll_view) {
//			if (action == MotionEvent.ACTION_DOWN) {
//				down = System.currentTimeMillis();
//			}
//			if (action == MotionEvent.ACTION_MOVE){
//				moveCount++;
//			}
//			int[] location = new int[2];
//			int[] locationRoot = new int[2];
//			v.getLocationInWindow(location);
//			getView().getLocationInWindow(locationRoot);
//			colorScreen((int)pc0.x+(location[0]-locationRoot[0]), (int)pc0.y+(location[1]-locationRoot[1]));
//			if (action == MotionEvent.ACTION_UP){
//				long up = System.currentTimeMillis();
//				if (moveCount < 5 && up - down > 2000) {
//					centerScreen((int)pc0.x, (int)pc0.y);
//					colorScreen((v.getRight() - v.getLeft())/2 + (location[0]-locationRoot[0]), (v.getBottom() - v.getTop())/2 + (location[1]-locationRoot[1]));
//				}
//				moveCount = 0;
//				down = -1;
//			}
//			
//		}
//		return false;
//	}
}
