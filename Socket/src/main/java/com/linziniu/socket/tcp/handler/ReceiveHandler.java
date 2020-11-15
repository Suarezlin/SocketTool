package com.linziniu.socket.tcp.handler;

import com.linziniu.socket.listener.CloseListener;
import com.linziniu.socket.listener.MessageListener;
import com.linziniu.socket.model.SocketInfo;
import com.linziniu.socket.model.SocketMessage;
import com.linziniu.socket.utils.TCPUtils;

import java.io.*;
import java.net.Socket;

public class ReceiveHandler implements Runnable {

    private final Socket socket;

    private final CloseListener closeListener;

    private final MessageListener messageListener;

    private volatile boolean done = false;

    public ReceiveHandler(Socket socket, CloseListener closeListener, MessageListener messageListener) {
        this.socket = socket;
        this.closeListener = closeListener;
        this.messageListener = messageListener;
    }

    public void close() {
        done = true;
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        closeListener.onClose();
    }

    @Override
    public void run() {

        while (!done) {
            SocketInfo info = TCPUtils.receive(socket);
            if (info == null) {
                closeListener.onClose();
            } else if (info.isStatus()) {
                SocketMessage message = SocketMessage.success(socket, info.getMsg());
                messageListener.onMessageReceive(message);
            } else {
//                SocketMessage message = SocketMessage.error(socket, info.getMsg());
                close();
//                messageListener.onMessageReceive(message);
                closeListener.onClose();
            }
        }
    }
}
