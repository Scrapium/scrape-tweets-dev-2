package com.scrapium;

import com.scrapium.utils.SLog;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TweetThreadTask {

    private final Scraper scraper;
    private int requestCount;

    OkHttpClient client;

    public TweetThreadTask(Scraper scraper) {

        this.scraper = scraper;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .writeTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .build();
    }

    public void perform() {

        SLog.log("TweetThreadTask: Before attempting to increase request count.");

        //scraper.logger.increaseRequestCount();

        SLog.log("TweetThreadTask: Asked to perform task.");

        //////////////////////////////////////////////////////////////////////////////



        // Construct the request to the Twitter API
        Request request = new Request.Builder()
                .url("http://glowingwholebrightmoon.neverssl.com/online/")
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
                SLog.log("SUCCESS: Scraping task completed");
                scraper.coroutineCount.decrementAndGet();
                //scraper.coroutineCount.set(scraper.coroutineCount.get() - 1);
            }

            @Override
            public void onFailure(Call call, IOException e) {

                SLog.log("FAIL: Scraping task completed");


                scraper.logger.increaseFailedRequestCount();
                // Handle the failure of the request
                //e.printStackTrace();
                scraper.coroutineCount.decrementAndGet();
                //scraper.coroutineCount.set(scraper.coroutineCount.get() - 1);
            }


        });

        /////////////////////////////////////////////////////////////////////////////

    }
}
