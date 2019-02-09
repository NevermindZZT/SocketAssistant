package com.letter.socketassistant.connection;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class TcpAcception extends Thread {

    private final int BUFFER_SIZE = 2048;
    private final int PACKET_TIMEOUT = 100;

    private Socket socket;
    private BufferedReader reader;
    private OutputStream outputStream;
    private String line;
    private char[] buffer = new char[BUFFER_SIZE];
    private int index;
    private long time;
    private ConnectionReceivedListener connectionReceivedListener;

    public TcpAcception (Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = socket.getOutputStream();

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
