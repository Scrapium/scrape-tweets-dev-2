package com.scrapium;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ProducerThread implements Runnable {

    private AtomicInteger coroutineCount;
    private final Scraper scraper;
    private final BlockingQueue<TweetThreadTask> taskQueue;

    public ProducerThread(Scraper scraper, BlockingQueue<TweetThreadTask> taskQueue, AtomicInteger coroutineCount) {
        this.scraper = scraper;
        this.taskQueue = taskQueue;
        this.coroutineCount = coroutineCount;
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("Coroutine count: " + coroutineCount.get());
            if (coroutineCount.get() < scraper.maxCoroutineCount) {
                for (int i = 0; i < scraper.maxCoroutineCount - coroutineCount.get(); i++) {
                    this.taskQueue.add(new TweetThreadTask());
                    coroutineCount.incrementAndGet();
                }
            } else {
                try {
                    //int sleepTime = 10; // Adjust this value based on the current state of the queue or the active coroutines
                    //Thread.sleep(sleepTime);
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}