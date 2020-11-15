package com.linziniu.socket.tcp;

import com.linziniu.socket.listener.CloseListener;
import com.linziniu.socket.listener.MessageListener;
import com.linziniu.socket.model.SocketInfo;
import com.linziniu.socket.model.SocketMessage;
import com.linziniu.socket.tcp.handler.ReceiveHandler;
import com.linziniu.socket.utils.TCPUtils;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPClient implements CloseListener {

    private final String serverIp;

    private final int serverPort;

    private volatile boolean status;

    private volatile ExecutorService receivePool;

    private volatile Socket socket;

    private final MessageListener messageListener;

    private final CloseListener closeListener;

    private volatile ReceiveHandler handler;

    public TCPClient(String serverIp, int serverPort, MessageListener messageListener, CloseListener closeListener) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.messageListener = messageListener;
        this.closeListener = closeListener;
    }

    public synchronized void start() {
        receivePool = Executors.newSingleThreadExecutor();
        try {
            socket = new Socket(serverIp, serverPort);
        } catch (IOException e) {
            SocketMessage message = SocketMessage.error(serverIp, serverPort, null, -1, e.getMessage());
            messageListener.onMessageReceive(message);
            return;
        }
        handler = new ReceiveHandler(socket, this, messageListener);
        receivePool.submit(handler);
    }

    public synchronized void stop() {
        if (handler != null) {
            handler.close();
        }
        onClose();
    }



    public synchronized void send(String msg) {
        send(msg.getBytes(StandardCharsets.UTF_8));
    }

    public synchronized void send(byte[] data) {
        SocketInfo info = TCPUtils.send(socket, data);
        if (!info.isStatus()) {
            SocketMessage message = SocketMessage.error(
                    serverIp,
                    serverPort,
                    socket.getInetAddress().toString().substring(1),
                    socket.getPort(),
                    info.getMsg()
            );
            stop();
            messageListener.onMessageReceive(message);
            closeListener.onClose();
        }
    }

    @Override
    public void onClose() {
        if (receivePool != null) {
            receivePool.shutdown();
        }
        TCPUtils.close(socket);
        SocketMessage message = SocketMessage.error(socket, "连接关闭");
        messageListener.onMessageReceive(message);
        closeListener.onClose();
    }
}
