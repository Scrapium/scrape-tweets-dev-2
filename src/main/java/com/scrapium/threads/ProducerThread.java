package com.scrapium.threads;

import com.scrapium.Scraper;
import com.scrapium.ThreadBase;
import com.scrapium.TweetTask;
import com.scrapium.utils.DebugLogger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ProducerThread extends ThreadBase implements Runnable {


    private final Scraper scraper;
    private final BlockingQueue<TweetTask> taskQueue;

    public ProducerThread(Scraper scraper, BlockingQueue<TweetTask> taskQueue) {
        this.scraper = scraper;
        this.taskQueue = taskQueue;
    }

    @Override
    public void run() {
        while (this.running) {
            if (scraper.tweetQueue.size() < 5000) {
               // System.out.println("Adding (" + (5000 - scraper.tweetQueue.size()) + ") tasks to queue.");
                for (int i = 0; i < 5000 - scraper.tweetQueue.size(); i++) {
                    //DebugLogger.log("Producer: Added item to TaskQueue");
                    this.taskQueue.add(new TweetTask("Example tweet search"));

                }

            } else {
               //System.out.println("tweetqueue size = " + scraper.tweetQueue.size());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
    }
}