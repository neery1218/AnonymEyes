package com.radiancetops.anonymeyes;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by neerajensritharan on 2015-09-19.
 */
public class NetworkThread extends Thread {
    public byte[] inFrame;
    private byte[] buf;

    private DatagramSocket socket;
    private DatagramPacket packet;

    public NetworkThread() {
        try {
            socket = new DatagramSocket();
            //socket.connect(Inet4Address.getByName("104.197.19.22"),52525);
        }
        catch (Exception e){
            Log.v("Socket Connection","connection failed!");
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
            this.buf = this.inFrame;
            this.inFrame = null;
            byte[] buf = new byte[10];
            byte[] test = new byte[10];
            try {
                packet = new DatagramPacket(test, 10, InetAddress.getByName("104.197.19.22"), 52525);
            }
            catch(UnknownHostException e){
                Log.v("Packet","packet creation failed");
            }
            try{
                socket.send(packet);
            }
            catch (Exception e){
                Log.v("Socket", "send failed", e);
            }
        }
    }

    public void sendFrame(byte[] frame) {
        this.inFrame = frame;
        this.interrupt();
    }
}
