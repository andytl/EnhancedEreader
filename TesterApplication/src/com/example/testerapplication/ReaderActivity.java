package com.example.testerapplication;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class ReaderActivity extends Activity {


	public static final String WEB_MODE = "WEB_MODE";
	public static final String SCROLL_MODE = "SCROLL_MODE";
	public static final String CAMERA_MODE = "CAMERA_MODE";
	public static String CUR_MODE = WEB_MODE;
	
	
	private Handler mHandler = null;
	
	/**********      camera stuff      ***************************/
//	private Mat                    mRgba;
//	private Mat                    mGray;
//    public CameraBridgeViewBase   mOpenCvCameraView = null;
    public boolean connected = false;
    /***********************************************************/

	private BaseLoaderCallback	mLoaderCallback = new BaseLoaderCallback(this) {
			@Override
			public void onManagerConnected(int status) {
					switch (status) {
							case LoaderCallbackInterface.SUCCESS:
							{
									Log.i("TesterApplication", "OpenCV loaded successfully");
									System.loadLibrary("TesterApplication"); 
									// Load native library after(!) OpenCV initialization
									NativeInterface tester = new NativeInterface();
									tester.doFoo();
									System.out.println("JNI Loaded, foo= " + tester.getFoo());
									enableCameraView();
							} break;
							default:
							{
									super.onManagerConnected(status);
							} break;
					}
			}
	};

	@Override
	protected void onResume() {
			super.onResume();
		// Load Opencv
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}
	
	public void enableCameraView() {
		((WebFragment)getFragmentManager().findFragmentByTag(WEB_MODE)).enableCameraView();
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("on create");
		setContentView(R.layout.activity_reader);
		
		if (savedInstanceState == null) {
			FragmentManager fm = getFragmentManager();
			if (CUR_MODE.equals(SCROLL_MODE)) {
				fm.beginTransaction()
					.add(R.id.container, new ReaderFragment(), SCROLL_MODE)
					.commit();
			} else if (CUR_MODE.equals(CAMERA_MODE)) {
				//TODO: camera view
//				setContentView(R.layout.camera_layout);
//				mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
//			    mOpenCvCameraView.setCvCameraViewListener(this);
		      
			} else if (CUR_MODE.equals(WEB_MODE)) {
				getFragmentManager().beginTransaction()
					.add(R.id.container,  new WebFragment(), WEB_MODE)
					.commit();
			}
			mHandler = new Handler(Looper.getMainLooper());
		}
	}
	
	public void newReadPosition(double x, double y) {
		if (CUR_MODE.equals(WEB_MODE)) {
			((WebFragment)getFragmentManager().findFragmentByTag(WEB_MODE)).newReadPosition(x, y);
		}
	}
	
	public Handler getHandler() {
		return mHandler;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.reader, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
}
