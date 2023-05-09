package com.scrapium;

import com.scrapium.proxium.Proxy;
import com.scrapium.utils.DebugLogger;
import org.apache.hc.client5.http.async.methods.AbstractCharResponseConsumer;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TweetThreadRequestConsumer extends AbstractCharResponseConsumer<Void> {

    private final AtomicInteger coroutineCount;
    private final Scraper scraper;
    private final TweetThreadTaskProcessor processor;
    private final Proxy proxy;
    public volatile boolean shouldCancel;

    public String response_str = "";

    private final AtomicBoolean isReleased;

    public TweetThreadRequestConsumer(TweetThreadTaskProcessor tweetThreadTaskProcessor, Proxy proxy, AtomicInteger coroutineCount, Scraper scraper) {
        this.processor = tweetThreadTaskProcessor;
        this.proxy = proxy;
        this.coroutineCount = coroutineCount;
        this.scraper = scraper;
        this.shouldCancel = false;

        this.isReleased = new AtomicBoolean(false);
    }

    @Override
    protected void start(HttpResponse response, ContentType contentType) throws HttpException, IOException {

        //System.out.println(response.getCode());

        if(response.getCode() == 200){
            this.proxy.onSuccess();
            scraper.logger.increaseSuccessRequestCount();
        } else {
        }

    }

    @Override
    protected int capacityIncrement() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected void data(CharBuffer data, boolean endOfStream) throws IOException {

        while (data.hasRemaining()) {
            char str = data.get();
            response_str += str;
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


       // cause.printStackTrace();

        scraper.logger.increaseFailedRequestCount();

        this.proxy.onFailure();
    }

    @Override
    public void releaseResources() {
        if (isReleased.compareAndSet(false, true)) {
            coroutineCount.decrementAndGet();
        }
    }
}