package com.example.sapr.gear1;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class ClientSendAndListen implements Runnable {
    private int portVal = 10101;
    private String ipVal = "127.0.0.1";

        @Override
        public void run() {
            boolean run = true;
            try {
                DatagramSocket udpSocket = new DatagramSocket(portVal);
                InetAddress serverAddr = InetAddress.getByName(ipVal);
                byte[] buf = ("FILES").getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length,serverAddr, portVal);
                udpSocket.send(packet);
                while (run) {
                    try {
                        byte[] message = new byte[8000];
                        packet = new DatagramPacket(message,message.length);
                        Log.i("UDP client: ", "about to wait to receive");
                        udpSocket.setSoTimeout(10000);
                        udpSocket.receive(packet);
                        String text = new String(message, 0, packet.getLength());
                        Log.d("Received text", text);
                    } catch (IOException e) {
                        Log.e("UDP has IOException", "error: ", e);
                        run = false;
                        udpSocket.close();
                    }
                }
            } catch (SocketException e) {
                Log.e("Socket Open:", "Error:", e);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}
