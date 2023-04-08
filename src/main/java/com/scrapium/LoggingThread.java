package com.scrapium;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class LoggingThread implements Runnable {
    private Scraper scraper;
    private BlockingQueue<TweetThreadTask> taskQueue;
    private AtomicInteger coroutineCount;

    public LoggingThread(Scraper scraper, BlockingQueue<TweetThreadTask> taskQueue, AtomicInteger coroutineCount) {
        this.scraper = scraper;
        this.taskQueue = taskQueue;
        this.coroutineCount = coroutineCount;
    }

    @Override
    public void run() {
        while (true) {

            System.out.println("Coroutine Count: " + scraper.coroutineCount);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
