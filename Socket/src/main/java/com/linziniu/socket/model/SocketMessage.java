package com.linziniu.socket.model;

import java.net.Socket;

public class SocketMessage {

    private boolean success;

    private String serverIp;

    private int serverPort;

    private String clientIp;

    private int clientPort;

    private String msg;

    private SocketMessage(boolean success, String serverIp, int serverPort, String clientIp, int clientPort, String msg) {
        this.success = success;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.clientIp = clientIp;
        this.clientPort = clientPort;
        this.msg = msg;
    }

    public static SocketMessage success(String serverIp, int serverPort, String clientIp, int clientPort, String msg) {
        return new SocketMessage(true, serverIp, serverPort, clientIp, clientPort, msg);
    }

    public static SocketMessage error(String serverIp, int serverPort, String clientIp, int clientPort, String msg) {
        return new SocketMessage(false, serverIp, serverPort, clientIp, clientPort, msg);
    }

    public static SocketMessage success(Socket socket, String msg) {
        SocketMessage message = SocketMessage.success(
                socket.getInetAddress().toString().substring(1),
                socket.getPort(),
                socket.getLocalAddress().toString().substring(1),
                socket.getLocalPort(),
                msg
        );
        return message;
    }

    public static SocketMessage error(Socket socket, String msg) {
        SocketMessage message = SocketMessage.error(
                socket.getInetAddress().toString().substring(1),
                socket.getPort(),
                socket.getLocalAddress().toString().substring(1),
                socket.getLocalPort(),
                msg
        );
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
