package com.example.testerapplication;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.MotionEvent.PointerCoords;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class WebFragment extends Fragment implements OnTouchListener {

	private ReadingMonitor mMonitor;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mMonitor = new ReadingMonitor(R.id.web_view, R.id.web_circle_overlay, R.id.web_color_overlay, activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.web_reader,
				container, false);
		rootView.findViewById(R.id.web_view).setOnTouchListener(this);
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		final View rootView = getView();
		final Context context = getActivity();
		WebView wv = (WebView) rootView.findViewById(R.id.web_view);
		wv.setWebViewClient(new WebViewClient());
		//wv.loadData("<p>asdf</p>", "text/html", null);
		wv.loadUrl("http://www.google.com");
		rootView.post(new Runnable() {
			@Override
			public void run() {
				mMonitor.createColorOverlay(rootView, context);
//				createOverlay(rootView, R.id.web_color_overlay);
			}
		});
	
	}
	
//	private void createOverlay(View rootView, int tableId) {
//		int top = rootView.getTop();
//		int bottom = rootView.getBottom();
//		int left = rootView.getLeft();
//		int right = rootView.getRight();
//		System.out.println("Overlay dims:");
//		System.out.println("\tleft: \t" + left );
//		System.out.println("\tright: \t" + right );
//		System.out.println("\ttop: \t" + top );
//		System.out.println("\tbottom: \t" + bottom );
//		Context context = getActivity();
//		TableLayout table = (TableLayout)rootView.findViewById(tableId);
//		for (int i = 0; i < (bottom -top)/10; i++) {
//			TableRow tr = new TableRow(context);
//			tr.setLayoutParams(new TableRow.LayoutParams(right-left, 10));
//			tr.setBackgroundColor(0x00000000);
//			TextView tv = new TextView(context);
//			tv.setText("");
//			tr.addView(tv);
//			table.addView(tr);
//		}
//	}
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int id = v.getId();
		int action = event.getAction();
		PointerCoords pc0 = new PointerCoords();
		event.getPointerCoords(0, pc0);
		mMonitor.newReadPosition(getView(), pc0.x, pc0.y);
		return true;
	}
}
