package com.linziniu.core;

import com.linziniu.core.listener.IOEventListener;

import java.io.Closeable;
import java.io.IOException;

public interface Sender extends Closeable {

    boolean sendAsync(IOArgs args, IOEventListener listener) throws IOException;

}
