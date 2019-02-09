package com.letter.socketassistant.connection;

import android.util.Log;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TcpServerConnection extends Thread {

    private int port;
    private ServerSocket serverSocket;
    private ConnectionReceivedListener connectionReceivedListener;
    private List<TcpAcception> tcpAcceptions;

    private final String TAG = "TCP SERVER";

    public TcpServerConnection (int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            Log.d(TAG, "run: port: " + String.valueOf(port));
            tcpAcceptions = new ArrayList<>();
            while (true) {
                Socket socket = serverSocket.accept();
                Log.d(TAG, "run: connected: " + socket.getInetAddress().toString());
                TcpAcception tcpAcception = new TcpAcception(socket);
                if (connectionReceivedListener != null) {
                    tcpAcception.setConnectionReceivedListener(connectionReceivedListener);
                }
                tcpAcceptions.add(tcpAcception);
                tcpAcception.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setConnectionReceivedListener(ConnectionReceivedListener connectionReceivedListener) {
        this.connectionReceivedListener = connectionReceivedListener;
    }

    /**
     * 写数据至输出流
     * @param index tcp客户端索引
     * @param string 待写入的数据
     */
    public void write(int index, String string) {
        if (index == -1) {
            for (int i = 0; i < tcpAcceptions.size(); i++) {
                tcpAcceptions.get(i).write(string);
            }
        } else {
            TcpAcception tcpAcception = tcpAcceptions.get(index);
            if (tcpAcception != null) {
                tcpAcception.write(string);
            }
        }
    }

    /**
     * 关闭tcp服务器
     */
    public void disconnect() {
        try {
            for (TcpAcception tcpAcception : tcpAcceptions) {
                closeConnection(tcpAcception);
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 断开tcp客户端链接
     * @param tcpAcception tcp客户端连接
     */
    public void closeConnection (TcpAcception tcpAcception) {
        tcpAcceptions.remove(tcpAcception);
        tcpAcception.disconnect();
    }
}
