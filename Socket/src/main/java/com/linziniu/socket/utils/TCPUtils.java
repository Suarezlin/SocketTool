package com.linziniu.socket.utils;

import com.linziniu.socket.model.SocketInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPUtils {

    public static SocketInfo send(Socket socket, String msg) {
        byte[] data = msg.getBytes(StandardCharsets.UTF_8);
        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
            outputStream.write(data);
            outputStream.flush();
            return SocketInfo.success(msg);
        } catch (IOException e) {
            return SocketInfo.error(e.getMessage());
        }
    }

    public static SocketInfo send(Socket socket, byte[] data) {
        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
            outputStream.write(data);
            outputStream.flush();
            return SocketInfo.success("发送成功");
        } catch (IOException e) {
            return SocketInfo.error(e.getMessage());
        }
    }

    public static SocketInfo receive(Socket socket) {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
            byte[] buffer = new byte[512];
            int length = 0;
            StringBuilder stringBuilder = new StringBuilder();
            if ((length = inputStream.read(buffer)) > 0) {
                stringBuilder.append(new String(buffer, 0, length, StandardCharsets.UTF_8));
            } else {
                return null;
            }
            return SocketInfo.success(stringBuilder.toString());
        } catch (IOException e) {
            return SocketInfo.success(e.getMessage());
        }
    }

    public static void close(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

}
