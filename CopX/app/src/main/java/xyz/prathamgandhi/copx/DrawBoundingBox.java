/*
This is a utility view used for creation of a bounding box around the faces detected by MLKit
 */

package xyz.prathamgandhi.copx;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DrawBoundingBox extends View {

    private final Paint paint = new Paint();
    RectF rect = new RectF();

    public DrawBoundingBox(Context context) {
        super(context);
    }

    public DrawBoundingBox(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }


    // Here the drawing takes place
    // Canvas draws while the Paint defines the style of drawing
    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(getResources().getColor(R.color.teal_200));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawRect(rect, paint);

        super.onDraw(canvas);
    }
}
