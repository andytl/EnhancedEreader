package com.example.enhancedereader.display;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class CircleView extends View {
	
	float x;
	float y;
	int radius;
	Paint paint;
	
	public CircleView(Context context, float x, float y, int radius, int color) {
		super(context);
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.paint = new Paint();
		paint.setColor(color);
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawCircle(x, y, radius, paint);
		
	}
	
	
}

