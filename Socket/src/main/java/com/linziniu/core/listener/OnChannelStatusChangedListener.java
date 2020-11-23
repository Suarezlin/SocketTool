package com.linziniu.core.listener;

import java.nio.channels.SocketChannel;

public interface OnChannelStatusChangedListener {

    void onChannelClosed(SocketChannel channel);

}
