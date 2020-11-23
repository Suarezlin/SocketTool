package com.linziniu.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class IOArgs {

    private final byte[] buffer = new byte[1024];
    private final ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

    public int read(SocketChannel channel) throws IOException {
        return channel.read(byteBuffer);
    }

    public int write(SocketChannel channel) throws IOException {
        return channel.write(byteBuffer);
    }

    public String bufferString() {
        return new String(buffer, 0, byteBuffer.position(), StandardCharsets.UTF_8);
    }

}
