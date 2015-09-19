package com.radiancetops.anonymeyes;

import android.hardware.Camera;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * Created by neerajensritharan on 2015-09-19.
 */
public class NetworkThread extends Thread {
    public byte[] inFrame;
    private byte[] buf;
    private long id;

    //private byte[] sendBuf = new byte[65000];
    private byte[] []sendBuf;

    private double longitude, latitude;

    private int width, height;
    private int frameNum;

    private DatagramSocket socket;
    private DatagramPacket packet;

    private Camera camera;

    public NetworkThread(int width, int height, double longitude, double latitude) {
        Log.v("NetworkThread", "starting network managing thread with params " + width + "," + height + "," + longitude + "," + latitude);
        this.longitude = longitude;
        this.latitude = latitude;
        this.width = width;
        this.height = height;
        frameNum = 0;
        sendBuf = new byte [height][512];
        this.id = (new Random().nextLong());
        try {
            socket = new DatagramSocket();
            //socket.connect(Inet4Address.getByName("104.197.19.22"),52525);
        }
        catch (Exception e){
            Log.v("Socket Connection","connection failed!");
        }
        for (int i = 0; i < height; i++)
            prepareSendBuf(i);
    }

    private void prepareSendBuf(int row) {
        longToBytes(sendBuf[row], id, 0);
        long l = Double.doubleToLongBits(latitude);
        longToBytes(sendBuf[row], l, 8);
        l = Double.doubleToLongBits(longitude);
        longToBytes(sendBuf[row], l, 16);
        sendBuf[row][24] = (byte) ((width & 0xff00) >> 8);
        sendBuf[row][25] = (byte) ((width & 0x00ff) >> 0);
        sendBuf[row][26] = (byte) ((height & 0xff00) >> 8);
        sendBuf[row][27] = (byte) ((height & 0x00ff) >> 0);

        sendBuf[row][28] = (byte) ((row & 0xff00) >> 8);
        sendBuf[row][29] = (byte) ((row & 0x00ff) >> 0);

    }

    private void longToBytes(byte[] b, long v, int idx) {
        for(int i = 0; i < 8; i++) {
            b[i + idx] = (byte) ((v >> (8 * (7 - i))) & 0xff);
        }
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {

            }

            if(this.inFrame == null) {
                continue;
            }
            Log.v("NetworkThread", "processing frame");
            this.buf = this.inFrame;
            this.inFrame = null;

            encodeImg();
            this.camera.addCallbackBuffer(buf);

            try {
                for (int i = 0; i < height; i++) {
                    packet = new DatagramPacket(sendBuf[i], sendBuf[i].length/*28+(1+3*width*height)/2*/, InetAddress.getByName("104.197.49.2"), 52525);
                    socket.send(packet);
                    Log.v("NetworkThread", "created packet of size " + packet.getLength());
                }
                //packet = new DatagramPacket(new byte[10], 10, InetAddress.getByName("104.197.49.2"), 52525);

            }
            catch(UnknownHostException e){
                Log.v("Packet","packet creation failed");
            }
            catch (IOException e){
                Log.v("Socket", "send failed", e);
            }
        }
    }

    private void encodeImg() {
        decodeNV21(buf, width, height);
    }

    public void sendFrame(byte[] frame, Camera camera) {
        this.camera = camera;
        this.inFrame = frame;
        addFrameNumber();
        this.interrupt();
        frameNum++;
    }

    private void addFrameNumber (){//bytes 30-33 are frame number
        Log.v("Frame",""+frameNum);
        for (int i = 0; i < height; i++){
            sendBuf[i][30] = (byte) ((frameNum & 0xff000000) >> 24);
            sendBuf[i][31] = (byte) ((frameNum & 0x00ff0000) >> 16);
            sendBuf[i][32] = (byte) ((frameNum & 0x0000ff00) >> 8);
            sendBuf[i][33] = (byte) ((frameNum & 0x000000ff) >> 0);


        }
    }

    private void decodeNV21(byte[] data, int width, int height) {
        final int frameSize = width * height;
        int a = 0;

        for (int j = 0; j < height; ++j) {
            int idx = 34;
            int offset = 0;
            for (int i = 0; i < width; ++i) {

                int y = (0xff & ((int) data[i * width + j]));
                int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
                int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);
                r >>= 4;
                g >>= 4;
                b >>= 4;

                //argb[a++] = 0xff000000 | (r << 16) | (g << 8) | b;
                sendBuf[j][idx] |= (b << offset);
                offset ^= 4; if(offset == 0) { idx++; sendBuf[j][idx] = 0; }
                sendBuf[j][idx] |= (g << offset);
                offset ^= 4; if(offset == 0) { idx++; sendBuf[j][idx] = 0; }
                sendBuf[j][idx] |= (r << offset);
                offset ^= 4; if(offset == 0) { idx++; sendBuf[j][idx] = 0; }
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
}
