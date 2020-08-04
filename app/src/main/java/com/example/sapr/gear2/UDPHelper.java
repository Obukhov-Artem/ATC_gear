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
import java.net.UnknownHostException;
import java.util.Calendar;


public class UDPHelper extends Thread {

    private BroadcastListener listener;
    private Context ctx;
    private DatagramSocket clientSocket = null;
    private DatagramSocket socket2;
    private DatagramSocket socket;
    private static final int PORT_IM_IN = 3022;
    private static final int PORT_PC_IN = 3041;
    private static final int PORT_IM_OUT = 3021;//??? проверить получаем ли данные с имитатора
    private static final int PORT_MY1 = 3041;
    //private static final int PORT_MY1 = 3024;
    private static final int PORT_MY2 = 3025;

    public UDPHelper(Context ctx, BroadcastListener listener) throws IOException {
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

    public void send(byte[] sendData) throws IOException {

        DatagramPacket sendPacket = new DatagramPacket(
                sendData, sendData.length, getBroadcastAddress(), PORT_IM_IN);
        clientSocket.send(sendPacket);
    }

    public void send_pulse(byte[] sendData) throws IOException {

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
                byte[] data = packet.getData();
                int status = data[0];
                //Log.d("STATUS", String.valueOf(status));
                if (status == 0) {

                    int temp_arz = (((data[1] & 0xFF) * 256) + (data[2] & 0xC0)) / 64;
                    if (temp_arz > 511) {
                        temp_arz -= 1024;
                    }
                    temperature = temp_arz * 0.25f;
                    int pressure_raw = ((data[3] & 0xFF) << 8) + (data[4] & 0xFF);

                    pressure = (((pressure_raw - 1024) * 1000.0f) / 60000.0f) - 500;
                    float k_pressure = 0.5f;
                    pressure = k_pressure*pressure;
                    int temp_raw = ((data[5] & 0xFF) << 8) + (data[6] & 0xFF);
                    inner_temp = (temp_raw - 10214.0f) / 37.39f;
                    //Log.d("UDP_temperature",String.valueOf(temperature));
                    //Log.d("UDP_pressure",String.valueOf(pressure));
                    //Log.d("UDP_inner_temp",String.valueOf(inner_temp));

                    if(ms==0 && ms_last == 0){
                        Calendar c = Calendar.getInstance();
                        ms = c.getTimeInMillis();
                        ms_last = c.getTimeInMillis();
                        volume = 0;
                    }
                    else{
                        Calendar c = Calendar.getInstance();
                        ms = c.getTimeInMillis();
                        volume = (int)(ms- ms_last);
                        //Log.d("ms",String.valueOf(ms));
                        //Log.d("ms2",String.valueOf(ms_last));
                        ms_last = ms;
                    }

                    listener.onReceive(status, temperature, pressure, inner_temp, 0, 0, 0,volume);


                } else {
                    im_temp = data[1];
                    im_damper = data[2];
                    im_max_temp = data[3];

                    Log.d("IMITATOR_temp", String.valueOf(im_temp));
                    Log.d("IMITATOR_damper", String.valueOf(im_damper));
                    Log.d("IMITATOR_max_temp", String.valueOf(im_max_temp));
                    listener.onReceive(status, 0, 0, 0, im_temp, im_damper, im_max_temp,0);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket.close();
        try {
            socket2 = new DatagramSocket(PORT_PC_IN);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (!socket2.isClosed()) {
            try {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket2.receive(packet);
                byte[] data = packet.getData();
                int pulse = data[0];
                listener.onReceivePulse(pulse);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket2.close();
    }

    public void end() {
        socket.close();
        socket2.close();
        clientSocket.close();
    }

    public boolean status() {
        return socket.isClosed();
    }

    public interface BroadcastListener {
        public void onReceive(int status, float temp_value, float pressure_value, float inner_temp_value, int im_temp, int im_damper, int im_max_temp,int volume);

        public void onReceivePulse(int pulse);

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

