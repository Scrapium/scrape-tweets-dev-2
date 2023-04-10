package com.scrapium;

import com.scrapium.utils.DebugLogger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TweetThread  extends ThreadBase implements Runnable{

    private final Scraper scraper;
    private final BlockingQueue<TweetTask> taskQueue;
    private AtomicInteger coroutineCount;

    private final TweetThreadTaskProcessor taskProcessor;

    // possibly move maxCoroutineCount to scraper so it doesn't need to be updated in each class - blocking.

    public TweetThread(Scraper scraper, BlockingQueue<TweetTask> taskQueue) {
        this.scraper = scraper;
        this.taskQueue = taskQueue;
        this.coroutineCount = new AtomicInteger(0);
        this.taskProcessor = new TweetThreadTaskProcessor(this.scraper, this.taskQueue, this.coroutineCount);
    }

    @Override
    public void run() {
        while (this.running) {
            try {
                if (this.taskQueue.size() > 0 && this.coroutineCount.get() < scraper.maxCoroutineCount) {
                    DebugLogger.log("TweetThread: Ran cycle");
                    DebugLogger.log("TweetThread: Task Taken");
                    this.taskProcessor.processNextTask();
                    DebugLogger.log("Decrementing counter");

                } else {

                    if(this.taskQueue.size() == 0){
                        DebugLogger.log("Skipping thread execution!");
                        DebugLogger.log("  Reason: QUEUE EMPTY");
                    }
                    if(this.coroutineCount.get() >= scraper.maxCoroutineCount){
                        //DebugLogger.log("Skipping thread execution!");
                        //DebugLogger.log("  Reason: MAX CO-ROUTINES (" + this.coroutineCount.get() + "/" + this.maxCoroutineCount + ")");
                    }

                    Thread.sleep(50); // Sleep when the maximum number of tasks are being executed
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                DebugLogger.log("Interrupted Exception!");
            }
        }

        // a bit hacky, but wait for all threads to finish requests before running closeRequestClient

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        this.taskProcessor.closeRequestClient();
    }
}
