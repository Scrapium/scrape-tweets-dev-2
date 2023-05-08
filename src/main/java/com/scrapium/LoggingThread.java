package com.scrapium;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class LoggingThread extends ThreadBase implements Runnable {

    private Scraper scraper;
    private BlockingQueue<TweetTask> taskQueue;
    private AtomicInteger coroutineCount;
    private int lastRequestCount = 0;
    public AtomicInteger successRequestCount;
    public AtomicInteger failedRequestCount;

    private long startEpoch;
    private int lastSuccessCount;
    private int lastFailedCount;
    private long lastLogEpoch;

    public LoggingThread(Scraper scraper, BlockingQueue<TweetTask> taskQueue) {
        this.scraper = scraper;
        this.taskQueue = taskQueue;
        this.successRequestCount = new AtomicInteger(0);
        this.failedRequestCount = new AtomicInteger(0);
        this.startEpoch = System.currentTimeMillis() / 1000;
    }

    @Override
    public void run() {
        while (this.running) {

            long currentEpoch = System.currentTimeMillis() / 1000;

            int successDelta = this.successRequestCount.get() - this.lastSuccessCount;
            int failedDelta = this.failedRequestCount.get() - this.lastFailedCount;

            double successPS = successDelta / (currentEpoch - this.lastLogEpoch);
            double failedPS = failedDelta / (currentEpoch - this.lastLogEpoch);

            int secondSinceStart = (int) (currentEpoch - this.startEpoch);

            double successPSTotal = this.successRequestCount.get() / (secondSinceStart == 0 ? 1 : secondSinceStart);

            String out = "\n\n=== Tweet Scraper ===\n";
            out += ("Requests : " + (this.successRequestCount.get() + this.failedRequestCount.get())) + "\n";
            out += ("Success/s: " + (successPS)) + "\n";
            out += ("Success Total/s: " + (successPSTotal)) + "\n";
            out += ("Failed/s: " + (failedPS)) + "\n";

            System.out.println(out);

            this.lastSuccessCount = this.successRequestCount.get();
            this.lastFailedCount = this.failedRequestCount.get();
            this.lastLogEpoch = System.currentTimeMillis() / 1000;


            scraper.proxyService.syncAndRefresh();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
