package com.linziniu.socket.model;


public class SocketInfo {

    private boolean status;

    private String msg;

    private SocketInfo(boolean status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public static SocketInfo success(String msg) {
        return new SocketInfo(true, msg);
    }

    public static SocketInfo error(String msg) {
        return new SocketInfo(false, msg);
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
