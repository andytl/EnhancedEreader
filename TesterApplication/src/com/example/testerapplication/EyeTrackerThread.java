package com.example.testerapplication;

import org.opencv.core.Mat;

public class EyeTrackerThread extends Thread implements Runnable {

	private ReaderActivity ra;
	private NewReadCallback nrc;
	private CVTaskBuffer<MatTime> tasks;
	private static final int MAX_TIME = 500;
	
	public EyeTrackerThread(ReaderActivity ra, NewReadCallback nrc, CVTaskBuffer<MatTime> tasks) {
		this.ra = ra;
		this.nrc = nrc;
		this.tasks = tasks;
	}
	
	@Override
	public void run() {
		Mat mat;
		while (true) {
			try {
				MatTime mt = tasks.getTask();
				if (System.currentTimeMillis() - mt.time > MAX_TIME) {
					System.err.println("skipping mat");
					mt.mat.release();
					continue;
				}
				mat = mt.mat;
			} catch (InterruptedException e) {
				continue;
			}
			final DoublePoint dp = NativeInterface.onNewFrame(mat);
			mat.release();
			System.err.println(dp);
			if (dp.x != 0 && dp.y != 0) {
				// send result back to UI thread. Shahar will do this
					ra.getHandler().post(new Runnable() {
						@Override
						public void run() {
							nrc.newReadPosition(dp.x, dp.y);
						}
					});
				}
		}
	}
	
}
