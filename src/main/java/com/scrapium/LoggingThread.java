package com.scrapium;

import com.scrapium.utils.SLog;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.exit;

public class LoggingThread implements Runnable {

    private Scraper scraper;
    private BlockingQueue<TweetThreadTask> taskQueue;
    private AtomicInteger coroutineCount;
    private int lastRequestCount = 0;
    public AtomicInteger totalRequestCount;
    public LoggingThread(Scraper scraper, BlockingQueue<TweetThreadTask> taskQueue, AtomicInteger coroutineCount) {
        this.scraper = scraper;
        this.taskQueue = taskQueue;
        this.coroutineCount = coroutineCount;
        this.totalRequestCount = new AtomicInteger(0);

    }

    @Override
    public void run() {
        while (true) {
            int delta = this.totalRequestCount.get() - lastRequestCount;

            System.out.println("Requests per second: " + (delta));
            System.out.println("Total Requests: " + this.totalRequestCount);
            System.out.println("Coroutine Count: " + scraper.coroutineCount);


            
            this.lastRequestCount = totalRequestCount.get();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void increaseRequestCount(){
        totalRequestCount.incrementAndGet();
    }

}
