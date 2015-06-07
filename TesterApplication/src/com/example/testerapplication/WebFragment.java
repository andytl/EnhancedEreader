package com.example.testerapplication;

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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import com.example.testerapplication.datastructures.CVTaskBuffer;
import com.example.testerapplication.datastructures.FocusData;
import com.example.testerapplication.datastructures.MatTime;
import com.example.testerapplication.webcommunication.WebCommSendData;

public class WebFragment extends Fragment implements OnTouchListener, OnClickListener, CvCameraViewListener2, NewReadCallback{

//	private ReadingMonitor mMonitor;
	private FocusTracker mMonitor;
	private Mat                     mRgba;
	private Mat                     mGray;
	private Mat mBlack = null;
	
    public CameraBridgeViewBase   mOpenCvCameraView = null;
    
    public static int FRAME_RATE = 0;
    
    private ReaderActivity ra;
    
    private int frameCount;
    
    private CVTaskBuffer<MatTime> tasks;
    private EyeTrackerThread trackerThread;
    private boolean validFrame;
	
    public WebFragment() {
    	super();
    	tasks = new CVTaskBuffer<MatTime>();
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
	
	
	private void registerOnClick(int id, View rootView) {
		Button button = (Button) rootView.findViewById(id);
		button.setOnClickListener(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		final View rootView = getView();
		mMonitor  = new FocusTracker(ra, rootView, R.id.web_view);
		registerOnClick(R.id.go, rootView);
		registerOnClick(R.id.save_data, rootView);
		WebView wv = (WebView) rootView.findViewById(R.id.web_view);
		wv.setWebViewClient(new WebViewClient());
		wv.loadUrl("https://www.gutenberg.org/files/31547/31547-h/31547-h.htm");
//		wv.loadUrl("http://www.madrona.com");
		enableCameraView();
		trackerThread = new EyeTrackerThread(ra, this, tasks);
		trackerThread.start();
	}
	
	public void enableCameraView() {
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.enableView();
		}
	}

	// Takes a point between -1 and 1
	public void newReadPosition(double x, double y) {
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
		if (validFrame || frameCount >= FRAME_RATE) {
			validFrame = false;
			tasks.addTask(new MatTime(squareT, System.currentTimeMillis()));
		} else {
			squareT.release();
		}
		if (FRAME_RATE != 0) {
			frameCount %= FRAME_RATE;
		}
		if (ra.show()) {
			return mGray;
		} else {
			if (mBlack == null) {
				mBlack = Mat.zeros(mGray.size(), 0);
			}
			mGray.release();
			return mBlack;
		}
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
	
	public boolean goBack() {
		WebView webView = (WebView) getView().findViewById(R.id.web_view);
		if (webView.canGoBack()) {
			webView.goBack();
			return true;
		}
		return false;
	}
	
	@Override 
	public void onClick(View v) {
		if (v.getId() == R.id.go) {
			View rootView = getView();
			EditText et = (EditText)rootView.findViewById(R.id.url);
			if (et != null) {
				String url = et.getText().toString() + "";
				hideKeyboard(ra);
				WebView webView = (WebView)rootView.findViewById(R.id.web_view);
				webView.loadUrl(url);
			}
		} else if (v.getId() == R.id.save_data){
			FocusData fd = mMonitor.getData();
			System.err.println(fd);
			
			//Send http POST to the website to save rate with current time
			new WebCommSendData(fd, ra.getUserName(), ra.getPassword()).start();
			
			
			//Reset the monitor
			mMonitor.reset();
		}
	}
	
	public void showCameraView(boolean show) {
		View webView = getView().findViewById(R.id.web_view);
		if (show) {
			webView.setAlpha((float)0.7);
		} else {
			webView.setAlpha(1);
		}
	}
	
	private void hideKeyboard(Activity activity) {
		InputMethodManager imm = (InputMethodManager)activity.getSystemService(
		      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
}
	
}
