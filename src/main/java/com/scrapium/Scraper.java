package com.scrapium;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Scraper {

    private final ExecutorService threadPool;
    public static final int maxCoroutineCount = 1;
    private ConcurrentLinkedQueue<TweetThreadTask> tweetQueue;
    private final int maxThreadCount;
    //private final ProducerThread producer;

    public AtomicInteger coroutineCount = new AtomicInteger(0);

    public Scraper(int maxThreadCount) {
        this.maxThreadCount = maxThreadCount;
        this.threadPool = Executors.newFixedThreadPool(maxThreadCount);
        this.tweetQueue = new ConcurrentLinkedQueue<>();
        //this.producer = new ProducerThread(this, tweetQueue, coroutineCount);
    }

    public void scrape() {
        this.tweetQueue.add(new TweetThreadTask());
        //this.producer.start();
        threadPool.submit(new ProducerThread(this, tweetQueue, coroutineCount));
        for (int i = 0; i < maxThreadCount; i++) {
            threadPool.submit(new TweetThread(this, tweetQueue, coroutineCount));
        }
    }
}