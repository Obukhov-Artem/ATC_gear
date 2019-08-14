package com.example.sapr.gear1;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;


public class UDPHelper extends Thread {

    private BroadcastListener listener;
    private Context ctx;
    private DatagramSocket socket;
    private static final int PORT_IM_IN = 3022;
    private static final int PORT_PC_IN = 3021;
    private static final int PORT_IM_OUT = 3021;

    public UDPHelper(Context ctx, BroadcastListener listener) throws IOException {
        this.listener = listener;
        this.ctx = ctx;
    }

    private float temperature;
    private float pressure;
    private float inner_temp;
    /*
        public void send(String msg) throws IOException {
            DatagramSocket clientSocket = new DatagramSocket();
            clientSocket.setBroadcast(true);
           // Log.d("UDP_out",msg);
            byte[] sendData = msg.getBytes();
            Log.d("UDP_out",String.valueOf(sendData[0])+" "+String.valueOf(sendData[1])+" "+String.valueOf(sendData[2])+" "+String.valueOf(sendData[3]));
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length, getBroadcastAddress(), PORT_IM_IN);
            clientSocket.send(sendPacket);
        }
        */
    public void send(byte[] sendData) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        clientSocket.setBroadcast(true);
        DatagramPacket sendPacket = new DatagramPacket(
                sendData, sendData.length, getBroadcastAddress(), PORT_IM_IN);
        clientSocket.send(sendPacket);
    }
    public void send_pulse(byte[] sendData) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        clientSocket.setBroadcast(true);
        DatagramPacket sendPacket = new DatagramPacket(
                sendData, sendData.length, getBroadcastAddress(), PORT_PC_IN);
        clientSocket.send(sendPacket);
    }
    @Override
    public void run() {
        try {
            socket = new DatagramSocket(PORT_IM_OUT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (!socket.isClosed()) {
            try {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
/*
                temperature =packet.getData()[0];
                pressure =packet.getData()[1];
                inner_temp =packet.getData()[2];*/
                byte[] data = packet.getData();
                //     Log.d("UDP",String.valueOf(temperature)+" "+String.valueOf(damp)+" "+String.valueOf(inner_temp)+" "+String.valueOf(packet.getData()[4]));
                //   Log.d("UDP",String.valueOf(packet.getData()[0])+" "+String.valueOf(packet.getData()[1])+" "+String.valueOf(packet.getData()[2])+" "+String.valueOf(packet.getData()[3])+" "+String.valueOf(packet.getData()[4])+" "+String.valueOf(packet.getData()[5])+" "+String.valueOf(packet.getData()[6])+" "+String.valueOf(packet.getData()[7])+" "+String.valueOf(packet.getData()[8]));
                // Log.d("UDP",String.valueOf(data[0])+" "+String.valueOf(data[1])+" "+String.valueOf(data[2])+" "+String.valueOf(data[3])+" "+String.valueOf(data[4])+" "+String.valueOf(data[5]));
                int temp_arz = (((data[0] & 0xFF) * 256) + (data[1] & 0xC0)) / 64;
                if (temp_arz > 511) { temp_arz -= 1024; }
                temperature = temp_arz * 0.25f;
                Log.d("UDP_temp",String.valueOf(temperature));

                int pressure_raw = ((data[2] & 0xFF) << 8) + (data[3] & 0xFF);

                pressure = (((pressure_raw - 1024) * 500 * 2.0f) / 60000.0f) - 500;

                // float rd_pressure_V = (0.0182f * pressure*pressure + (0.0261f * pressure) - 0.2241f);

                //if (pressure < 0) { rd_pressure_V = -rd_pressure_V; }

                int temp_raw = ((data[4] & 0xFF) << 8) + (data[5] & 0xFF);
                inner_temp = (temp_raw - 10214.0f) / 37.39f;


                Log.d("UDP_pressure",String.valueOf(pressure));
                Log.d("UDP_inner_temp",String.valueOf(inner_temp));
                listener.onReceive(temperature,pressure,inner_temp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket.close();
    }

    public void end() {
        socket.close();
    }

    public interface BroadcastListener {
        public void onReceive(float temp_value, float pressure_value, float inner_temp_value);
    }

    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if(dhcp == null)
            return InetAddress.getByName("255.255.255.255");
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }
}

