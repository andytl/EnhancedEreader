package com.example.testerapplication;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.testerapplication.display.CircleView;

public class ReadingMonitor {

	private static final int GRADIENT_SIZE = 10;
	private static final int FOCUS_SIZE = 300;
	
	private static final double AVG_UPDATE_FACTOR = 0.2;
	private static final double ERROR_UPDATE_FACTOR = 0.8;
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
	
	private int viewIdScrollable;
	private int circleOverlayId;
	private int colorOverlayId;
	private Context context;
	
	private ViewGroup circleOverlay;
	private TableLayout colorOverlay;
	private ViewGroup scrollable;
	private View rootView;
	
	public ReadingMonitor(int viewIdScrollable, int circleOverlayId, int colorOverlayId, Context context) {
		this.viewIdScrollable = viewIdScrollable;
		this.circleOverlayId = circleOverlayId;
		this.colorOverlayId = colorOverlayId;
		this.context = context;
	}
	
	public void createColorOverlay(View rootView, Context context) {
		int top = rootView.getTop();
		int bottom = rootView.getBottom();
		int left = rootView.getLeft();
		int right = rootView.getRight();
		System.out.println("Overlay dims:");
		System.out.println("\tleft: \t" + left );
		System.out.println("\tright: \t" + right );
		System.out.println("\ttop: \t" + top );
		System.out.println("\tbottom: \t" + bottom );
		TableLayout table = (TableLayout)rootView.findViewById(colorOverlayId);
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
	
	private void centerScreen(View rootView, int x, int y) {
		ViewGroup vg = (ViewGroup)rootView.findViewById(viewIdScrollable);
		int left = 	vg.getLeft();
		int top = vg.getTop();
		int right = vg.getRight();
		int bottom = vg.getBottom();
		int centerX = (right-left)/2;
		int centerY = (bottom-top)/2;
		int dx = x-centerX;
		dx = 0;
		int dy = y-centerY;
		vg.scrollBy(dx, dy);
	}
	
	public void newReadPosition(View rootView, double x, double y) {
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
			if (outCount > 30) {
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
		
		circleOverlay = (ViewGroup)rootView.findViewById(circleOverlayId);
		colorOverlay = (TableLayout)rootView.findViewById(colorOverlayId);
		scrollable = (ViewGroup)rootView.findViewById(viewIdScrollable);
		this.rootView = rootView;
		
		//TODO: figure out double vs int
		drawCircle((int)x, (int)y, 30, 0x66000000, true);
		drawCircle((int)avgX, (int)avgY, 20, 0xFFFF0000, false);
		updateScroll();
		updateBlur();
		System.out.println("ValidRate: " + validRate);
		circleOverlay = null;
		colorOverlay = null;
		scrollable = null;
		this.rootView = null;

	}

	private void updateBlur() {
		System.out.println("udpateBlur");
		int top = scrollable.getTop();
		int bottom = scrollable.getBottom();
		int center = (top + bottom)/2;
		if (validRate > 0.7) {
			focusRange(top, bottom);
			return;
		} else {
			//TODO: figure out int / double stuff
			colorScreen((int)avgX, (int)avgY);
		}
		
	}
	
	private void colorScreen(int x, int y) {
		int top = rootView.getTop();
		int bottom = rootView.getBottom();
		int upper = Math.max(y - FOCUS_SIZE, 0);
		int lower = Math.min(y + FOCUS_SIZE, bottom);
		colorRange(top, upper, 0x0000FF00, 0x00, 0x44);
		colorRange(lower, bottom, 0x000FF000, 0x44, 0x00);
		colorRange(upper, lower, 0x00000000, 0x00, 0x00);
	}
	
	private void updateScroll() {
		if (KEEP_TOP.equals(curMode)) {
			int center = (scrollable.getBottom() + scrollable.getTop())/2;
			System.out.println("Center: " + center + "\tAvg: " + avgY);
			if (center < avgY) {
				scrollable.scrollBy(0, center/6);
				clearReadData();
				waitCount = 30;
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
	
	private void focusRange(int upper, int lower) {
		int rowSize = colorOverlay.getChildAt(0).getHeight();
		int upperRow = upper/rowSize;
		int lowerRow = lower/rowSize;
		for (int i = upperRow; i < lowerRow; i++) {
			TableRow tr_blur = (TableRow)colorOverlay.getChildAt(i);
			tr_blur.setBackgroundColor(0x00000000);
			
		}
	}
	
	private void colorRange(int upper, int lower, int color, int alphaBottom, int alphaTop) {
		int rowSize = colorOverlay.getChildAt(0).getHeight();
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
			TableRow tr = (TableRow)colorOverlay.getChildAt(i);
			tr.setBackgroundColor(color | alpha);	
		}
	}
	
	private void drawCircle(int x, int y, int radius, boolean clearAll) {
		drawCircle(x, y, radius, 0x88000000, clearAll);
	}
	
	private void drawCircle(int x, int y, int radius, int color, boolean clearAll) {
		if (circleOverlay == null) {
			return;
		}
		if (clearAll) {
			circleOverlay.removeAllViews();
		}
		CircleView cv = new CircleView(context, x, y, radius, color);
		circleOverlay.addView(cv);
	}
	
}
