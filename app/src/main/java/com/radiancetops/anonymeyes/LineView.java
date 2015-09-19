package com.radiancetops.anonymeyes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by neerajen on 15/08/15.
 */
public class LineView extends View {
    private Paint paint;

    private float width = -1;
    private float height = -1;
    private boolean firstTime = true;


    public LineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
    }


    public void setup() {
        //Only ran once when the view is first created
        if (!firstTime)
            return;

        firstTime = false;

        //Sets up the width and height of the gameControl on the screen
        //The gameControl is centered in the screen with a possible border around them
        width = getWidth();
        height = getHeight();

    }//initialisation of the gameboard

    @Override
    public void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        setup();
        paint.setColor(Color.WHITE);
        paint.setAlpha(70);
        canvas.drawRect(0,0,width, height,paint);
        paint.setColor(0);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(4);

        canvas.drawLine(0, 0, width, 0, paint);
        canvas.drawLine(0,height,width,height,paint);
        Log.v("line dims", "" + width + "  " + height);
    }
}
