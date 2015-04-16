package com.example.testerapplication.display;

import java.lang.ref.WeakReference;
import java.util.InputMismatchException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.View;

public class BlurDrawable extends Drawable {

	private WeakReference<View> targetRef;
    private Bitmap blurred;
    private Paint paint;
    private int radius;


    public BlurDrawable(View target) {
        this(target, 10);
    }

    public BlurDrawable(View target, int radius) {
        this.targetRef = new WeakReference<View>(target);
        setRadius(radius);
        target.setDrawingCacheEnabled(true);
        target.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
    }

    @Override
    public void draw(Canvas canvas) {
        if (blurred == null) {
            View target = targetRef.get();
            if (target != null) {
                Bitmap bitmap = target.getDrawingCache(true);
                if (bitmap == null) return;
                blurred = Blur.fastBlur(bitmap, radius);
            }
        }
        if (blurred != null && !blurred.isRecycled())
            canvas.drawBitmap(blurred, 0, 0, paint);
    }

    /**
     * Set the bluring radius that will be applied to target view's bitmap
     *
     * @param radius should be 0-100
     */
    public void setRadius(int radius) {
        if (radius < 0 || radius > 100)
            throw new InputMismatchException("Radius must be 0 <= radius <= 100 !");
        this.radius = radius;
        if (blurred != null) {
            blurred.recycle();
            blurred = null;
        }
        invalidateSelf();
    }


    public int getRadius() {
        return radius;
    }

    @Override
    public void setAlpha(int alpha) {
    }


    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
