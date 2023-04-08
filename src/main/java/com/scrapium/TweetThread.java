package com.scrapium;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Semaphore;

public class TweetThread implements Runnable {

    private final Scraper scraper;
    private final BlockingQueue<TweetThreadTask> taskQueue;
    private final AtomicInteger coroutineCount;
    private final Semaphore taskSemaphore;

    public TweetThread(Scraper scraper, BlockingQueue<TweetThreadTask> taskQueue, AtomicInteger coroutineCount, Semaphore taskSemaphore) {
        this.scraper = scraper;
        this.taskQueue = taskQueue;
        this.coroutineCount = coroutineCount;
        this.taskSemaphore = taskSemaphore;
    }

    @Override
    public void run() {
        while (true) {
            try {
                taskSemaphore.acquire();

                TweetThreadTask task = taskQueue.take();
                // Perform the task here
                // ...
                //System.out.println("Running Task");
                task.perform();
                coroutineCount.decrementAndGet(); // Decrement coroutineCount after the task is completed

            } catch (InterruptedException e) {
                System.out.println("InterruptedExcepton!");
                throw new RuntimeException(e);
            } finally {
                taskSemaphore.release();
            }
        }
    }
}
