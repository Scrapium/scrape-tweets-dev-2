package com.scrapium;

import com.scrapium.proxium.Proxy;
import com.scrapium.utils.DebugLogger;
import org.apache.hc.client5.http.async.methods.AbstractCharResponseConsumer;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.concurrent.atomic.AtomicInteger;

public class TweetThreadRequestConsumer extends AbstractCharResponseConsumer<Void> {

    private final AtomicInteger coroutineCount;
    private final Scraper scraper;
    private final TweetThreadTaskProcessor processor;
    private final Proxy proxy;
    public volatile boolean shouldCancel;

    public volatile boolean isFinished;
    public TweetThreadRequestConsumer(TweetThreadTaskProcessor tweetThreadTaskProcessor, Proxy proxy, AtomicInteger coroutineCount, Scraper scraper) {
        this.processor = tweetThreadTaskProcessor;
        this.proxy = proxy;
        this.coroutineCount = coroutineCount;
        this.scraper = scraper;
        this.shouldCancel = false;
        this.isFinished = false;
    }

    @Override
    protected void start(HttpResponse response, ContentType contentType) throws HttpException, IOException {

        coroutineCount.decrementAndGet();

        System.out.println("\n> Started new request!!!\n");

        if (response.getCode() != 200) {
            System.out.println("code not 200, but " + response.getCode());
            scraper.logger.increaseFailedRequestCount();
            releaseResources();
            shouldCancel = true;
        } else {
            System.out.println("code 200");
            scraper.logger.increaseSuccessRequestCount();
        }
    }

    @Override
    protected int capacityIncrement() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected void data(CharBuffer data, boolean endOfStream) throws IOException {
        if (shouldCancel) {
            releaseResources();
            return;
        }

        while (data.hasRemaining()) {
            char str = data.get();
            //System.out.print(str);
        }

        if (endOfStream) {
            releaseResources();
        }
    }

    @Override
    protected Void buildResult() throws IOException {
        return null;
    }

    @Override
    public void failed(Exception cause) {
        if (!shouldCancel) {
            coroutineCount.decrementAndGet();
            DebugLogger.log("Failed request");
            scraper.logger.increaseFailedRequestCount();
            releaseResources();
        }
    }

    @Override
    public void releaseResources() {
        // Release resources here
        this.isFinished = true;
        this.processor.removeConsumerFromQueue(this);
    }
}
