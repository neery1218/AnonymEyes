package com.radiancetops.anonymeyes;

import android.hardware.Camera;
import android.util.Log;
import android.widget.TextView;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Sean on 15-08-15.
 */
public class ImageHandler implements Camera.PreviewCallback {

    private int width, height, stripheight;

    private double[] H, S, L;
    private int[] rgb;

    private double[] Ha, Sa, La, diff;

    private int[] idxs, cols;

	private static double h, s, l, r, g, b;

    TextView rtv;
    MarkerView markerTextView;

    public ImageHandler(int width, int height, int stripheight, TextView rtv,MarkerView markerView) {
        super();

        this.width = height;
        this.height = width;
        this.stripheight = stripheight;

        this.H = new double[width * stripheight];
        this.S = new double[width * stripheight];
        this.L = new double[width * stripheight];
        this.rgb = new int[width * stripheight];

        this.Ha = new double[width];
        this.Sa = new double[width];
        this.La = new double[width];
        this.diff = new double[width];

        this.idxs = new int[4];
        this.cols = new int[4];
        this.rtv = rtv;

        this.markerTextView = markerView;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.v("test", "test");
        // Decode the image data to HSL
        decodeNV21(data, width, height);

        // Average data
        avgImg();

        // Find the maxima
        findMaxima();

        colors(idxs, rgb);
        validateColors();

        rtv.setText("\n" + resistanceValue(cols[0], cols[1], cols[2], cols[3]) + "\n" + cols[0] + " " + cols[1] + " " + cols[2] + " " + cols[3]);
        markerTextView.setBandLocation(idxs, cols);

        camera.addCallbackBuffer(data);
        //camera.autoFocus(null);
    }

