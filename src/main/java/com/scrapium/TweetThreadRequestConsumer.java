package com.scrapium;

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
    public volatile boolean shouldCancel;

    public volatile boolean isFinished;
    public TweetThreadRequestConsumer(TweetThreadTaskProcessor tweetThreadTaskProcessor, AtomicInteger coroutineCount, Scraper scraper) {
        this.processor = tweetThreadTaskProcessor;
        this.coroutineCount = coroutineCount;
        this.scraper = scraper;
        this.shouldCancel = false;
        this.isFinished = false;
    }

    @Override
    protected void start(HttpResponse response, ContentType contentType) throws HttpException, IOException {

        coroutineCount.decrementAndGet();

        if (response.getCode() != 200) {
            scraper.logger.increaseFailedRequestCount();
            shouldCancel = true;
        } else {
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
            System.out.print(str);
        }

        if (endOfStream) {
            releaseResources();
            coroutineCount.decrementAndGet();
        }
    }

    @Override
    protected Void buildResult() throws IOException {
        return null;
    }

    @Override
    public void failed(Exception cause) {
        // Handle failure here
    }

    @Override
    public void releaseResources() {
        // Release resources here
        this.isFinished = true;
        this.processor.removeConsumerFromQueue(this);
    }
}
