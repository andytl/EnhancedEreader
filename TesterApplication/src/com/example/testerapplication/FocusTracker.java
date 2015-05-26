package com.example.testerapplication;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.example.testerapplication.display.CircleView;

public class FocusTracker {

	public static final double MAX_AVERAGE_RANGE = 100;
	
	private double rateOffScreen;
	private double curX;
	private double curY;
	private double avgX;
	private double avgY;
	private View rootView;
	private ReaderActivity ra;
	
	
	public FocusTracker(ReaderActivity ra, View rootView) {
		this.rootView = rootView;
		this.ra = ra;
		reset();
	}
	
	// double between -1 and 1 signifies valid read
	public void newReadPosition(double x, double y) {
		if (outOfRange(x, y)) {
			//update moving average
			rateOffScreen = rateOffScreen * .99 + .01; 
		} else {
			rateOffScreen = rateOffScreen * .99;
		}
		curX = interpolateValue(x, rootView.getWidth());
		curY = interpolateValue(y, rootView.getHeight());
		avgX = avgX * .5 + curX * .5;
		avgY = avgY * .5 + curY * .5;
		ViewGroup circleOverlay = (ViewGroup)rootView.findViewById(R.id.web_circle_overlay);
		clearCircles(circleOverlay);
		drawCircle((float)curX, (float)curY, 20, 0xFFFFFF00, circleOverlay);
		drawCircle((float)avgX, (float)avgY, 20, 0xFF00FFFF, circleOverlay);
		ra.updateFocusRate(getFocusRate());
	}
	
	public double getFocusRate() {
		return (1-rateOffScreen) * 100;
	}
	
	public void reset() {
		curX = 0;
		curY = 0;
		avgX = 0;
		avgY = 0;
		rateOffScreen = 0;
	}
	
	private void clearCircles(ViewGroup circleOverlay) {
		circleOverlay.removeAllViews();
	}
	
	private void drawCircle(float x, float y, int radius, int color, ViewGroup circleOverlay) {
		if (circleOverlay == null) {
			return;
		}
		CircleView cv = new CircleView(ra, x, y, radius, color);
		circleOverlay.addView(cv);
	}
	
	private boolean outOfRange(double x, double y) {
		return x < -1 || y < -1 || y > 1 || x > 1;
	}
	
	// maps a -1 to 1 value onto range
	private double interpolateValue(double value, double range) {
		return (value + 1) / 2 * range;
	}
}
