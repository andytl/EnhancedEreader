package com.example.testerapplication;

import java.util.HashMap;
import java.util.Map;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class ReaderActivity extends Activity {

	public static final boolean VERBOSE = false; 

	public static final String WEB_MODE = "WEB_MODE";
	public static final String LOGIN_MODE = "LOGIN_MODE";
	public static final String CALIBRATE_MODE = "CALIBRATE_MODE";
	
	
	private DbHelper dbHelper;
	private UserProfile currentUser;
	
	private Handler mHandler = null;
	
	/**********      camera stuff      ***************************/
//	private Mat                    mRgba;
//	private Mat                    mGray;
//    public CameraBridgeViewBase   mOpenCvCameraView = null;
    public boolean connected = false;
    /***********************************************************/

    
    /* ************* OpenCv Setup ***************/
    
	private BaseLoaderCallback	mLoaderCallback = new BaseLoaderCallback(this) {
			@Override
			public void onManagerConnected(int status) {
					switch (status) {
							case LoaderCallbackInterface.SUCCESS:
							{
									Log.i("TesterApplication", "OpenCV loaded successfully");
									System.loadLibrary("TesterApplication"); 
									// Load native library after(!) OpenCV initialization
									System.out.println("JNI Loaded");
									enableCameraView();
									connected = true;
							} break;
							default:
							{
									super.onManagerConnected(status);
							} break;
					}
			}
	};	
	
	public void enableCameraView() {
		if (isWebMode()) {
			((WebFragment)getFragmentManager().findFragmentByTag(WEB_MODE)).enableCameraView();
		} else if (isCalibrateMode()) {
			((CalibrateFragment)getFragmentManager().findFragmentByTag(CALIBRATE_MODE)).enableCameraView();
		}
	}
	
	/* ****************************************/
	
	/* ********** Android Lifecycle *****************/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("on create");
		setContentView(R.layout.activity_reader);
		
        dbHelper = new DbHelper(this);
		if (savedInstanceState == null) {
			currentUser = null;
//			
//			getFragmentManager().beginTransaction()
//				.add(R.id.container,  new WebFragment(), WEB_MODE)
//				.commit();
//			
		} else {
			// TODO: get information from savedInstanceState
		}
		mHandler = new Handler(Looper.getMainLooper());

		if (currentUser == null) {
			getFragmentManager().beginTransaction()
				.add(R.id.container,  new LoginFragment(), LOGIN_MODE)
				.commit();
		} else {
			getFragmentManager().beginTransaction()
				.add(R.id.container, new WebFragment(), WEB_MODE)
				.commit();
		}
	}
	
	@Override
	protected void onResume() {
			super.onResume();
		// Load Opencv
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
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
	/* **********************************/
	
	
	
	/* ******  Callbacks ******************/
	public void newReadPosition(double x, double y) {
		((WebFragment)getFragmentManager().findFragmentByTag(WEB_MODE)).newReadPosition(x, y);
	}
	
	public void selectUser(UserProfile user) {
		currentUser = user;
		getFragmentManager().beginTransaction()
			.add(R.id.container, new WebFragment(), WEB_MODE)
			.commit();
	}
	
	public void createNewUser(String userName) {
		UserProfile user = new UserProfile(userName);
		currentUser = user;
		boolean result = dbHelper.addUser(currentUser);
		if (result) {
			getFragmentManager().beginTransaction()
				.replace(R.id.container,  new CalibrateFragment(), CALIBRATE_MODE)
				.commit();
		}
	}
	
	public void enterWebMode() {
		getFragmentManager().beginTransaction()
			.replace(R.id.container, new WebFragment(), WEB_MODE)
			.commit();
	}
	/* ***********************************/
	
	
	/* ******** User Db ******************/

	public Map<String, UserProfile> getProfiles() {
		Map<String, UserProfile> result = new HashMap<String,UserProfile>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		if (!isTableExists(DbHelper.USER_TABLE_NAME, db)) {
			db.execSQL(DbHelper.SQL_CREATE_USERS_TABLE);
		} else {
			db.beginTransaction();
			try {
				String sql = "Select " + DbHelper.USER_ID;
				sql += " from " + DbHelper.USER_TABLE_NAME;
				Cursor cursor = db.rawQuery(sql, null);
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					while (!cursor.isAfterLast()) {
						String userName = cursor.getString(0);
						UserProfile user = new UserProfile(userName);
						result.put(userName, user);
						cursor.moveToNext();
					}
					db.setTransactionSuccessful();
				}
			} finally {
				db.endTransaction();
			}

		}
		return result;
	}
	
	public boolean createProfile(UserProfile user) {
		boolean result = false;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DbHelper.USER_ID, user.getUserName());
		db.beginTransaction();
		try {
			// need to change schema of local database to match new UUID format
			long newRowId = db.insert(DbHelper.USER_TABLE_NAME, null, values);
			if (newRowId == -1) {
				System.err.println("error entering user into database");
			} else {
				db.setTransactionSuccessful();
				result = true;
			}
		} finally {
			db.endTransaction();
		}
		return result;
	}
	
	public boolean removeProfile(UserProfile user) {
		return false;
	}
	
	private boolean isTableExists(String tableName, SQLiteDatabase db) {
	    boolean result = false;
		Cursor cursor = db.rawQuery(
				"select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
	    if (cursor != null) {
	    	if (cursor.getCount() > 0) {
	    		result = true;
	    	}
	    	cursor.close();
	    }
	    return result;
	}
	/* ***************************************/
	
	/* ******* Getters/Setters ****************/

	private boolean isWebMode() {
		return isFragmentMode(WEB_MODE);
	}
	
	private boolean isCalibrateMode() {
		return isFragmentMode(CALIBRATE_MODE);
	}
	
	private boolean isFragmentMode(String fragmentMode) {
		try {
			return getFragmentManager().findFragmentByTag(fragmentMode).isVisible();
		} catch (NullPointerException e) {
			return false;
		}
	}
	
	public Handler getHandler() {
		return mHandler;
	}
	
	/* ****************************************/
	
}
