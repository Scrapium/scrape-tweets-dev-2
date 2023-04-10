package com.scrapium;

import com.scrapium.utils.DebugLogger;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TweetThreadTaskProcessor {
    private final Scraper scraper;
    private final BlockingQueue<TweetTask> taskQueue;
    private AtomicInteger coroutineCount;

    private int requestCount;

    public TweetThreadTaskProcessor(Scraper scraper, BlockingQueue<TweetTask> taskQueue, AtomicInteger coroutineCount) {

        this.scraper = scraper;
        this.taskQueue = taskQueue;
        this.coroutineCount = coroutineCount;
        /*
        this.client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        */
    }

    public void processNextTask() {
        DebugLogger.log("TweetThreadTask: Before attempting to increase request count.");

        /*
        try {

            TweetTask task = this.taskQueue.take();


            DebugLogger.log("(1) TweetThreadTask: Asked to perform task.");

            //////////////////////////////////////////////////////////////////////////////

            coroutineCount.incrementAndGet();

            DebugLogger.log("(2) TweetThreadTask: Asked to perform task.");


            // Construct the request to the Twitter API
            Request request = new Request.Builder()
                    .url("https://example.com")
                    .build();


            // Make an asynchronous request to the Twitter API
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // Handle the response from the Twitter API
                    String responseBody = response.body().string();
                    //System.out.println("SUCCESS: "+response.code());

                    scraper.logger.increaseSuccessRequestCount();

                    // Parse the response and add the tweets to the tweetQueue
                    // ...

                    // Print a message to indicate that the task is completed
                    //DebugLogger.log("SUCCESS: Scraping task completed");
                    coroutineCount.decrementAndGet();
                    //scraper.coroutineCount.set(scraper.coroutineCount.get() - 1);
                }

                @Override
                public void onFailure(Call call, IOException e) {

                    DebugLogger.log("FAIL: Scraping task completed");


                    scraper.logger.increaseFailedRequestCount();
                    // Handle the failure of the request
                    //e.printStackTrace();
                    coroutineCount.decrementAndGet();
                    //scraper.coroutineCount.set(scraper.coroutineCount.get() - 1);
                }


            });


        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        */
    }
}
