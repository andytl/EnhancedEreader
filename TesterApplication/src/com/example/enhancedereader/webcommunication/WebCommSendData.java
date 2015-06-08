package com.example.enhancedereader.webcommunication;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.enhancedereader.datastructures.FocusData;

public class WebCommSendData extends WebCommBase implements Runnable {

	private FocusData fd;
	private String username;
	private String password;
	
	public WebCommSendData(FocusData fd, String username, String password) {
		this.fd = fd;
		this.username = username;
		this.password = password;
	}
	
	@Override
	public void run() {
		String url = "http://attu4.cs.washington.edu:3777/api/entry";
		HttpURLConnection conn = connect(url, "POST", getJSON().toString());
		if (conn != null) {
			try {
				int response = conn.getResponseCode();
				System.out.println(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private JSONObject getJSON() {
		JSONObject result = null;
		try {
			result = new JSONObject();
			result.put("username", username);
			result.put("password", password);
			result.put("focusrate", fd.focusRate);
			result.put("totaltime", (int)fd.totalTime);
			result.put("timereading", (int)fd.timeReading);
			result.put("timestamp", fd.date);
			result.put("dartingrate", fd.dartingRate);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	
}
