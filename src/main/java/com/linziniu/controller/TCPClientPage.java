package com.linziniu.controller;


import com.linziniu.socket.listener.CloseListener;
import com.linziniu.socket.listener.MessageListener;
import com.linziniu.socket.model.SocketMessage;
import com.linziniu.socket.tcp.TCPClient;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ResourceBundle;

public class TCPClientPage implements Initializable, CloseListener, MessageListener {

    @FXML
    public Button connect;

    @FXML
    public Button disconnect;

    @FXML
    public TextField ip;

    @FXML
    public TextField port;

    @FXML
    public TextField msg;

    @FXML
    public Button send;

    @FXML
    public TableView<SocketMessage> table;

    private TCPClient client;


    @Override
    public void initialize(URL location, ResourceBundle resources) {




        connect.setDisable(false);
        disconnect.setDisable(true);
        send.setDisable(true);
        port.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    port.setText(newValue.replaceAll("[^\\d]", ""));
                }

                try {
                    int value = Integer.parseInt(port.getText());
                    if (value <= 0 || value > 65535) {
                        port.setText(oldValue);
                    }
                } catch (Exception ignored) {}

            }
        });

        connect.setOnAction(event -> {
            System.out.println("新建连接");

            String ip = this.ip.getText();
            if (StringUtils.isEmpty(ip)) {
                System.out.println("ip 不能为空");
                return;
            }
            if (!ip.matches("^((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}$")) {
                System.out.println("ip 地址格式不正确");
                return;
            }
            if (StringUtils.isEmpty(this.port.getText())) {
                System.out.println("端口不能为空");
            }
            int port = Integer.parseInt(this.port.getText());
            client = new TCPClient(ip, port, this, this);
            connect.setDisable(true);
            disconnect.setDisable(false);
            send.setDisable(false);
            client.start();
        });
        disconnect.setOnAction(event -> {
            System.out.println("断开连接");
            client.stop();
            connect.setDisable(false);
            disconnect.setDisable(true);
            send.setDisable(true);
        });
        send.setOnAction(event -> client.send(msg.getText()));
    }

    @Override
    public void onClose() {
        connect.setDisable(false);
        disconnect.setDisable(true);
        send.setDisable(true);
    }

    @Override
    public void onMessageReceive(SocketMessage message) {
        System.out.printf("[%s]%s:%d -> %s:%d: %s\n",message.isSuccess() ? "成功" : "失败", message.getClientIp(), message.getClientPort(), message.getServerIp(), message.getServerPort(), message.getMsg());

    }
}
