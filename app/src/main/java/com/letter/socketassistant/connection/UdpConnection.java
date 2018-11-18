package com.letter.socketassistant.connection;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpConnection extends Thread {

    private String remoteIp;
    private int remotePort;
    private int localPort;
    private InetAddress remoteAddress;
    private DatagramSocket datagramSocket;
    private DatagramPacket packetTransmit;
    private DatagramPacket packetReceived;
    private ConnectionReceivedListener connectionReceivedListener;

    private byte[] data = new byte[1024];

    public UdpConnection (String remoteIp, int remotePort, int localPort) {
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        this.localPort = localPort;
        try {
            remoteAddress = InetAddress.getByName(remoteIp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            datagramSocket = new DatagramSocket(localPort);

            packetReceived = new DatagramPacket(data, data.length);

            while (true) {
                datagramSocket.receive(packetReceived);
                if (connectionReceivedListener != null) {
                    connectionReceivedListener.onReceivedListener(new String(data, 0, packetReceived.getLength()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (datagramSocket != null) {
                    datagramSocket.close();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    public void setConnectionReceivedListener(ConnectionReceivedListener connectionReceivedListener) {
        this.connectionReceivedListener = connectionReceivedListener;
    }

    public void write (String string) {
        try {
            packetTransmit = new DatagramPacket(string.getBytes(), string.length(), remoteAddress, remotePort);
            datagramSocket.send(packetTransmit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect () {
        try {
            if (datagramSocket != null) {
                datagramSocket.close();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
