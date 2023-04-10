package com.scrapium;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class LoggingThread implements Runnable {

    private Scraper scraper;
    private BlockingQueue<TweetThreadTask> taskQueue;
    private AtomicInteger coroutineCount;
    private int lastRequestCount = 0;
    public AtomicInteger successRequestCount;
    public AtomicInteger failedRequestCount;

    private int lastSuccessCount;
    private int lastFailedCount;
    private long lastLogEpoch;

    public LoggingThread(Scraper scraper, BlockingQueue<TweetThreadTask> taskQueue, AtomicInteger coroutineCount) {
        this.scraper = scraper;
        this.taskQueue = taskQueue;
        this.coroutineCount = coroutineCount;
        this.successRequestCount = new AtomicInteger(0);
        this.failedRequestCount = new AtomicInteger(0);
    }

    @Override
    public void run() {
        while (true) {

            long currentEpoch = System.currentTimeMillis() / 1000;

            int successDelta = this.successRequestCount.get() - this.lastSuccessCount;
            int failedDelta = this.failedRequestCount.get() - this.lastFailedCount;

            double successPS = successDelta / (currentEpoch - this.lastLogEpoch);
            double failedPS = failedDelta / (currentEpoch - this.lastLogEpoch);

            String out = "\n\n=== Tweet Scraper ===\n";
            out += ("Requests : " + (this.successRequestCount.get() + this.failedRequestCount.get())) + "\n";
            out += ("Success/s: " + (successPS)) + "\n";
            out += ("Failed/s: " + (failedPS)) + "\n";

            System.out.println(out);

            this.lastSuccessCount = this.successRequestCount.get();
            this.lastFailedCount = this.failedRequestCount.get();
            this.lastLogEpoch = System.currentTimeMillis() / 1000;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void increaseSuccessRequestCount(){
        successRequestCount.incrementAndGet();
    }

    public void increaseFailedRequestCount(){
        failedRequestCount.incrementAndGet();
    }


}
