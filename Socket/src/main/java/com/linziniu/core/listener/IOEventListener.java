package com.linziniu.core.listener;

import com.linziniu.core.IOArgs;

public interface IOEventListener {

    void onIOStart(IOArgs args);

    void onIOComplete(IOArgs args);

}
