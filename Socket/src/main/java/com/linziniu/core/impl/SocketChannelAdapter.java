package com.linziniu.core.impl;

import com.linziniu.core.IOArgs;
import com.linziniu.core.IOProvider;
import com.linziniu.core.Receiver;
import com.linziniu.core.Sender;
import com.linziniu.core.listener.IOEventListener;
import com.linziniu.core.listener.InputSelectCallback;
import com.linziniu.core.listener.OnChannelStatusChangedListener;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketChannelAdapter implements Closeable, Sender, Receiver {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private final SocketChannel channel;

    private final IOProvider provider;

    private IOEventListener sendIOEventListener;

    private IOEventListener receiveIOEventListener;

    private final OnChannelStatusChangedListener onChannelStatusChangedListener;

    private final InputSelectCallback inputSelectCallback = new InputSelectCallback() {
        @Override
        protected void inputReady() {
            if (isClosed.get()) {
                return;
            }

            IOArgs args = new IOArgs();
            if (sendIOEventListener != null) {
                sendIOEventListener.onIOStart(args);
            }

            try {
                if (sendIOEventListener != null && args.read(channel) > 0) {
                    sendIOEventListener.onIOComplete(args);
                } else {
                    throw new IOException("Cannot read any data");
                }
            } catch (IOException ignored) {
                try {
                    SocketChannelAdapter.this.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                onChannelStatusChangedListener.onChannelClosed(channel);
            }
        }
    };

    public SocketChannelAdapter(SocketChannel channel, IOProvider provider, OnChannelStatusChangedListener onChannelStatusChangedListener) {
        this.channel = channel;
        this.provider = provider;
        this.onChannelStatusChangedListener = onChannelStatusChangedListener;

        try {
            channel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean receiveAsync(IOEventListener listener) throws IOException {
        if (isClosed.get()) {
            throw new IOException("Channel closed");
        }
        this.receiveIOEventListener = listener;
        return provider.registerInputAsync(channel, inputSelectCallback);
    }

    @Override
    public boolean sendAsync(IOArgs args, IOEventListener listener) throws IOException {
        if (isClosed.get()) {
            throw new IOException("Channel closed");
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            provider.unRegisterInputAsync(channel);
            provider.unRegisterOutputAsync(channel);
            channel.close();
        }
    }
}
