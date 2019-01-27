package com.letter.socketassistant.connection;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class TcpClientConnection extends Thread {

    private final int BUFFER_SIZE = 2048;
    private final int PACKET_TIMEOUT = 100;

    private String serverIp;
    private int port;

    private Socket socket;
    private BufferedReader reader;
    private OutputStream outputStream;
    private String line;
    private char[] buffer = new char[BUFFER_SIZE];
    private int index;
    private long time;
    private ConnectionReceivedListener connectionReceivedListener;

    private final String TAG = "TCP CLIENT";

    public TcpClientConnection (String serverIp, int port) {
        this.serverIp = serverIp;
        this.port = port;
    }

    @Override
    public void run() {
        Log.d(TAG, "run: connected start");
        try {
            socket = new Socket(serverIp, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = socket.getOutputStream();
            Log.d(TAG, "TcpClientConnection: Connected");

            while (true) {
                if (reader.ready()) {
                    time = System.currentTimeMillis();
                    while (System.currentTimeMillis() - time < PACKET_TIMEOUT
                            && index < BUFFER_SIZE
                            && reader.ready()) {
                        buffer[index++] = (char)reader.read();
                    }
                    if (connectionReceivedListener != null
                            && index > 0) {
                        line = String.valueOf(buffer, 0, index);
                        Log.d(TAG, "run: packet length: " + String.valueOf(index) + ", data: " + line);
                        connectionReceivedListener.onReceivedListener(line);
                    }
                    index = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (socket != null) {
                    socket.close();
                }
                Log.d(TAG, "TcpClientConnection: Connect failed");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    public void setConnectionReceivedListener(ConnectionReceivedListener connectionReceivedListener) {
        this.connectionReceivedListener = connectionReceivedListener;
    }

    public void write(String string) {
        try {
            outputStream.write(string.getBytes("utf-8"));
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
