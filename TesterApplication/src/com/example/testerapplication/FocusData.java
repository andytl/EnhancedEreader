package com.example.testerapplication;

import org.json.JSONException;
import org.json.JSONObject;

public class FocusData {
	public static final String FOCUS_RATE = "FOCUS_RATE";
	public static final String TOTAL_TIME = "TOTAL_TIME";
	public static final String TIME_READING = "TIME_READING";
	public static final String DATE = "DATE";
	public long totalTime;
	public long timeReading;
	public double focusRate;
	public long date;
	
	public FocusData(long totalTime, long timeReading, double focusRate, long date) {
		this.totalTime = totalTime;
		this.timeReading = timeReading;
		this.focusRate = focusRate;
		this.date = date;
	}
	
	public String toString() {
		return "Total Time:\t" + totalTime + "\nTime Spent Reading:\t" + timeReading
				+ "\nFocus Rate:\t" + focusRate + "\nDate (in millis):\t" + date;
	}
	

}
