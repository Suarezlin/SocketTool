package com.linziniu.controller;


import com.linziniu.model.SocketMessageModel;
import com.linziniu.socket.listener.CloseListener;
import com.linziniu.socket.listener.MessageListener;
import com.linziniu.socket.model.SocketMessage;
import com.linziniu.socket.tcp.TCPClient;
import com.linziniu.utils.HexUtils;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TCPClientPage implements Initializable, CloseListener, MessageListener {

    @FXML
    public VBox parent;

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
    public ToggleButton timedSend;

    @FXML
    public Button switchToByte;

    @FXML
    public Button clear;

    @FXML
    public Button byteSend;

    @FXML
    public ToggleButton byteTimedSend;

    @FXML
    public Button switchToText;

    @FXML
    public Button byteClear;

    @FXML
    public HBox text;

    @FXML
    public VBox bytes;

    @FXML
    public TextArea hexArea;

    @FXML
    public TextArea charArea;

    @FXML
    public TableView<SocketMessageModel> table;

    @FXML
    public VBox areas;

    @FXML
    public Button importButton;

    @FXML
    public Label message;

    @FXML
    public Label textMessage;

    @FXML
    public TextField timePeriod;

    @FXML
    public TextField byteTimePeriod;

    @FXML
    public Button exportButton;

    private TCPClient client;

    private ScheduledExecutorService pool;

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
        timePeriod.disableProperty().bind(isConnected.not());
        byteTimePeriod.disableProperty().bind(isConnected.not());

        clear.setOnAction(event -> clear());
        byteClear.setOnAction(event -> clear());
        importButton.setOnAction(event -> importData());

        hexArea.textProperty().addListener(new HexListener());
        exportButton.setOnAction(event -> export());

        byteSend.setOnAction(event -> byteSend());
        table.setFixedCellSize(50);

        message.setOnMouseClicked(event -> {
            Stage infoStage = new Stage();

            Label label = new Label();
            label.setWrapText(true);
            label.setText(message.getText());

//            System.out.println(message.getText());
            AnchorPane pane = new AnchorPane(label);
//            label.setPrefWidth(400);
//            label.setPrefHeight(300);
            label.setPadding(new Insets(10, 10, 10, 10));
            infoStage.setTitle("详细信息");
            Scene scene = new Scene(pane);
            scene.getStylesheets().add(getClass().getResource("/css/font.css").toExternalForm());
            infoStage.setScene(scene);
            infoStage.show();
        });

//        bytes.widthProperty().addListener(new SizeListener());


        limitNumber(port, true);
        limitNumber(timePeriod, false);
        limitNumber(byteTimePeriod, false);
        text.managedProperty().bind(text.visibleProperty());
        bytes.managedProperty().bind(bytes.visibleProperty());

        timedSend.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    System.out.println("开始发送");

                    pool = Executors.newScheduledThreadPool(1);

                    long period;
                    if (StringUtils.isEmpty(timePeriod.getText())) {
                        period = 1000;
                    } else {
                        period = Long.parseLong(timePeriod.getText());
                    }

                    Runnable runnable = () -> {
                        client.send(msg.getText());
                    };
                    pool.scheduleAtFixedRate(runnable, 0, period, TimeUnit.MILLISECONDS);
                    textMessage.setTextFill(Paint.valueOf("green"));
                    textMessage.setText("开始定时发送，发送间隔 " + period + " 毫秒");
                } else {
                    if (oldValue) {
                        pool.shutdownNow();
                        textMessage.setTextFill(Paint.valueOf("red"));
                        textMessage.setText("停止定时发送");
                    }

                }
            }
        });

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
            boolean status = client.start();

            if (status) {
                textMessage.setTextFill(Paint.valueOf("green"));
                textMessage.setText("连接成功");
            } else {
                textMessage.setTextFill(Paint.valueOf("red"));
                textMessage.setText("连接失败");
            }

            parent.getScene().getWindow().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, ev -> {
                if (isConnected.get()) {
                    client.stop();
                    timedSend.setSelected(false);
                    byteTimedSend.setSelected(false);
                }
            });

        });
        disconnect.setOnAction(event -> {
            client.stop();
            isConnected.set(false);
            timedSend.setSelected(false);
            byteTimedSend.setSelected(false);
//            connect.setDisable(false);
//            disconnect.setDisable(true);
//            send.setDisable(true);
        });
        send.setOnAction(event -> textSend());

        switchToByte.setOnAction(event -> switchTextAndByte());
        switchToText.setOnAction(event -> switchTextAndByte());

        table.setRowFactory(tv -> {
            TableRow<SocketMessageModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    SocketMessageModel model = row.getItem();
                    MessageDetail messageDetail = new MessageDetail(model);
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MessageDetail.fxml"));
                    loader.setController(messageDetail);
                    Stage stage = new Stage();
                    stage.setMinHeight(450);
                    stage.setMinWidth(300);
                    try {
                        Scene scene = new Scene(loader.load());
                        scene.getStylesheets().add(getClass().getResource("/css/light_theme.css").toExternalForm());
                        scene.getStylesheets().add(getClass().getResource("/css/font.css").toExternalForm());
                        stage.setScene(scene);
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    System.out.println(model);
                }
            });
            return row;
        });

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
//                                setTextFill(Paint.valueOf("red"));
                                setStyle("-fx-text-fill: red");
                            } else {
//                                setTextFill(Paint.valueOf("green"));
                                setStyle("-fx-text-fill: green");
                            }
