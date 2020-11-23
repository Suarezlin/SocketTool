package com.linziniu.core.listener;

public abstract class OutputSelectCallback implements Runnable {

    private Object attach;

    @Override
    public void run() {
        outputReady(attach);
    }

    public void setAttach(Object attach) {
        this.attach = attach;
    }

    protected abstract void outputReady(Object attach);
}
