package com.letter.socketassistant.connection;

import java.io.Serializable;

public class ConnectionInfo implements Serializable {
    public static final int CONN_NULL = 0;
    public static final int CONN_TCP_SERVER = 1;
    public static final int CONN_TCP_CLIENT = 2;
    public static final int CONN_UDP = 3;

    private int type;
    private int localPort;
    private String remoteIp;
    private int remotePort;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
}
