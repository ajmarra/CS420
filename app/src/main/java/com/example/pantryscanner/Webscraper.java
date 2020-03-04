package com.example.pantryscanner;

import java.util.concurrent.Callable;

// Runnable class that takes in a callable and runs it's call() method
public class Webscraper implements Runnable {
    Callable scrape_instructions;

    public Webscraper(Callable callable) {
        scrape_instructions = callable;
    }
    public void webscrape() throws Exception {
        scrape_instructions.call();
    }

    @Override
    public void run() {
        try {
            webscrape();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}