package com.scrapium;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Scraper {

    private final int maxThreadCount;
    public final int maxCoroutineCount;
    private final ExecutorService threadPool;
    private BlockingQueue<TweetThreadTask> tweetQueue;
    public AtomicInteger coroutineCount = new AtomicInteger(0);
    public Semaphore taskSemaphore;

    public Scraper(int maxThreadCount, int maxCoroutineCount) {
        this.maxThreadCount = maxThreadCount;
        this.maxCoroutineCount = maxCoroutineCount;
        this.taskSemaphore = new Semaphore(maxCoroutineCount);
        this.threadPool = Executors.newFixedThreadPool(maxThreadCount);
        this.tweetQueue = new LinkedBlockingQueue<>();
    }

    public void scrape() {
        this.tweetQueue.add(new TweetThreadTask());
        coroutineCount.incrementAndGet(); // Increment coroutineCount when adding the initial task
        threadPool.submit(new LoggingThread(this, tweetQueue, coroutineCount));
        threadPool.submit(new ProducerThread(this, tweetQueue, coroutineCount));
        for (int i = 0; i < maxThreadCount; i++) {
            threadPool.submit(new TweetThread(this, tweetQueue, coroutineCount, taskSemaphore));
        }
    }
}
