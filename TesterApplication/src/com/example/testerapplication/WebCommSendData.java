package com.example.testerapplication;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

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
		try {
			int response = conn.getResponseCode();
			System.out.println(response);
			if (response == 200) {
//				OutputStream request = conn.getOutputStream();
//				request.write(getJSON().toString().getBytes());
//				request.flush();
//				request.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	
}
