package com.scrapium;

import com.scrapium.Scraper;
import com.scrapium.utils.SLog;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TweetThread implements Runnable {

    private final Scraper scraper;
    private final BlockingQueue<TweetThreadTask> taskQueue;
    private final AtomicInteger coroutineCount;

    public TweetThread(Scraper scraper, BlockingQueue<TweetThreadTask> taskQueue, AtomicInteger coroutineCount) {
        this.scraper = scraper;
        this.taskQueue = taskQueue;
        this.coroutineCount = coroutineCount;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (coroutineCount.get() > 0) {
                    SLog.log("TweetThread: Ran cycle");

                    TweetThreadTask task = taskQueue.take();
                    SLog.log("TweetThread: Task Taken");

                    task.perform();

                    SLog.log("Decrementing counter");
                } else {
                    Thread.sleep(10); // Sleep when the maximum number of tasks are being executed
                }
            } catch (InterruptedException e) {
                SLog.log("Interrupted Exception!");
            }
        }
    }
}
