package com.example.testerapplication;

public class CalibrationState {
	private int curRow;
	private int curCol;
	private final int numRow = 21;
	private final int numCol = 46;
	private double width;
	private double height;
	private boolean complete;
	private int position;
	
	public CalibrationState() {
		this(-1, -1);
	}
	
	public CalibrationState(double width, double height) {
		this.width = width;
		this.height = height;
		curRow = 0;
		curCol = 0;
		complete = false;
		position = 0;
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
 		if (this.position > position) {
 			return;
 		}
 		
		if (curRow % 2 == 0) {
			// going to the right
			if (curCol < numCol) {
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
		position++;
		complete = curRow >= numRow;
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public DoublePoint getCurrentCoordinate() {
		return new DoublePoint(width * curCol/numCol, height * curRow/numRow);
	}
	
}
