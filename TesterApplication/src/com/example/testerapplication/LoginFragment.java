package com.example.testerapplication;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class LoginFragment extends Fragment implements OnItemClickListener, OnClickListener {

	private Map<String, UserProfile> profiles;
	private String[] userNames;
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_fragment, container, false);
		Button newUser =  (Button) rootView.findViewById(R.id.new_user);
		newUser.setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		ReaderActivity ra = getReaderActivity();
		ListView userList = (ListView) ra.findViewById(R.id.user_list);
		profiles = ra.getProfiles();
		userNames = new String[profiles.size()];
		Set<String> keySet = profiles.keySet();
		Iterator<String> iter = keySet.iterator();
		int i = 0;
		while (iter.hasNext()) {
			userNames[i] = iter.next();
			i++;
		}
		Arrays.sort(userNames);
		ArrayAdapter<String> adapter = 	new ArrayAdapter<String>(ra, R.layout.user_list_item, userNames);
		userList.setAdapter(adapter);
        userList.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		UserProfile user = profiles.get(userNames[position]);
		if (user != null) {
			final ReaderActivity ra = getReaderActivity();
			ra.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ra.createDialog("Loading...");
					ra.displayDialog();
				}
			});
			hideKeyboard(ra);
			ra.selectUser(user);
			ra.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ra.enterWebMode();
					ra.cancelDialog();
				}
			});
		}
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.new_user) {
			final ReaderActivity ra = getReaderActivity();
			EditText et = (EditText) ra.findViewById(R.id.new_user_name);
			hideKeyboard(ra);
			ra.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ra.createDialog("Loading...");
					ra.displayDialog();
				}
			});
			ra.createNewUser(et.getText().toString() + "");
			ra.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ra.cancelDialog();
				}
			});
		}
	}
	
	private ReaderActivity getReaderActivity() {
		return (ReaderActivity)getActivity();
	}
	
	private void hideKeyboard(Activity activity) {
			InputMethodManager imm = (InputMethodManager)activity.getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
	}
}
