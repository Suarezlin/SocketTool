package com.linziniu.core;

import java.io.IOException;

public class IOContext {

    private static IOContext INSTANCE;

    private static IOProvider provider;

    private IOContext() {}

    public static void setup(IOProvider ioProvider) {
        provider = ioProvider;
    }

    public static IOProvider getProvider() {
        return provider;
    }

    public static void close() {
        try {
            provider.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
