package com.radiancetops.anonymeyes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by neerajensritharan on 2015-09-19.
 */
public class RecorderView extends View {
    private Paint paint;

    private float width = -1;
    private float height = -1;
    private boolean firstTime = true;

    public RecorderView(Context context, AttributeSet attrs) {
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
        paint = new Paint();
        paint.setColor(Color.RED);
        canvas.drawRect(0,0,20,20,paint);
        canvas.drawCircle(0,0,width/2,paint);
    }


}