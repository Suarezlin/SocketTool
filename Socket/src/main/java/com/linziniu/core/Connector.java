package com.linziniu.core;

import com.linziniu.core.impl.SocketChannelAdapter;
import com.linziniu.core.listener.IOEventListener;
import com.linziniu.core.listener.OnChannelStatusChangedListener;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class Connector implements Closeable, OnChannelStatusChangedListener {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private String info;

    private SocketChannel channel;

    private Receiver receiver;

    private Sender sender;

    private final IOEventListener receiveListener = new IOEventListener() {
        @Override
        public void onIOStart(IOArgs args) {

        }

        @Override
        public void onIOComplete(IOArgs args) {
            String msg = args.bufferString();
            System.out.println(info + ":" + msg);
            getMessage();
        }
    };

    private Connector() {}

    public static Connector getConnection(SocketChannel channel) {
        Connector connector = new Connector();
        connector.channel = channel;
        connector.info = channel.socket().getInetAddress() + ":" + channel.socket().getPort();
        SocketChannelAdapter adapter = new SocketChannelAdapter(channel, IOContext.getProvider(), connector);
        connector.sender = adapter;
        connector.receiver = adapter;
        connector.getMessage();
        return connector;
    }

//    public void setup(SocketChannel channel) {
//        this.channel = channel;
//        this.info = channel.socket().getInetAddress() + ":" + channel.socket().getPort();
//
//        SocketChannelAdapter adapter = new SocketChannelAdapter(channel, IOContext.getProvider(), this);
//        sender = adapter;
//        receiver = adapter;
//    }

    private void getMessage() {
        if (receiver != null) {
            try {
                receiver.receiveAsync(receiveListener);
            } catch (IOException e) {
                System.out.println("接收数据异常: " + e.getMessage());
            }
        }
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {

    }

    @Override
    public void close() throws IOException {

    }
}
