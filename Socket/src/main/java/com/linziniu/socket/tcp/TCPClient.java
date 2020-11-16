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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPClient implements CloseListener {

    private final String serverIp;

    private final int serverPort;

    private volatile boolean started = false;

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
        if (!started) {
            receivePool = Executors.newSingleThreadExecutor();
            try {
                socket = new Socket(serverIp, serverPort);
            } catch (IOException e) {
                SocketMessage message = SocketMessage.error(serverIp, serverPort, null, -1, e.getMessage());
                messageListener.onMessageReceive(message);
                closeListener.onClose();
                return;
            }
            handler = new ReceiveHandler(socket, this, messageListener);
            receivePool.submit(handler);
            started = true;
        }

    }

    public synchronized void stop() {
        if (started) {
            if (handler != null) {
                handler.close();
            }
            if (receivePool != null) {
                receivePool.shutdown();
            }
            TCPUtils.close(socket);
//            SocketMessage message = SocketMessage.error(socket, "连接关闭");
//            messageListener.onMessageReceive(message);
            started = false;
        }
    }



    public synchronized void send(String msg) {
        if (started) {
            send(msg.getBytes(StandardCharsets.UTF_8));
        }

    }

    public synchronized void send(byte[] data) {
        if (started) {
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
    }

    @Override
    public void onClose() {
        if (started) {
            if (receivePool != null) {
                receivePool.shutdown();
            }
            TCPUtils.close(socket);
            SocketMessage message = SocketMessage.error(socket, "连接关闭");
            messageListener.onMessageReceive(message);
            closeListener.onClose();
            started = false;
        }
    }

    public static void main(String[] args) throws IOException {
        CloseListener closeListener = () -> {};
        MessageListener messageListener = message -> {
            System.out.printf("[%s]%s:%d -> %s:%d: %s\n",message.isSuccess() ? "成功" : "失败", message.getClientIp(), message.getClientPort(), message.getServerIp(), message.getServerPort(), message.getMsg());
        };
        TCPClient client = new TCPClient("127.0.0.1", 4567, messageListener, closeListener);
        client.start();
        client.send("Hello World!");
        System.in.read();
        client.stop();
        System.out.println("客户端退出");
    }
}
