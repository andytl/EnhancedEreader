package com.example.testerapplication.datastructures;

import java.util.UUID;

public class UserProfile {
	
	// Store what information we need to characterize the 4 corners of the application
	
	// Head Size information
	
	// Eye size information
	
	// User Information
	private String userName;
	private String password;
	
	public UserProfile(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public String getPassword() {
		return password;
	}

}
