package com.example.testerapplication;

import java.util.Set;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class LoginFragment extends Fragment {

	private Set<UserProfile> profiles;
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ReaderActivity ra = (ReaderActivity)getActivity();
		ListView listView = new ListView(ra);
		profiles = ra.getProfiles();
		return listView;
	}
	
}
