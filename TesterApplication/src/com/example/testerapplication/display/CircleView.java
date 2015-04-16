package com.example.testerapplication.display;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class CircleView extends View {
	
	int x;
	int y;
	int radius;
	Paint paint;
	
	public CircleView(Context context, int x, int y, int radius) {
		super(context);
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.paint = new Paint();
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawCircle(x, y, radius, paint);
		
	}
	
}

