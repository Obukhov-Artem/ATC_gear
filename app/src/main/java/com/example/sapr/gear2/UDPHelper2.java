package com.example.sapr.gear2;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Calendar;


public class UDPHelper2 extends Thread {

    private BroadcastListener listener;
    private Context ctx;
    private DatagramSocket clientSocket = null;
    private DatagramSocket socket;
    private static final int PORT_IM_IN = 3022;
    private static final int PORT_PC_IN = 3041;
    private static final int PORT_IM_OUT = 3021;//??? проверить получаем ли данные с имитатора
    private static final int PORT_MY1 = 3041;
    //private static final int PORT_MY1 = 3024;
    private static final int PORT_MY2 = 3025;

    public UDPHelper2(Context ctx, BroadcastListener listener) throws IOException {
        this.listener = listener;
        this.ctx = ctx;
        if (clientSocket == null) {
            //clientSocket = new DatagramSocket(PORT_MY2);
            //clientSocket.setBroadcast(true);
            clientSocket = new DatagramSocket(null);
            clientSocket.setReuseAddress(true);
            clientSocket.bind(new InetSocketAddress(PORT_MY2));
            clientSocket.setBroadcast(true);
        }

    }

    private float temperature;
    private float pressure;
    private float inner_temp;
    private int im_damper;
    private int im_temp;
    private int im_max_temp;


    long ms=0;
    long ms_last=0;
    int volume=0;




    @Override
    public void run() {

        try {
            socket = new DatagramSocket(PORT_PC_IN);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        while (!socket.isClosed()) {
            try {

                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                byte[] data2 = packet.getData();
                int pulse = data2[0];
                if (data2[0]<0)
                    pulse = data2[0] & 0xFF;

                Log.d("IMITATOR_pulse", String.valueOf(data2[0])+" - "+String.valueOf(pulse));
                listener.onReceive(pulse);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket.close();

    }

    public void end() {
        socket.close();
        clientSocket.close();
    }

    public boolean status() {
        return socket.isClosed();
    }

    public interface BroadcastListener {
        public void onReceive(int pulse);


    }

    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp == null)
            return InetAddress.getByName("255.255.255.255");
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }
}

