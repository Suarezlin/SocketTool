package com.linziniu.core;

import com.linziniu.core.listener.IOEventListener;

import java.io.Closeable;
import java.io.IOException;

public interface Receiver  extends Closeable {

    boolean receiveAsync(IOEventListener listener) throws IOException;

}
