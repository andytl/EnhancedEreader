package com.example.testerapplication;

import java.util.Random;

import org.opencv.core.Mat;

public class CVProcessingThread extends Thread implements Runnable {

	private Mat mat;
	private ReaderActivity ra;
	private NewReadCallback nrc;
	private double top;
	private double bottom;
	private double left;
	private double right;

	
	public CVProcessingThread(Mat mat,  ReaderActivity ra, NewReadCallback nrc,
								double top, double bottom, double left, double right) {
		this.mat = mat;
		this.ra = ra;
		this.nrc = nrc;
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
	}
	
	
	@Override
	public void run() {
		// call Sunjays code 
		
		// send result back to UI thread. Shahar will do this
		final Random r = new Random();
//		ra.getHandler().post(new Runnable() {
//			@Override
//			public void run() {
//				nrc.newReadPosition(80, 80);
//			}
//		});
	}

}
