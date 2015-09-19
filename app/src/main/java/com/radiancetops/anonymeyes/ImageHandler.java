package com.radiancetops.anonymeyes;

import android.hardware.Camera;
import android.util.Log;
import android.widget.TextView;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Sean on 15-08-15.
 */
public class ImageHandler implements Camera.PreviewCallback {

    private int width, height;

    private double[] H, S, L;
    private int[] rgb;

    private double[] Ha, Sa, La, diff;

    private int[] idxs, cols;

    private static double h, s, l, r, g, b;
    private double latitude;
    private double longitude;

    private NetworkThread networkHandler;


    public ImageHandler(int width, int height) {
        super();

        this.width = height;
        this.height = width;

        this.Ha = new double[width];
        this.Sa = new double[width];
        this.La = new double[width];
        this.diff = new double[width];

        this.idxs = new int[4];
        this.cols = new int[4];

        this.networkHandler = new NetworkThread();
        this.networkHandler.start();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.v("test", "Frame");

        latitude = MainActivity.getLatitude();
        longitude = MainActivity.getLongitude();
        //TODO: packet format, Sean
        networkHandler.sendFrame(data);

         camera.addCallbackBuffer(data);
        //camera.autoFocus(null);
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


}
