package com.scrapium;

import com.scrapium.utils.DebugLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Scraper {

    public long conSocketTimeout;
    private int consumerCount;
    public int maxCoroutineCount;

    private final ExecutorService threadPool;
    public BlockingQueue<TweetTask> tweetQueue;

    //public AtomicInteger coroutineCount;
    public LoggingThread logger;
    private ProducerThread producer;

    private ArrayList<ThreadBase> threads;

    // the number of coroutines currently running



    public Scraper(int consumerCount, int maxCoroutineCount, int conSocketTimeout) {

        this.consumerCount = consumerCount;
        this.maxCoroutineCount = maxCoroutineCount;
        this.conSocketTimeout = conSocketTimeout;

        this.threadPool = Executors.newFixedThreadPool(consumerCount + 2);
        this.tweetQueue = new LinkedBlockingQueue<>();
        this.threads = new ArrayList<ThreadBase>();


        // Handle the SIGINT signal (CTRL + C)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gracefully...");
            this.stop();
        }));

        String osName = System.getProperty("os.name");
        if (!osName.toLowerCase().contains("windows")) {
            // the environment is not Windows

            // Handle the SIGTSTP signal (CTRL + Z)
            CustomSignalHandler.handleTSTPSignal(() -> {
                this.stop();
                System.out.println("SIGTSTP signal received!");
                System.exit(0);
            });
        }

    }

    public void scrape() {

        this.logger = new LoggingThread(this, tweetQueue);
        threads.add(this.logger);
        threadPool.submit(this.logger);

        this.producer = new ProducerThread(this, tweetQueue);
        threads.add(this.producer);
        threadPool.submit(this.producer);

        for (int i = 0; i < consumerCount; i++) {
            DebugLogger.log("Scraper: Created consumer thread.");
            TweetThread tweetThread = new TweetThread(this, tweetQueue);
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
            e.printStackTrace();
            System.err.println("Thread pool termination interrupted.");
        } finally {
            if (!threadPool.isTerminated()) {
                System.err.println("Forcing thread pool shutdown...");
                threadPool.shutdownNow();
                try {
                    threadPool.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            System.out.println("Thread pool shutdown complete.");
        }
    }
}
