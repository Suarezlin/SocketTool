package com.linziniu.socket.listener;

import com.linziniu.socket.model.SocketMessage;

public interface MessageListener {
    void onMessageReceive(SocketMessage socketMessage);
}
