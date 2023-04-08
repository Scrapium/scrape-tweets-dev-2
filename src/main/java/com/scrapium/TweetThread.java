package com.scrapium;

import okhttp3.*;

import java.io.IOException;
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
            //System.out.println("Task Count: " + this.scraper.coroutineCount.get());
            //System.out.println("Task Size Count: " + this.taskQueue.size());

            if(coroutineCount.get() > 0){
                TweetThreadTask task = taskQueue.poll();
                if (task != null) {
                    // perform the task here
                    System.out.println("Running task ");

                    //////////////////////////



                    OkHttpClient client = new OkHttpClient();

                    // Construct the request to the Twitter API
                    Request request = new Request.Builder()
                            .url("https://example.com")
                            .build();

                    // Make an asynchronous request to the Twitter API
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                            System.out.println("FAIL: Scraping task completed");

                            // Handle the failure of the request
                            e.printStackTrace();

                            scraper.coroutineCount.set(scraper.coroutineCount.get() - 1);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            // Handle the response from the Twitter API
                            String responseBody = response.body().string();
                            //System.out.println(responseBody);
                            System.out.println(response.code());
                            // Parse the response and add the tweets to the tweetQueue
                            // ...

                            // Print a message to indicate that the task is completed
                            System.out.println("SUCCESS: Scraping task completed");

                            scraper.coroutineCount.set(scraper.coroutineCount.get() - 1);
                        }
                    });










                    ///////////////////



                    //scraper.coroutineCount.set(scraper.coroutineCount.get() - 1);
                } else {
                    //System.out.println("No task found");
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