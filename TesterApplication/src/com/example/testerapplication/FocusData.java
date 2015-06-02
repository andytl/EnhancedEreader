package com.example.testerapplication;

public class FocusData {
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
