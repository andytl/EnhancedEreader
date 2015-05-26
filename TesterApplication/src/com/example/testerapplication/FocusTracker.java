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
	private int velocity;
	private int scrollId;
	
	
	public FocusTracker(ReaderActivity ra, View rootView, int scrollId) {
		this.rootView = rootView;
		this.ra = ra;
		this.scrollId = scrollId;
		reset();
	}
	
	// double between -1 and 1 signifies valid read
	public void newReadPosition(double x, double y) {
		if (outOfRange(x, y)) {
			//update moving average
			rateOffScreen = rateOffScreen * .99 + .01; 
			return;
		} else {
			rateOffScreen = rateOffScreen * .99;
		}
		double width = rootView.getWidth();
		double height = rootView.getHeight();
		curX = interpolateValue(x, width);
		curY = interpolateValue(y, height);
		avgX = avgX * .5 + curX * .5;
		avgY = avgY * .5 + curY * .5;
		if (avgY < height/3.0) {
			velocity--;
		}
		if (avgY > height * 2.0/3) {
			velocity++;
		}
		ViewGroup circleOverlay = (ViewGroup)rootView.findViewById(R.id.web_circle_overlay);
		clearCircles(circleOverlay);
		drawCircle((float)curX, (float)curY, 20, 0xFFFFFF00, circleOverlay);
		drawCircle((float)avgX, (float)avgY, 20, 0xFF00FFFF, circleOverlay);
		ra.updateFocusRate(getFocusRate());
//		if (velocity > 5 || velocity < 5) {
//			ViewGroup vg = (ViewGroup) rootView.findViewById(scrollId);
//			vg.scrollBy(0, velocity);
//			velocity = 0;
//		}
		System.err.println("avgY: " + avgY);
		System.err.println("velocity: " + velocity);
		System.err.println("height: " + height);
		System.err.println("Height/3:" + height/3.0);
		ViewGroup vg = (ViewGroup) rootView.findViewById(scrollId);
		vg.scrollBy(0, velocity);		
		velocity = 0;
		
	}
	
	public double getFocusRate() {
		return (1-rateOffScreen) * 100;
	}
	
	public void reset() {
		curX = 0;
		curY = 0;
		avgX = 0;
		avgY = 0;
		velocity = 0;
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
