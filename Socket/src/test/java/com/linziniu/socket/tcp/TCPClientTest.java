package com.linziniu.socket.tcp;


import com.linziniu.socket.listener.CloseListener;
import com.linziniu.socket.listener.MessageListener;
import org.junit.Test;

import java.io.IOException;

public class TCPClientTest {

    @Test
    public void testTCPClient() throws InterruptedException, IOException {
        CloseListener closeListener = () -> {};
        MessageListener messageListener = message -> {
            System.out.printf("[%s]%s:%d -> %s:%d: %s",message.isSuccess() ? "成功" : "失败", message.getClientIp(), message.getClientPort(), message.getServerIp(), message.getServerPort(), message.getMsg());
        };
        TCPClient client = new TCPClient("127.0.0.1", 4567, messageListener, closeListener);
        client.start();
        client.send("Hello World!");
        System.in.read();
    }

}