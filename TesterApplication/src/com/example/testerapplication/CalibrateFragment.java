package com.example.testerapplication;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.testerapplication.display.CircleView;

public class CalibrateFragment extends Fragment implements CvCameraViewListener2, OnTouchListener, OnClickListener {

	private Mat                    mGrayT;
	private Mat                    mGray;
    public CameraBridgeViewBase   mOpenCvCameraView = null;
    private double curX;
    private double curY;
	private boolean validFrame = false;
	private ReaderActivity ra;
    private CVTaskBuffer tasks;
    private EyeTrainerThread trainerThread;
    
    public CalibrateFragment() {
    	super();
    	tasks = new CVTaskBuffer();
    }
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	try {
    		this.ra = (ReaderActivity)activity;
    	} catch (ClassCastException e) {
    		e.printStackTrace();
    	}
    }
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.calibrate_fragment, container, false);
    	mOpenCvCameraView = (CameraBridgeViewBase) rootView.findViewById(R.id.web_camera_view);
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.setCameraIndex(1);
		rootView.setOnTouchListener(this);
		Button button = (Button) rootView.findViewById(R.id.complete_calibrate);
		button.setOnClickListener(this);
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (ra.connected) {
			enableCameraView();
		}
		//TODO: figure out if we need to terminate this thread
		trainerThread = new EyeTrainerThread(tasks);
		trainerThread.start();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		trainerThread.finish();
	}
	
	public void enableCameraView() {
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.enableView();
		}
	}
	
	public void disableCameraView() {
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.disableView();
		}
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
	    mGray = new Mat();
        mGrayT = new Mat();		
	}

	@Override
	public void onCameraViewStopped() {
		mGray.release();
		mGrayT.release();		
	}

	private void drawCircle(Context context, View rootView, double x, double y) {
		RelativeLayout rl = (RelativeLayout) rootView.findViewById(R.id.calibrate_circle_overlay);
		CircleView cv = new CircleView(context, (int)x, (int)y, 100, 0xFFFF0000);
		rl.removeAllViews();
		rl.addView(cv);
	}
	
	public double interpolateX(double x, View v) {
		double right = v.getRight();
		double left = v.getLeft();
		return (x/(right-left)) * 2 -1;
	}
	
	public double interpolateY(double y, View v) {
		double bottom = v.getBottom();
		double top = v.getTop();
		return (y/(bottom-top)) * 2 -1;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() != MotionEvent.ACTION_MOVE) {
			drawCircle(ra, v, event.getX(), event.getY());
			curX = interpolateX(event.getX(), v);
			curY = interpolateY(event.getY(), v);
			validFrame = true;
			return true;
		}
		return false;
	}	
	
	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mGray = inputFrame.gray();
		Mat temp1 = mGray.t();
		mGrayT = mGray.t();
		Core.flip(temp1,  mGrayT,  -1);
		Imgproc.resize(mGrayT, mGray, mGray.size());
		temp1.release();
		mGrayT.release();
		if (validFrame) {
			validFrame = false;
			tasks.addTask(new MatPoint(mGray, curX, curY));
			return mGray;
		}
		return null;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.complete_calibrate) {
			NativeInterface.trainNeuralNetwork();
			ra.enterWebMode();
		}
		
	}	
}
