package com.linziniu.core.listener;

public abstract class InputSelectCallback implements Runnable {

    @Override
    public void run() {
        inputReady();
    }

    protected abstract void inputReady();
}
