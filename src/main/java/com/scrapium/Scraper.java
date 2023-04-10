package com.scrapium;

import com.scrapium.utils.SLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Scraper {

    private final int consumerCount;
    public final int maxCoroutineCount;
    private final ExecutorService threadPool;
    private BlockingQueue<TweetThreadTask> tweetQueue;
    public LoggingThread logger;
    private ProducerThread producer;

    private ArrayList<ThreadBase> threads;

    public AtomicInteger coroutineCount = new AtomicInteger(0);



    public Scraper(int consumerCount, int maxCoroutineCount) {
        this.consumerCount = consumerCount;
        this.maxCoroutineCount = maxCoroutineCount;
        this.threadPool = Executors.newFixedThreadPool(consumerCount + 2);
        this.tweetQueue = new LinkedBlockingQueue<>();
        this.threads = new ArrayList<ThreadBase>();
    }

    public void scrape() {

        this.logger = new LoggingThread(this, tweetQueue, coroutineCount);
        threads.add(this.logger);
        threadPool.submit(this.logger);

        this.producer = new ProducerThread(this, tweetQueue, coroutineCount);
        threads.add(this.producer);
        threadPool.submit(this.producer);

        for (int i = 0; i < consumerCount; i++) {
            SLog.log("Scraper: Created consumer thread.");
            TweetThread tweetThread = new TweetThread(this, tweetQueue, coroutineCount);
            threads.add(tweetThread);
            threadPool.submit(tweetThread);
        }
    }

    public void stop() {
        for (Iterator<ThreadBase> iterator = threads.iterator(); iterator.hasNext(); ) {
            ThreadBase item = iterator.next();
            item.running = false;
            // do something with the item
        }

        try {
            System.out.println("Attempting to shutdown thread pool...");
            threadPool.shutdown();
            threadPool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Thread pool termination interrupted.");
        } finally {
            if (!threadPool.isTerminated()) {
                System.err.println("Forcing thread pool shutdown...");
                threadPool.shutdownNow();
                try {
                    threadPool.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("Thread pool shutdown complete.");
        }
    }
}
