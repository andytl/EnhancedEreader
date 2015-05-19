package com.example.testerapplication;


public class EyeTrainerThread extends Thread implements Runnable {

	private CVTaskBuffer<MatPoint> tasks;
	//TODO: technically a bug
	private boolean finished;
	
	public EyeTrainerThread(CVTaskBuffer<MatPoint> tasks) { 
		this.tasks = tasks;
		this.finished = false;
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
			NativeInterface.trainOnFrame(mp.mat, mp.x, mp.y);
		}
	}
	
}
