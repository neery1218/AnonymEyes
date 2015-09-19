package com.radiancetops.anonymeyes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by neerajen on 18/08/15.
 */
public class MarkerView extends View {
    private Paint paint;

    private float width = -1;
    private float height = -1;
    private boolean firstTime = true;
    private ArrayList<Integer> bandLocation;
    private ArrayList<Integer> colorIndexes;
    private int [] presetRGB;


    public MarkerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();

        firstTime = true;
        width = -1;

        bandLocation = new ArrayList<Integer>();
        colorIndexes = new ArrayList<Integer>();

        presetRGB = new int [12];

        presetRGB[0] = rgbToInt(0,0,0);
        presetRGB[1] = rgbToInt(102, 51, 50);
        presetRGB[2] = rgbToInt(255,0,0);
        presetRGB[3] = rgbToInt(255, 102, 0);
        presetRGB[4] = rgbToInt(255, 255, 0);
        presetRGB[5] = rgbToInt(0, 255, 0);
        presetRGB[6] = rgbToInt(0, 0, 255);
        presetRGB[7] = rgbToInt(206, 101, 255);
        presetRGB[8] = rgbToInt(130, 130, 130);
        presetRGB[9] = rgbToInt(255, 255, 255);
        presetRGB[10] = rgbToInt(205, 153, 51);
        presetRGB[11] = rgbToInt(204, 204, 204);

    }

    private int rgbToInt(int locR, int locG, int locB){
        int a = 255;
        return (((a<<8)+locR<<8)+locG<<8)+locB;
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

    public void setBandLocation (int [] bandLocation, int [] colorIndexes){
        this.bandLocation = new ArrayList<Integer>();
        this.colorIndexes = new ArrayList<Integer>();
        for (int i = 0; i < bandLocation.length; i++){
            this.bandLocation.add(bandLocation[i]);
            this.colorIndexes.add(colorIndexes[i]);
        }
        Log.v("band size",""+bandLocation.length);
        invalidate();
    }
    @Override
    public void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        setup();

        paint.setStrokeWidth(6);
        for (int i = 0; i < bandLocation.size(); i++){
            paint.setColor(presetRGB[colorIndexes.get(i)]);
            canvas.drawLine(bandLocation.get(i), 0, bandLocation.get(i), height, paint);
            Log.v("band location", "" + bandLocation.get(i));
            Log.v("color Index",""+colorIndexes.get(i));
        }


        //paint.setColor(Color.BLACK);
       // canvas.drawLine(0, 0, width, 0, paint);
        //canvas.drawLine(0, height, width, height, paint);
        Log.v("band dims", "" + width + "  " + height);



    }
}