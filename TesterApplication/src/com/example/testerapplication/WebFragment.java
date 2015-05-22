package com.example.testerapplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebFragment extends Fragment implements OnTouchListener, CvCameraViewListener2, NewReadCallback{

//	private ReadingMonitor mMonitor;
	private FocusTracker mMonitor;
	private Mat                     mRgba;
	private Mat                     mGray;
	private Mat 					mSquareT;
    public CameraBridgeViewBase   mOpenCvCameraView = null;
    
    private ReaderActivity ra;
    
    private int frameCount;
    
    private CVTaskBuffer<Mat> tasks;
    private EyeTrackerThread trackerThread;
    private boolean validFrame;
	
    public WebFragment() {
    	super();
    	tasks = new CVTaskBuffer<Mat>();
    	validFrame = false;
    	frameCount = 0;
    }
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		ra = (ReaderActivity) activity;
//		mMonitor = new ReadingMonitor(R.id.web_view, R.id.web_circle_overlay, R.id.web_color_overlay, activity);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.web_reader,
				container, false);
		rootView.findViewById(R.id.web_view).setOnTouchListener(this);
		mOpenCvCameraView = (CameraBridgeViewBase) rootView.findViewById(R.id.web_camera_view);
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.setCameraIndex(1);
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		final View rootView = getView();
		final Context context = getActivity();
		mMonitor  = new FocusTracker(ra, rootView);
		WebView wv = (WebView) rootView.findViewById(R.id.web_view);
		wv.setWebViewClient(new WebViewClient());
		wv.loadUrl("https://www.gutenberg.org/files/31547/31547-h/31547-h.htm");		
		rootView.post(new Runnable() {
			@Override
			public void run() {
//				mMonitor.createColorOverlay(rootView, context);
//				createOverlay(rootView, R.id.web_color_overlay);
			}
		});
//		if (((ReaderActivity)getActivity()).connected) {
			enableCameraView();
//		}
		trackerThread = new EyeTrackerThread(ra, this, tasks);
		trackerThread.start();
	}
	
	public void enableCameraView() {
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.enableView();
		}
	}
	
//	private void createOverlay(View rootView, int tableId) {
//		int top = rootView.getTop();
//		int bottom = rootView.getBottom();
//		int left = rootView.getLeft();
//		int right = rootView.getRight();
//		System.out.println("Overlay dims:");
//		System.out.println("\tleft: \t" + left );
//		System.out.println("\tright: \t" + right );
//		System.out.println("\ttop: \t" + top );
//		System.out.println("\tbottom: \t" + bottom );
//		Context context = getActivity();
//		TableLayout table = (TableLayout)rootView.findViewById(tableId);
//		for (int i = 0; i < (bottom -top)/10; i++) {
//			TableRow tr = new TableRow(context);
//			tr.setLayoutParams(new TableRow.LayoutParams(right-left, 10));
//			tr.setBackgroundColor(0x00000000);
//			TextView tv = new TextView(context);
//			tv.setText("");
//			tr.addView(tv);
//			table.addView(tr);
//		}
//	}
	
	// Takes a point between -1 and 1
	public void newReadPosition(double x, double y) {
//		mMonitor.newReadPosition(getView(), x, y);
		mMonitor.newReadPosition(x, y);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
//		int id = v.getId();
//		int action = event.getAction();
//		PointerCoords pc0 = new PointerCoords();
//		event.getPointerCoords(0, pc0);
//		mMonitor.newReadPosition(getView(), pc0.x, pc0.y);
		validFrame = true;
		return false;
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
	    mGray = new Mat();
        mRgba = new Mat();
        mSquareT = new Mat();
	}

	@Override
	public void onCameraViewStopped() {
		if (mGray != null) {
			mGray.release();
		} 
		if (mRgba != null) {
			mRgba.release();	
		}
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		frameCount++;
		mGray = inputFrame.gray();
		Mat square = new Mat(mGray, getCropArea(mGray));
		square = square.clone();
		Mat tempT = square.t();
		Mat squareT = square.t();
		Core.flip(tempT,  squareT, -1);
		
		square.release();
		tempT.release();
		Imgproc.resize(squareT, mGray, mGray.size());
		if (validFrame || frameCount >= 10) {
			validFrame = false;
			tasks.addTask(squareT);
		} else {
			squareT.release();
		}
		frameCount %= 10;
		return mGray;		
	}
	
	private Rect getCropArea(Mat m) {
		int width = m.cols();int height = m.rows();
		if(width > height) {
			int start = (width - height)/2;
			return new Rect(start , 0, height, height);
		} else {
			int start = (height - width) /2;
			return new Rect(0, start, width, width);
		}
	}

	private ReaderActivity getReaderActivity() {
		return (ReaderActivity) getActivity();
	}
	
}
