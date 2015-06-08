package com.example.enhancedereader;

import android.view.View;
import android.view.ViewGroup;

import com.example.enhancedereader.datastructures.FocusData;
import com.example.enhancedereader.display.CircleView;

public class FocusTracker {

	public static final double MAX_AVERAGE_RANGE = 100;
	private double rateOffScreen; // "moving average" of how often the user looks away from the device
	private double dartingRate; // "moving average" of how often the user's eye darts around the screen
	// constants for updating the moving averages. need to be optimized
	private double avgUpdate = .85; 
	private double offScreenUpdate = .99;
	
	private long startTime = -1;
	private long endTime = -1;
	private long timeReading;
	private long totalTime;
	private long tempTime = -1;
	private boolean curFocused; // whether the user is currently looking at the screen or not
	
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
		curFocused = false;
		reset();
	}
	
	// double between -1 and 1 signifies valid read
	public void newReadPosition(double x, double y) {
		// initialize if necessary
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
		// convert parameters to screen size
		double width = rootView.getWidth();
		double height = rootView.getHeight();
		curX = interpolateValue(x, width);
		curY = interpolateValue(y, height);
		
		//update moving average
		if (Math.abs(curY - avgY) < height/3) {
			dartingRate = dartingRate * offScreenUpdate + 1 - offScreenUpdate;
		} else {
			dartingRate = dartingRate * offScreenUpdate;
		}
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
		if (ra.showCameraView()) {		
			float Y = getYCoord(avgY, height);
			drawCircle((float)width/2, (float)Y, 30, 0xFF11FFFF, circleOverlay);
			drawCircle((float)curX, (float)curY, 5, 0xFFFFFF00, circleOverlay);
		}
		ViewGroup vg = (ViewGroup) rootView.findViewById(scrollId);
		if (velocity < 0) {
			vg.scrollBy(0, velocity);
		} else {
			vg.scrollBy(0, velocity * 5);
		}
		velocity = 0;
		
	}
	
	// maps a point into which third of the screen it falls into
	private float getYCoord(double avgY, double height) {
		if (avgY < height/3) {
			return (float)height/4;
		} else if (avgY < 2*height/3) {
			return (float)height/2;
		} else {
			return (float) height/4*3;
		}
	}
	
	// determines what portion of the time in the app has been focused reading
	public double getFocusRate() {
		if (totalTime > 0) {
			return timeReading*1.0/totalTime;
		} else {	
			long curTime = System.currentTimeMillis();
			if (curFocused) {
				return (timeReading + curTime - tempTime)* 1.0/(curTime - startTime);
			} else {
				return timeReading *1.0/(curTime - startTime);
			}
		}
	}
	
	// returns a FocusData object containing all the gathered data. 
	public FocusData getData() {
		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;
		return new FocusData(totalTime, timeReading, getFocusRate(), System.currentTimeMillis(), dartingRate);
	}
	
	public void reset() {
		totalTime = 0;
		timeReading = 0;
		tempTime = -1;
		startTime = -1;
		endTime  = -1;
		curX = 0;
		curY = 0;
		avgX = 0;
		avgY = 0;
		velocity = 0;
		rateOffScreen = 0;
		dartingRate = 0;
	}
	
	// clears the circles on screen
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
	
	// checks if the point passed in falls out of the -1 to 1 range
	private boolean outOfRange(double x, double y) {
		return x < -1 || y < -1 || y > 1 || x > 1;
	}
	
	// maps a -1 to 1 value onto range
	private double interpolateValue(double value, double range) {
		return (value + 1) / 2 * range;
	}
}
