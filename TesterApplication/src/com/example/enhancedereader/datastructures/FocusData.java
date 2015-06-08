package com.example.enhancedereader.datastructures;

// Immutable object that stores data about the a reading session

public class FocusData {
	public static final String FOCUS_RATE = "FOCUS_RATE";
	public static final String TOTAL_TIME = "TOTAL_TIME";
	public static final String TIME_READING = "TIME_READING";
	public static final String DATE = "DATE";
	public final long totalTime;
	public final long timeReading;
	public final double focusRate;
	public final long date;
	public final double dartingRate;
	
	public FocusData(long totalTime, long timeReading, double focusRate, long date, double dartingRate) {
		this.totalTime = totalTime;
		this.timeReading = timeReading;
		this.focusRate = focusRate;
		this.date = date;
		this.dartingRate = dartingRate;
	}
	
	public String toString() {
		return "Total Time:\t" + totalTime + "\nTime Spent Reading:\t" + timeReading
				+ "\nFocus Rate:\t" + focusRate + "\nDate (in millis):\t" + date
				+ "\nDarting Rate:\t" + dartingRate;
	}
	

}
