package com.example.testerapplication;

import android.graphics.Point;

public class FaceCalculation {

	private double headWidthInit;
	private double headHeightInit;
	private double leftEyeWidthInit;
	private double leftEyeHeightInit;
	private double rightEyeWidthInit;
	private double rightEyeHeightInit;
	private Point headPosInit;
	private Point leftEyePosInit;
	private Point rightEyePosInit;
	
	
	public FaceCalculation(double headWidthInit, double headHeightInit, double leftEyeWidthInit, 
						   double leftEyeHeightInit, double rightEyeWidthInit, double rightEyeHeightInit,
						   Point head, Point leftEye, Point rightEye) {
		
		this.headWidthInit = headWidthInit;
		this.headHeightInit = headHeightInit;
		this.leftEyeHeightInit = leftEyeHeightInit;
		this.rightEyeHeightInit = rightEyeHeightInit;
		this.leftEyeWidthInit = leftEyeWidthInit;
		this.rightEyeWidthInit = rightEyeWidthInit;
		this.headPosInit = head;
		this.leftEyePosInit = leftEye;
		this.rightEyePosInit =  rightEye;	
	}
	
	public void newFrame(Point headTL, Point headBR, Point leftTL, 
						 Point leftBR, Point rightTL, Point rightBR) {
		
		
	}
		
	
	
}
