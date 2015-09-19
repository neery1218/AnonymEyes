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

    private int[] rgb;

    private int[] idxs, cols;

    private double latitude;
    private double longitude;

    private NetworkThread networkHandler;


    public ImageHandler(int width, int height) {
        super();

        this.width = height;
        this.height = width;

        this.idxs = new int[4];
        this.cols = new int[4];

        this.networkHandler = new NetworkThread(width, height, MainActivity.getLongitude(), MainActivity.getLatitude());
        this.networkHandler.start();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.v("test", "Frame");

        latitude = MainActivity.getLatitude();
        longitude = MainActivity.getLongitude();
        //TODO: packet format, Sean
        networkHandler.sendFrame(data, camera);

        //camera.addCallbackBuffer(data);
        //camera.autoFocus(null);
    }



}
