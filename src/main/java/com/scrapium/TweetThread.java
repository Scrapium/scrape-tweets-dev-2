package com.scrapium;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TweetThread implements Runnable {

    private final Scraper scraper;
    private final ConcurrentLinkedQueue<TweetThreadTask> taskQueue;
    private final AtomicInteger coroutineCount;

    public TweetThread(Scraper scraper, ConcurrentLinkedQueue<TweetThreadTask> taskQueue, AtomicInteger coroutineCount) {
        this.scraper = scraper;
        this.taskQueue = taskQueue;
        this.coroutineCount = coroutineCount;
    }

    @Override
    public void run() {
        while(true){
            System.out.println("Task Count: " + this.scraper.coroutineCount.get());
            System.out.println("Task Size Count: " + this.taskQueue.size());

            if(coroutineCount.get() > 0){
                TweetThreadTask task = taskQueue.poll();
                if (task != null) {
                    // perform the task here
                    System.out.println("Running task ");
                    task.perform();
                    scraper.coroutineCount.set(scraper.coroutineCount.get() - 1);
                } else {
                    System.out.println("No task found");
                    // no task found, exit the task
                }
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }
}