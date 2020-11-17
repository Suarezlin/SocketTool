package com.linziniu.controller;


import com.linziniu.model.SocketMessageModel;
import com.linziniu.socket.listener.CloseListener;
import com.linziniu.socket.listener.MessageListener;
import com.linziniu.socket.model.SocketMessage;
import com.linziniu.socket.tcp.TCPClient;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
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
    public TextArea msg;

    @FXML
    public Button send;

    @FXML
    public Button timedSend;

    @FXML
    public Button switchToByte;

    @FXML
    public Button clear;

    @FXML
    public Button byteSend;

    @FXML
    public Button byteTimedSend;

    @FXML
    public Button switchToText;

    @FXML
    public Button byteClear;

    @FXML
    public HBox text;

    @FXML
    public HBox bytes;

    @FXML
    public TextArea hexArea;

    @FXML
    public TextArea charArea;

    @FXML
    public TableView<SocketMessageModel> table;

    private TCPClient client;

    private boolean isText = true;

    private final SimpleBooleanProperty isConnected = new SimpleBooleanProperty(false);


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        connect.disableProperty().bind(isConnected);
        disconnect.disableProperty().bind(isConnected.not());
        send.disableProperty().bind(isConnected.not());
        byteSend.disableProperty().bind(isConnected.not());
        timedSend.disableProperty().bind(isConnected.not());
        byteTimedSend.disableProperty().bind(isConnected.not());

        hexArea.textProperty().addListener(new HexListener());

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
        text.managedProperty().bind(text.visibleProperty());
        bytes.managedProperty().bind(bytes.visibleProperty());

        bytes.setVisible(false);

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
            isConnected.set(true);
//            connect.setDisable(true);
//            disconnect.setDisable(false);
//            send.setDisable(false);
            client.start();
        });
        disconnect.setOnAction(event -> {
            System.out.println("断开连接");
            client.stop();
            isConnected.set(false);
//            connect.setDisable(false);
//            disconnect.setDisable(true);
//            send.setDisable(true);
        });
        send.setOnAction(event -> textSend());

        switchToByte.setOnAction(event -> switchTextAndByte());
        switchToText.setOnAction(event -> switchTextAndByte());

        List<String> columns = Arrays.asList("success", "serverIp", "serverPort", "clientIp", "clientPort", "msg");

        TableColumn<SocketMessageModel, String> col1 = new TableColumn<>("状态");
        col1.setMaxWidth(80);
        col1.setMinWidth(80);
        col1.setResizable(false);
        col1.setCellValueFactory(new PropertyValueFactory<>("success"));
        col1.setCellFactory(new Callback<TableColumn<SocketMessageModel, String>, TableCell<SocketMessageModel, String>>() {
            @Override
            public TableCell<SocketMessageModel, String> call(TableColumn<SocketMessageModel, String> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        if (!empty) {
                            if ("失败".equals(item)) {
                                setStyle("-fx-background-color: red; -fx-text-fill: white");
                            } else {
                                setStyle("-fx-background-color: #00ca00; -fx-text-fill: white");
                            }
//                            setStyle("-fx-text-fill: white");
                            setText(item);
                        } else {
                            setStyle("-fx-background-color: white; -fx-text-fill: black");
                            setText("");
                        }
                    }
                };
            }
        });

        TableColumn col2 = new TableColumn("服务器");
        col2.setResizable(false);
        TableColumn col3 = new TableColumn("IP");
        col3.setMaxWidth(120);
        col3.setMinWidth(120);
        col3.setResizable(false);
        col3.setCellValueFactory(new PropertyValueFactory<>("serverIp"));
        TableColumn col4 = new TableColumn("端口");
        col4.setMaxWidth(80);
        col4.setMinWidth(80);
        col4.setResizable(false);
        col4.setCellValueFactory(new PropertyValueFactory<>("serverPort"));
        col2.getColumns().addAll(col3, col4);

        TableColumn col5 = new TableColumn("客户端");
        col5.setResizable(false);
        TableColumn col6 = new TableColumn("IP");
        col6.setMaxWidth(120);
        col6.setMinWidth(120);
        col6.setResizable(false);
        col6.setCellValueFactory(new PropertyValueFactory<>("clientIp"));
        TableColumn col7 = new TableColumn("端口");
        col7.setMaxWidth(80);
        col7.setMinWidth(80);
        col7.setResizable(false);
        col7.setCellValueFactory(new PropertyValueFactory<>("clientPort"));
        col5.getColumns().addAll(col6, col7);

        TableColumn col8 = new TableColumn("消息");
        col8.setCellValueFactory(new PropertyValueFactory<>("msg"));

        table.getColumns().setAll(col1, col2, col5, col8);


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
        table.getItems().add(new SocketMessageModel(message));
    }

    public void switchTextAndByte() {
        this.isText = !isText;
        text.setVisible(isText);
        bytes.setVisible(!isText);
    }

    public void textSend() {
        client.send(msg.getText());
    }

    private class HexListener implements ChangeListener<String> {

        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//            char input = newValue.charAt(newValue.length() - 1);

//            if ((input >= 'a' && input <= 'f')) {
//                newValue = newValue.substring(0, newValue.length() - 1) + (char)(input + ('A' - 'a'));
//                hexArea.setText(newValue);
//            }


            String pattern = "([0-9a-fA-F]{1,2}\\s)*([0-9a-fA-F]{1,2}\\s?$)?";
            if (!newValue.matches(pattern)) {
                hexArea.setText(oldValue);
            }

//            if (oldValue.equals(newValue)) {
//                return;
//            }
//
//            System.out.printf("[%s] [%s]\n", oldValue, newValue);
//
//            if (oldValue.length() > newValue.length()) {
//                int i;
//                for (i = 0; i < newValue.length(); i++) {
//                    if (newValue.charAt(i) != oldValue.charAt(i)) {
//                        break;
//                    }
//                }
//                char input = oldValue.charAt(i);
//                System.out.println(input);
//                if (oldValue.charAt(i) == ' ') {
//                    newValue = oldValue.substring(0, i-1) + oldValue.substring(i+1);
////                    System.out.println(newValue);
//                }
//            } else {
//                int i;
//                for (i = 0; i < oldValue.length(); i++) {
//                    if (newValue.charAt(i) != oldValue.charAt(i)) {
//                        break;
//                    }
//                }
////                if (i == oldValue.length()) {
////                    return;
////                }
//                char input = newValue.charAt(i);
//                if ((input >= '0' && input <= '9') || (input >= 'a' && input <= 'f') || (input >= 'A' && input <= 'F') || input == ' ') {
//
//                } else {
//                    hexArea.setText(oldValue);
//                    return;
//                }
//            }
//
//
//
//            StringBuffer buffer = new StringBuffer();
//            String temp = newValue.replace(" ", "");
//            int i;
//            for (i = 0; i < temp.length() - 1; i += 2) {
//                buffer.append(temp, i, i + 2);
//                buffer.append(" ");
//            }
//
//            if (i == temp.length() - 1) {
//                buffer.append(temp.charAt(temp.length() - 1));
//            }
////            System.out.printf("[%s] [%s] [%s]\n", oldValue, newValue, buffer.toString());
////            if (newValue.equals(buffer.toString()) || oldValue.equals(buffer.toString())) {
////                return;
////            }
//            hexArea.setText(buffer.toString().trim());
//        }
        }
    }


}
