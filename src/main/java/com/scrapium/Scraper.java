package com.scrapium;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Scraper {
    private ExecutorService threadPool;
    private static final int NUM_THREADS = 10;

    public Scraper() {
        threadPool = Executors.newFixedThreadPool(NUM_THREADS);
    }

    public void scrape() {
        // Submit tasks to the thread pool using the submit() method
        for (int i = 0; i < 100000000; i++) {
            System.out.println("Pushing task " + i);
            threadPool.submit(new ScraperTask(i));
        }
    }


}
