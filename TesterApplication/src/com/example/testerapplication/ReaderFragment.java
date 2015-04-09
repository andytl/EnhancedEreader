package com.example.testerapplication;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

public class ReaderFragment extends Fragment {

	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_reader,
				container, false);
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		displayText();
	}
	
	private void displayText() {
		ScrollView sv = (ScrollView) getView().findViewById(R.id.scroll_view);
		TextView tv = new TextView(getActivity());
		String text = "";
		for(int i = 0; i < 1000; i++) { 
			text += i + "\n";
		}
		tv.setText(text);
		tv.setTextSize(20);
		sv.addView(tv);
	}
	
}