//                            setStyle("-fx-text-fill: white");
                            setText(item);
                        } else {
//                            setTextFill(Paint.valueOf("black"));
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

    private void limitNumber(TextField field, boolean flag) {
        field.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    field.setText(newValue.replaceAll("[^\\d]", ""));
                }
                if (flag) {
                    try {
                        int value = Integer.parseInt(field.getText());
                        if (value <= 0 || value > 65535) {
                            field.setText(oldValue);
                        }
                    } catch (Exception ignored) {}
                }


            }
        });
    }

    @Override
    public void onClose() {
        isConnected.set(false);
        timedSend.setSelected(false);
        byteTimedSend.setSelected(false);
    }

    @Override
    public synchronized void onMessageReceive(SocketMessage message) {
        System.out.printf("[%s]%s:%d -> %s:%d: %s\n",message.isSuccess() ? "成功" : "失败", message.getClientIp(), message.getClientPort(), message.getServerIp(), message.getServerPort(), message.getMsg());
        table.getItems().add(new SocketMessageModel(message));
    }

    public void switchTextAndByte() {
        this.isText = !isText;
        text.setVisible(isText);
        bytes.setVisible(!isText);

//        if (!isText) {
//            String msg = this.msg.getText();
//            hexArea.setText(HexUtils.str2HexStr(msg));
//        } else {
//            String hex = hexArea.getText().replace(" ", "");
//            this.msg.setText(HexUtils.hexStr2Str(hex));
//        }
    }

    public void textSend() {
        client.send(msg.getText());
    }

    public void byteSend() {
        client.send(HexUtils.hexStr2Bytes(hexArea.getText().replace(" ", "")));
    }

    private class HexListener implements ChangeListener<String> {

        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            String pattern = "([0-9a-fA-F]{1,2}\\s)*([0-9a-fA-F]{1,2}\\s?$)?";
            if (!newValue.matches(pattern)) {
                hexArea.setText(oldValue);
            } else {
                String msg = HexUtils.hexStr2Str(hexArea.getText().replace(" ", ""));
                System.out.println(msg);
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < msg.length(); i++) {
                    if (msg.charAt(i) >= 32 && msg.charAt(i) <= 126) {
                        buffer.append(msg.charAt(i));
                    } else {
                        buffer.append('.');
                    }

                    buffer.append("  ");
                }
                charArea.setText(buffer.toString());
            }
        }
    }

    private void clear() {
        table.getItems().clear();
    }

    private void importData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择要导入的文件");

        File file = fileChooser.showOpenDialog(parent.getScene().getWindow());
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = fileInputStream.readAllBytes();
            String msg = HexUtils.byte2HexStr(bytes);
//            System.out.println(msg);
            StringBuilder buffer = new StringBuilder();
            int i;
            for (i = 0; i < msg.length() - 1; i += 2) {
                buffer.append(msg, i, i + 2);
                buffer.append(" ");
            }
            if (i == msg.length() - 1) {
                buffer.append(msg.charAt(msg.length() - 1));
            }
            hexArea.setText(buffer.toString());
            message.setStyle("-fx-text-fill: green; -fx-font-size: 12");
            message.setText("导入文件成功 " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            message.setStyle("-fx-text-fill: red; -fx-font-size: 12");
            message.setText("导入文件失败 " + file.getAbsolutePath() + ", " + e.getMessage());
        }

//        System.out.println(file.getAbsolutePath());
    }

    private void export() {
        String msg = hexArea.getText().replace(" ", "");
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(HexUtils.hexStr2Str(msg));
        clipboard.setContent(content);
        message.setStyle("-fx-text-fill: green; -fx-font-size: 12");
        message.setText("成功导出字符串到剪贴板");
    }

}
