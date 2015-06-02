package com.example.testerapplication;

import java.util.HashMap;
import java.util.Map;

import android.view.View;
import android.view.ViewGroup;

import com.example.testerapplication.display.CircleView;

public class FocusTracker {

	public static final double MAX_AVERAGE_RANGE = 100;
	

	
	
	private double rateOffScreen;
	private long startTime = -1;
	private long endTime = -1;
	private long timeReading;
	private long totalTime;
	private long tempTime = -1;
	private boolean curFocused;
	
	private double curX;
	private double curY;
	private double avgX;
	private double avgY;
	private View rootView;
	private ReaderActivity ra;
	private int velocity;
	private int scrollId;
	
	private double avgUpdate = .85;
	private double offScreenUpdate = .99;
	
	
	public FocusTracker(ReaderActivity ra, View rootView, int scrollId) {
		this.rootView = rootView;
		this.ra = ra;
		this.scrollId = scrollId;
		curFocused = false;
		reset();
	}
	
	// double between -1 and 1 signifies valid read
	public void newReadPosition(double x, double y) {
		if (startTime < 0) {
			startTime = System.currentTimeMillis();
		} 
		if (tempTime < 0) {
			tempTime = System.currentTimeMillis();
		}
		if (outOfRange(x, y)) {
			//update moving average
			rateOffScreen = rateOffScreen * offScreenUpdate + 1 -offScreenUpdate; 
			if  (curFocused) {
				long time = System.currentTimeMillis() - tempTime;
				tempTime = System.currentTimeMillis();
				timeReading += time;
			}
			
			curFocused = false;
			return;
		} else {
			if (!curFocused) {
				tempTime = System.currentTimeMillis();
			}
			curFocused = true;
			rateOffScreen = rateOffScreen * offScreenUpdate;
		}
		double width = rootView.getWidth();
		double height = rootView.getHeight();
		curX = interpolateValue(x, width);
		curY = interpolateValue(y, height);
		avgX = avgX * avgUpdate + curX * (1-avgUpdate);
		avgY = avgY * avgUpdate + curY * (1-avgUpdate);
		if (avgY < height/3.0) {
			velocity--;
		}
		if (avgY > height * 2.0/3) {
			velocity++;
		}
		ViewGroup circleOverlay = (ViewGroup)rootView.findViewById(R.id.web_circle_overlay);
		clearCircles(circleOverlay);
		drawCircle((float)avgX, (float)avgY, 30, 0xFF00FFFF, circleOverlay);
		drawCircle((float)curX, (float)curY, 25, 0xFFFFFF00, circleOverlay);
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
//		return (1-rateOffScreen) * 100;
		return timeReading*1.0/totalTime;
	}
	
	public FocusData getData() {
		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;
		return new FocusData(totalTime, timeReading, getFocusRate(), System.currentTimeMillis());
	}
	
	public void reset() {
		totalTime = 0;
		timeReading = 0;
		startTime = -1;
		endTime  = -1;
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
