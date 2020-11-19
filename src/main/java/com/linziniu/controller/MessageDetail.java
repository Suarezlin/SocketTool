package com.linziniu.controller;

import com.linziniu.model.SocketMessageModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Paint;

import java.net.URL;
import java.util.ResourceBundle;

public class MessageDetail implements Initializable {

    @FXML
    public Label status;

    @FXML
    public Label serverIp;

    @FXML
    public Label serverPort;

    @FXML
    public Label clientIp;

    @FXML
    public Label clientPort;

    @FXML
    public TextArea msg;

    private SocketMessageModel model;

    public MessageDetail(SocketMessageModel model) {
        this.model = model;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        msg.setWrapText(true);
        if ("成功".equals(model.getSuccess())) {
            status.setTextFill(Paint.valueOf("green"));
        } else {
            status.setTextFill(Paint.valueOf("red"));
        }
        status.setText(model.getSuccess());
        serverIp.setText(model.getServerIp());
        serverPort.setText(model.getServerPort());
        clientIp.setText(model.getClientIp());
        clientPort.setText(model.getClientPort());
        msg.setText(model.getMsg());
    }
}
