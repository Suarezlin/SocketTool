package com.linziniu.core;

import com.linziniu.core.listener.InputSelectCallback;
import com.linziniu.core.listener.OutputSelectCallback;

import java.io.Closeable;
import java.nio.channels.SocketChannel;

public interface IOProvider extends Closeable {

    boolean registerInputAsync(SocketChannel channel, InputSelectCallback callback);

    boolean registerOutputAsync(SocketChannel channel, OutputSelectCallback callback);

    void unRegisterInputAsync(SocketChannel channel);

    void unRegisterOutputAsync(SocketChannel channel);

}
