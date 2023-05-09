package com.scrapium;

import com.scrapium.proxium.Proxy;
import com.scrapium.utils.DebugLogger;
import org.asynchttpclient.*;
import org.asynchttpclient.proxy.ProxyServer;

import static org.asynchttpclient.Dsl.*;


import javax.net.ssl.*;
import java.io.IOException;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TweetThreadTaskProcessor {
    private final AsyncHttpClient c;

    /*
        Notes TODO:
            - AtomicReference isn't efficient (create a new object instead)
     */

    private Scraper scraper;
    private final BlockingQueue<TweetTask> taskQueue;
    private final int threadID;
    private volatile boolean  tweetThreadRunning;
    private AtomicInteger coroutineCount;

    private int requestCount;



    public TweetThreadTaskProcessor(int threadID, boolean running, Scraper scraper, BlockingQueue<TweetTask> taskQueue, AtomicInteger coroutineCount) {
        this.threadID = threadID;
        this.scraper = scraper;
        this.taskQueue = taskQueue;
        this.coroutineCount = coroutineCount;
        this.tweetThreadRunning = running;

        AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder().build();
        this.c = asyncHttpClient(config);

    }

    /*
        Run Continuously
     */
    public void processNextTask() {
        DebugLogger.log("TweetThreadTask: Before attempting to increase request count.");

        ProxyServer proxy1 = new ProxyServer.Builder("proxy1.example.com", 8080).build();

        Request request1 = new RequestBuilder("GET")
                .setUrl("http://example.com/")
                //.setProxyServer(proxy1)
                .build();

            c.executeRequest(request1, new handler(c, this));

    }

    public Scraper getScraper(){
        return this.scraper;
    }

    public LoggingThread getLogger(){
        return this.scraper.logger;
    }

}