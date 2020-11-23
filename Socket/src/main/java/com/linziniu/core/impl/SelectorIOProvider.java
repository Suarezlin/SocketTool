package com.linziniu.core.impl;

import com.linziniu.core.IOProvider;
import com.linziniu.core.listener.InputSelectCallback;
import com.linziniu.core.listener.OutputSelectCallback;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectorIOProvider implements IOProvider {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private final AtomicBoolean registerInputLock = new AtomicBoolean(false);

    private final AtomicBoolean registerOutputLock = new AtomicBoolean(false);

    private final Selector inputSelector;

    private final Selector outputSelector;

    private final Map<SelectionKey, Runnable> inputCallbackMap = new HashMap<>();

    private final Map<SelectionKey, Runnable> outputCallbackMap = new HashMap<>();

    private final ExecutorService inputHandlerPool = Executors.newFixedThreadPool(8, new HandlerThreadFactory("Input-Handler-Thread"));

    private final ExecutorService outputHandlerPool = Executors.newFixedThreadPool(8, new HandlerThreadFactory("Output-Handler-Thread"));

    public SelectorIOProvider() throws IOException {
        this.inputSelector = Selector.open();
        this.outputSelector = Selector.open();
        selectInput();
        selectOutput();
    }

    private void selectInput() {
        Thread thread = new Thread("Input-Selector") {
            @Override
            public void run() {
                while (!isClosed.get()) {
                    try {
                        if (inputSelector.select() == 0) {
                            waitRegistration(registerInputLock);
                            continue;
                        }
                        Set<SelectionKey> selectionKeys = inputSelector.selectedKeys();
                        for (SelectionKey key : selectionKeys) {
                            if (key.isReadable()) {
                                handleSelection(key, SelectionKey.OP_READ, inputCallbackMap, inputHandlerPool);
                            }
                        }
                        selectionKeys.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        thread.start();
    }

    private void selectOutput() {
        Thread thread = new Thread("Output-Selector") {
            @Override
            public void run() {
                while (!isClosed.get()) {
                    try {
                        if (outputSelector.select() == 0) {
                            waitRegistration(registerOutputLock);
                            continue;
                        }
                        Set<SelectionKey> selectionKeys = outputSelector.selectedKeys();
                        for (SelectionKey key : selectionKeys) {
                            if (key.isReadable()) {
                                handleSelection(key, SelectionKey.OP_WRITE, outputCallbackMap, outputHandlerPool);
                            }
                        }
                        selectionKeys.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        thread.start();
    }

    private static void handleSelection(SelectionKey key, int selectOps, Map<SelectionKey, Runnable> callbackMap, ExecutorService pool) {
        key.interestOps(key.readyOps() & ~selectOps);
        Runnable callback = callbackMap.get(key);
        if (callback != null && !pool.isShutdown()) {
            pool.execute(callback);
        }
    }

    @Override
    public boolean registerInputAsync(SocketChannel channel, InputSelectCallback callback) {
        SelectionKey key = handleRegistration(channel, inputSelector, SelectionKey.OP_READ, callback, inputCallbackMap, registerInputLock);
        return key != null;
    }

    @Override
    public boolean registerOutputAsync(SocketChannel channel, OutputSelectCallback callback) {
        SelectionKey key = handleRegistration(channel, outputSelector, SelectionKey.OP_WRITE, callback, outputCallbackMap, registerOutputLock);
        return key != null;
    }

    @Override
    public void unRegisterInputAsync(SocketChannel channel) {
        SelectionKey key = channel.keyFor(inputSelector);
        if (key != null) {
            key.cancel();
            inputCallbackMap.remove(key);
        }
    }

    @Override
    public void unRegisterOutputAsync(SocketChannel channel) {
        SelectionKey key = channel.keyFor(outputSelector);
        if (key != null) {
            key.cancel();
            outputCallbackMap.remove(key);
        }
    }

    private void waitRegistration(AtomicBoolean lock) {
        synchronized (lock) {
            if (lock.get()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static SelectionKey handleRegistration(SocketChannel channel, Selector selector, int selectOps, Runnable callback, Map<SelectionKey, Runnable> map, AtomicBoolean lock) {
        synchronized (lock) {
            lock.set(true);
            try {
                selector.wakeup();
                SelectionKey key = null;
                if (channel.isRegistered()) {
                    key = channel.keyFor(selector);
                    if (key != null) {
                        map.put(key, callback);
                    }
                }
                if (key == null) {
                    key = channel.register(selector, selectOps);
                    map.put(key, callback);
                }
                return key;
            } catch (ClosedChannelException ignored) {
                return null;
            } finally {
                lock.set(false);
                lock.notify();
            }
        }
    }


    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            inputCallbackMap.clear();
            outputCallbackMap.clear();
            inputHandlerPool.shutdown();
            outputHandlerPool.shutdown();
            inputSelector.wakeup();
            inputSelector.close();
            outputSelector.wakeup();
            outputSelector.close();
        }
    }

    private static class HandlerThreadFactory implements ThreadFactory {

        private final String prefix;

        private final AtomicInteger threadNum = new AtomicInteger(1);

        public HandlerThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, prefix + "-" + threadNum.getAndIncrement());
        }
    }
}
