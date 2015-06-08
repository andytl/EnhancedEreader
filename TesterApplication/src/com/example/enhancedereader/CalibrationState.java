package com.example.enhancedereader;

import com.example.enhancedereader.datastructures.DoublePoint;

public class CalibrationState {
	private int curRow;
	private int curCol;
	private final int numRows = 15;
	private final int numCols = 35;
	private double width;
	private double height;
	private boolean complete;
	private String phase;
	private static String rows = "ROWS";
	private static String cols = "COLS";
	private int position;
	private static int FRAMES_PER_POSITION = 1;
	private int curFrames;
	
	public CalibrationState() {
		this(-1, -1);
	}
	
	public CalibrationState(double width, double height) {
		this.width = width;
		this.height = height;
		phase = rows;
		curRow = 0;
		curCol = 0;
		complete = false;
		position = 0;
		curFrames = 0;
	}
	
	public boolean initialized() {
		return width != -1 && height != -1;
	}
	
	public void setDimensions(double x, double y) {
		width = x; 
		height = y;
	}
	
	public int getPositionID() {
		return position;
	}
	
	// called everytime there is a successful scan
	public void advancePosition(int position) {	
		curFrames++;
 		if (this.position > position) {
 			return;
 		}
 		if (curFrames < FRAMES_PER_POSITION) {
 			return;
 		}
 		curFrames = 0;
 		if (phase.equals(rows)) {
			if (curRow % 2 == 0) {
				// going to the right
				if (curCol < numCols) {
					curCol++;
				} else {
					curRow++;
				}			
			} else {
				// going to the left
				if (curCol > 0) {
					curCol--;
				} else {
					curRow++;
				}
			}
			this.position++;
			System.err.println(position);
//			uncomment to loop twice
//			if (position == numRows * numCols) {
//				curCol = 0; 
//				curRow = 0;
//			}
			complete = curRow >= numRows;
 		}
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public DoublePoint getCurrentCoordinate() {
		return new DoublePoint(width * curCol/numCols, height * curRow/numRows);
	}
	
}
