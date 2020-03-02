package com.example.pantryscanner;

import java.util.concurrent.Executor;

public class Invoker implements Executor {
    @Override
    public void execute(Runnable r) {
        new Thread(r).start();
    }
}