    private void colors(int[] idxs, int[] rgb) {
        WIDTH = width;
        HEIGHT = stripheight;
        rgb1 = new int[WIDTH][HEIGHT];
        output1 = new int[WIDTH][HEIGHT];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < stripheight; j++) {
                rgb1[i][j] = rgb[j * width + i];
            }
        }

        initializeColors();
        normalizeGray();
        avgColorStrip();


        for (int i = 0; i < idxs.length - 1; i++) {
            /* image is reversed due to rotation */
            cols[i] = getResistorColor(rgb1[width - idxs[i] - 1][0]);
        }
        cols[idxs.length - 1] = getGoldSilver(rgb1[width - idxs[idxs.length - 1] - 1][0]);
    }

    private void validateColors() {
        for(int i = 0; i < 3; i++) {
            if(cols[i] == 10) {
                cols[i] = 4;
            }
            if(cols[i] == 11) {
                cols[i] = 8;
            }
        }
        
        if(cols[3] == 2 || cols[3] == 1) {
            cols[3] = 10;
        }
    }

    private  String resistanceValue (int a, int b, int c, int tolerance){
        //gold is ten
        int SILVER = 11;
        //silver is eleven
        int GOLD = 10;


        if (a == 10) a = 1;
        if (b == 10) b = 4;
        if (a == 11) a = 8;
        if (b == 11) b = 8;


        int resistance = (int)((10 * a + b)*Math.pow(10,c));
        String value = "\n" + resistance;

        if (tolerance == 8){
            tolerance = 11;
        }
        else tolerance = 10;

        double mult = 1;

        if(tolerance == GOLD) {
            mult = 0.05;
        } else {
            mult = 0.1;
        }

        value+= " ± " + (int)(mult * resistance) + "Ω\n";
        return value;
    }

    private void findMaxima() {
        int[] midx = new int[4];
        for(int i = 20; i < this.width - 20; i++) {
            boolean nvalid = false;
            for(int j = i - 20; j <= i + 20; j++) {
                if(i == j) continue;
                if(diff[j] >= diff[i]) {
                    nvalid = true;
                    break;
                }
            }

            if(!nvalid) {
                if(diff[i] > diff[midx[3]]) {
                    midx[3] = i;
                    for(int q = 3; q >= 1; q--) {
                        if(diff[midx[q]] > diff[midx[q-1]]) {
                            int tmp = midx[q];
                            midx[q] = midx[q-1];
                            midx[q-1] = tmp;
                        }
                    }
                }
            }
        }

        Log.v("idx", midx[0] + " " + midx[1] + " " + midx[2] + " " + midx[3]);

        for(int i = 0; i < 4; i++) {
            /* the image is reversed due to the rotation */
            idxs[i] = width - midx[i] - 1;
        }

        Arrays.sort(idxs);
    }

    private void avgImg() {
        for(int i = 0; i < width; i++) {
            for (int j = 0; j < stripheight; j++) {
                Ha[i] += H[i + j * width];
                Sa[i] += S[i + j * width];
                La[i] += L[i + j * width];
            }
            Ha[i] /= stripheight;
            Sa[i] /= stripheight;
            La[i] /= stripheight;

            diff[i] = Sa[i] - La[i];
        }
    }

	public void writeCSV () {

        Log.v("idx", idxs[0] + " " + idxs[1] + " " + idxs[2] + " " + idxs[3]);
        /*
        double[] h = new double[width], s = new double[width], l = new double[width];
        int[] rgb = new int[width];
        for(int i = 0; i < width; i++) {
                rgb[i] = this.rgb[i + (stripheight / 2) * this.width];
            h[i] = Ha[i];
            s[i] = Sa[i];
            l[i] = La[i];
        }
        String csv = "";
        Log.v("data", "w: " + width);
        for (int i = 0; i < width; i++) {
            //Log.v("data", h[i] + ","+ s[i]+ "," + l[i]);
            Log.v("rgb", i + "," + (0xff & (rgb[i] >> 16)) + "," + (0xff & (rgb[i] >> 8)) + "," + (0xff & (rgb[i])));
        }
        /*
		try {
			PrintWriter pw = new PrintWriter(new FileWriter("data.csv"));
			for (int i = 0; i < width; i++) {
				pw.println(Ha[i] + ","+ Sa[i]+ "," + La[i]);
			}
			pw.close();
		} catch (IOException e) {}
		*/
    }

    private void decodeNV21(byte[] data, int height, int width) {
        final int frameSize = width * height;

        for (int j = 0; j < this.width; ++j) {
            for (int i = this.height / 2 - stripheight / 2; i < this.height / 2 + stripheight / 2; ++i) {
                int y = (0xff & ((int) data[j * this.height + i]));
                int v = (0xff & ((int) data[frameSize + (j >> 1) * width + (i & ~1) + 0]));
                int u = (0xff & ((int) data[frameSize + (j >> 1) * width + (i & ~1) + 1]));

                int a = (i - (this.height / 2 - stripheight / 2)) * this.width + j;

                int rgb = this.rgb[a] = YUVtoRGB(y, u, v);

                double r = (0xff & (rgb >> 16)) / 255.;
                double g = (0xff & (rgb >>  8)) / 255.;
                double b = (0xff & (rgb >>  0)) / 255.;

                double max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));

                L[a] = ((max + min) / 2);

                if(max == min){
                    H[a] = S[a] = 0; // achromatic
                } else {
                    double d = max - min;
                    S[a] = L[a] > 0.5 ? d / (double) (2 - max - min) : d / (double) (max + min);
                    if (max == r) {
                        H[a] = (g - b) / (double) d + (g < b ? 6 : 0);
                    } else if (max == g) {
                        H[a] = (b - r) / (double) d + 1;
                    } else {
                        H[a] = (r - g) / (double) d + 4;
                    }
                    H[a] /= 6;
                }
            }
        }
    }

    private int YUVtoRGB(int y, int u, int v) {
        y = y < 16 ? 16 : y;

        int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
        int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
        int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

        r = r < 0 ? 0 : (r > 255 ? 255 : r);
        g = g < 0 ? 0 : (g > 255 ? 255 : g);
        b = b < 0 ? 0 : (b > 255 ? 255 : b);

        return 0xff000000 | (r << 16) | (g << 8) | b;
    }


    private static int WIDTH, HEIGHT;
    private static int[][] rgb1;
    private static int[][] output1;
    private static int[] presetRGB = new int[20];
    private static double avgr, avgg, avgb, avgsat;

    private static void initializeColors () {
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
    private static void normalizeSat () {
        avgsat = 0;
        for (int i = 0; i < WIDTH; i++)
            for (int j = 0; j < HEIGHT; j++) {
				toHSL(getRed(rgb1[i][j]), getGreen(rgb1[i][j]), getBlue(rgb1[i][j]));
				avgsat += s;
			}
        avgsat /= HEIGHT * WIDTH;


        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                toHSL(getRed(rgb1[i][j]), getGreen(rgb1[i][j]), getBlue(rgb1[i][j]));
                s = Math.min(1.0, s / avgsat / 2);
                toRGB(h, s, l);
                rgb1[i][j] = rgbToInt((int)r, (int)g, (int)b);
            }
        }
    }

    private static void normalizeGray(){
        double avgR = 0, avgB = 0, avgG = 0;
        for (int i = 0; i<WIDTH; i++){
            for (int j = 0; j<HEIGHT; j++){
                avgR += getRed(rgb1[i][j]);
                avgB += getBlue(rgb1[i][j]);
                avgG += getGreen(rgb1[i][j]);
            }
        }
        avgR /= HEIGHT*WIDTH;
        avgB /= HEIGHT*WIDTH;
        avgG /= HEIGHT*WIDTH;

        for (int i = 0; i<WIDTH; i++){
            for (int j = 0; j<HEIGHT; j++){
                int tr = (int)(getRed(rgb1[i][j])/avgR*128);
                int tg = (int)(getGreen(rgb1[i][j])/avgG*128);
                int tb = (int)(getBlue(rgb1[i][j])/avgB*128);

                rgb1[i][j] = rgbToInt( Math.max(0,Math.min(255,tr)), Math.max(0,Math.min(255,tg)),Math.max(0,Math.min(255,tb)));
            }
        }
    }

    private static void avgColorStrip () {
        for (int i = 0; i < WIDTH; i++) {
            avgr = 0;
            avgg = 0;
            avgb = 0;
            for (int j = 0; j < HEIGHT; j++) {
                avgr += getRed(rgb1[i][j]);
                avgg += getGreen(rgb1[i][j]);
                avgb += getBlue(rgb1[i][j]);
            }
            avgr /= HEIGHT;
            avgg /= HEIGHT;
            avgb /= HEIGHT;
            for (int j = 0; j < HEIGHT; j++)
                rgb1[i][j] = rgbToInt((int)avgr, (int)avgg, (int)avgb);
        }
    }
    private static void replaceColors () {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                rgb1[i][j] = getResistorColor(rgb1[i][j]);
                output1[i][j] = rgb1[i][j];
            }
        }
    }
    private static int getResistorColor (int rgb) {
        r = getRed(rgb);
        g = getGreen(rgb);
        b = getBlue(rgb);
        toHSL(r, g, b);
        // BLACK AND WHITE
        if (l < 0.13) return 0;
        if (l > 0.90) return 9;

        if (Math.max(r, Math.max(g, b)) - Math.min(r,  Math.min(g,b)) < 10){
            return 8;
        }
        if (h > 0.95 || h < 0.093){ // red,orange or brown
            if (((l < 0.32 || s<0.51) && (h>0.01 && h < 0.04)) || ((l<0.29 || s < 0.42) && h>=0.05 && h <= 0.093)) return 1;
            else if ( h>0.9 || h < 0.05) return 2;
            else return 3;
        }
        if (h >= 0.093 && h < 0.21){
            return 4;
        }

        if (h >= 0.21 && h < 0.49)
            return 5;
        if (h >= 0.49 && h < 0.69)
            return 6;
        if (h>=0.69 && h <= 0.95)
            return 7;

        return 12;

    }
    private static int getGoldSilver(int rgb){
        if (Math.max(r, Math.max(g, b)) - Math.min(r,  Math.min(g,b)) < 10){
             return 11;
        }
        return 10;
    }
    // get the R value (0, 255) from a 32 bit integer
    private static int getRed (int n) {
        return 0xFF & (n >> 16);
    }
    // get the G value (0, 255) from a 32 bit integer
    private static int getBlue (int n) {
        return 0xFF & (n >> 0);
    }
    // get the B value (0, 255) from a 32 bit integer
    private static int getGreen (int n) {
        return 0xFF & (n >> 8);
    }
    private static void toHSL (double r, double g, double b) {
        r = r / 255.0; // RED
        g = g / 255.0; // GREEN
        b = b / 255.0; // BLUE
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        h = (max + min) / 2.0;
        s = (max + min) / 2.0;
        l = (max + min) / 2.0;
        if (max == min) {
            h = s = 0;
        } else {
            double d = max - min;
            s = l > 0.5 ? d / (2.0 - max - min) : d / (max + min);
            if (max == r) {
                h = (g - b) / d + (g < b ? 6 : 0);
            } else if (max == g) {
                h = (b - r) / d + 2;
            } else if (max == b) {
                h = (r - g) / d + 4;
            }
            h /= 6.0;
        }
    }
    private static int rgbToInt(int locR, int locG, int locB){
        int a = 255;
        return (((a<<8)+locR<<8)+locG<<8)+locB;
    }
    private static void toRGB (double h, double s, double l) {
        if (s == 0) {
            r = g = b = 1;
        } else {
            double q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            double p = 2 * l - q;
            r = hueToRGB(p, q, (h + 1.0d/3.0d));
            g = hueToRGB(p, q, h);
            b = hueToRGB(p, q, (h - 1.0d/3.0d));
        }
        r = Math.round(r * 255);
		g = Math.round(g * 255);
		b = Math.round(b * 255);
    }
    private static double hueToRGB (double p, double q, double t) {
        if(t < 0.0d) t += 1;
        if(t > 1.0d) t -= 1;
        if(t < 1.0d/6.0d) return p + (q - p) * 6 * t;
        if(t < 1.0d/2.0d) return q;
        if(t < 2.0d/3.0d) return p + (q - p) * (2.0/3.0 - t) * 6;
        return p;
    }
}
