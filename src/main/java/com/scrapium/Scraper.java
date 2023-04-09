package com.scrapium;

import com.scrapium.utils.SLog;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Scraper {

    private final int consumerCount;
    public final int maxCoroutineCount;
    private final ExecutorService threadPool;
    private BlockingQueue<TweetThreadTask> tweetQueue;
    public LoggingThread logger;

    public AtomicInteger coroutineCount = new AtomicInteger(0);

    public Scraper(int consumerCount, int maxCoroutineCount) {
        this.consumerCount = consumerCount;
        this.maxCoroutineCount = maxCoroutineCount;
        this.threadPool = Executors.newFixedThreadPool(consumerCount + 2);
        this.tweetQueue = new LinkedBlockingQueue<>();
    }

    public void scrape() {
        this.logger = new LoggingThread(this, tweetQueue, coroutineCount);
        threadPool.submit(this.logger);
        threadPool.submit(new ProducerThread(this, tweetQueue, coroutineCount));
        for (int i = 0; i < consumerCount; i++) {
            SLog.log("Scraper: Created consumer thread.");
            threadPool.submit(new TweetThread(this, tweetQueue, coroutineCount));
        }
    }
}
