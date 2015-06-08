package com.example.enhancedereader.webcommunication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.enhancedereader.ReaderActivity;
import com.example.enhancedereader.datastructures.UserProfile;

public class WebCommTrain extends WebCommBase implements Runnable {

	private UserProfile user;
	private String trainFilename;
	private String netFilename;
	private ReaderActivity ra;
	
	public WebCommTrain(UserProfile user, String trainFilename, String netFilename, ReaderActivity ra) {
		this.user=  user;
		this.trainFilename = trainFilename;
		this.netFilename = netFilename;
		this.ra = ra;
	}
	
	// sends the training data to the server, and receives the neural net 
	@Override
	public void run() {
		byte[] data = getSerializedData();
		if (data != null) {
			String url = "http://attu4.cs.washington.edu:3777/fann/train";
			HttpURLConnection conn = connect(url, "POST", data);
			if (conn == null) {
				return;
			}
			int response = -1;
			try {
				response = conn.getResponseCode();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (response == 200) {
				InputStream in = null;
				try {
					in = conn.getInputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (in != null) {
					String netData = getResponseBody(in, conn.getContentLength());
					saveSerializedData(netData);
					ra.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							ra.selectUser(user);
						}
					});
				}
			}
		
			
		}
	}
	
	private String getResponseBody(InputStream in, long length) {
		String result = "";
		byte[] data = new byte[2048]; 
		int bytesRead = 0;
		while (bytesRead < length) {
			try {
					int bytes = in.read(data);
					bytesRead += bytes;
					result += new String(data, 0, bytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
	
	private byte[] getSerializedData() {
		File file = new File(trainFilename);
		try {
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[(int)file.length()];
			fis.read(data);
			return data;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private void saveSerializedData(String netData) {
		File file = new File(netFilename);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(netData.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private JSONObject getJSON(byte[] data) {		
		try {
			JSONObject jobj = new JSONObject();
			jobj.put("data_serialized", new String(data));
			return jobj;
		} catch(JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
