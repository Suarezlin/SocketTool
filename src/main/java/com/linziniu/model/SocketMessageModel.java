package com.linziniu.model;

import com.linziniu.socket.model.SocketMessage;
import javafx.beans.property.SimpleStringProperty;

public class SocketMessageModel {

    private final SimpleStringProperty success;

    private final SimpleStringProperty serverIp;

    private final SimpleStringProperty serverPort;

    private final SimpleStringProperty clientIp;

    private final SimpleStringProperty clientPort;

    private final SimpleStringProperty msg;

    public SocketMessageModel(SocketMessage socketMessage) {
        if (socketMessage.isSuccess()) {
            success = new SimpleStringProperty("成功");
        } else {
            success = new SimpleStringProperty("失败");
        }
        serverIp = new SimpleStringProperty(socketMessage.getServerIp());
        serverPort = new SimpleStringProperty(String.valueOf(socketMessage.getServerPort()));
        clientIp = new SimpleStringProperty(socketMessage.getClientIp());
        clientPort = new SimpleStringProperty(String.valueOf(socketMessage.getClientPort()));
        msg = new SimpleStringProperty(socketMessage.getMsg());
    }

    public String getSuccess() {
        return success.get();
    }

    public SimpleStringProperty successProperty() {
        return success;
    }

    public void setSuccess(String success) {
        this.success.set(success);
    }

    public String getServerIp() {
        return serverIp.get();
    }

    public SimpleStringProperty serverIpProperty() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp.set(serverIp);
    }

    public String getServerPort() {
        return serverPort.get();
    }

    public SimpleStringProperty serverPortProperty() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort.set(serverPort);
    }

    public String getClientIp() {
        return clientIp.get();
    }

    public SimpleStringProperty clientIpProperty() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp.set(clientIp);
    }

    public String getClientPort() {
        return clientPort.get();
    }

    public SimpleStringProperty clientPortProperty() {
        return clientPort;
    }

    public void setClientPort(String clientPort) {
        this.clientPort.set(clientPort);
    }

    public String getMsg() {
        return msg.get();
    }

    public SimpleStringProperty msgProperty() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg.set(msg);
    }
}
