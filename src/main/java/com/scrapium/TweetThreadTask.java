package com.scrapium;

import com.scrapium.utils.SLog;
import okhttp3.*;

import java.io.IOException;

public class TweetThreadTask {

    private final Scraper scraper;
    private int requestCount;

    public TweetThreadTask(Scraper scraper) {
        this.scraper = scraper;
    }

    public void perform() {

        SLog.log("TweetThreadTask: Before attempting to increase request count.");

        //scraper.logger.increaseRequestCount();

        SLog.log("TweetThreadTask: Asked to perform task.");

        //////////////////////////////////////////////////////////////////////////////

        OkHttpClient client = new OkHttpClient();

        // Construct the request to the Twitter API
        Request request = new Request.Builder()
                .url("https://example.com")
                .build();

        // Make an asynchronous request to the Twitter API
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                SLog.log("FAIL: Scraping task completed");



                // Handle the failure of the request
                e.printStackTrace();
                scraper.coroutineCount.decrementAndGet();
                //scraper.coroutineCount.set(scraper.coroutineCount.get() - 1);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Handle the response from the Twitter API
                String responseBody = response.body().string();
                System.out.println("SUCCESS: "+response.code());

                scraper.logger.increaseRequestCount();

                // Parse the response and add the tweets to the tweetQueue
                // ...

                // Print a message to indicate that the task is completed
                SLog.log("SUCCESS: Scraping task completed");
                scraper.coroutineCount.decrementAndGet();
                //scraper.coroutineCount.set(scraper.coroutineCount.get() - 1);
            }
        });

        /////////////////////////////////////////////////////////////////////////////

    }
}
