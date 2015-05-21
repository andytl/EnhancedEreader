package com.example.testerapplication;

import android.app.Activity;


public class EyeTrainerThread extends Thread implements Runnable {

	private CVTaskBuffer<MatPoint> tasks;
	//TODO: technically a bug
	private boolean finished;
	private CalibrationState cState;
	private CalibrateFragment cf;
	private Activity activity;
	
	public EyeTrainerThread(CVTaskBuffer<MatPoint> tasks, CalibrationState cState, CalibrateFragment cf, Activity activity) { 
		this.tasks = tasks;
		this.finished = false;
		this.cState = cState;
		this.cf = cf;
		this.activity = activity;
	}
	
	public void finish() {
		finished = true;
	};
	
	@Override
	public void run() {
		while (!finished || !tasks.isEmpty()) {
			MatPoint mp;
			try {
				mp = tasks.getTask();
			} catch (InterruptedException e) {
				continue;
			}
			if (NativeInterface.trainOnFrame(mp.mat, mp.x, mp.y)) {
				cState.advancePosition(mp.positionID);
				if (cState.isComplete()) {
					finish();
				}
				//TODO: run on UI thread, draw circle with new position
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						cf.updateCircle(cState.getCurrentCoordinate());
					}
				});
			}
			if (!finished) {
				cf.validFrame = true;
			}
		}
		NativeInterface.trainNeuralNetwork();
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				((ReaderActivity)activity).enterWebMode();
			}
		});
	}
	
}
