package com.scrapium;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ProducerThread implements Runnable {

    private AtomicInteger coroutineCount;
    private int current_thread_id = 0;
    private final Scraper scraper;
    private final ConcurrentLinkedQueue<TweetThreadTask> taskQueue;

    public ProducerThread(Scraper scraper, ConcurrentLinkedQueue<TweetThreadTask> taskQueue, AtomicInteger coroutineCount) {
        this.scraper = scraper;
        this.taskQueue = taskQueue;
        this.coroutineCount = coroutineCount;
    }

    @Override
    public void run() {
        while(true){
            if(coroutineCount.get() < scraper.maxCoroutineCount) {
                for (int i = 0; i < scraper.maxCoroutineCount - coroutineCount.get(); i++) {
                    //System.out.println("Producer: Added task " + current_thread_id++);
                    this.taskQueue.add(new TweetThreadTask());
                    coroutineCount.set(coroutineCount.get() + 1);
                }
            } else {
                try {
                    Thread.sleep(500);
                    //System.out.println("Producer: Not adding, queue full");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}