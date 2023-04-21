package com.scrapium;

import com.scrapium.proxium.Proxy;
import com.scrapium.utils.DebugLogger;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http.support.BasicRequestBuilder;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TweetThreadTaskProcessor {

    /*
        Notes TODO:
            - AtomicReference isn't efficient (create a new object instead)
     */

    private final Scraper scraper;
    private final BlockingQueue<TweetTask> taskQueue;
    private final int threadID;
    private BlockingQueue<TweetThreadRequestConsumer> consumerQueue;
    private volatile boolean  tweetThreadRunning;
    private AtomicInteger coroutineCount;

    private int requestCount;
    final IOReactorConfig ioReactorConfig;
    final CloseableHttpAsyncClient client;

    public TweetThreadTaskProcessor(int threadID, boolean running, Scraper scraper, BlockingQueue<TweetTask> taskQueue, AtomicInteger coroutineCount) {
        this.threadID = threadID;
        this.scraper = scraper;
        this.taskQueue = taskQueue;
        this.coroutineCount = coroutineCount;
        this.tweetThreadRunning = running;
        this.consumerQueue = new LinkedBlockingQueue<>();
        
        ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(this.scraper.conSocketTimeout))
                .build();

        // Create a custom connection manager with custom limits

        PoolingAsyncClientConnectionManagerBuilder connectionManagerBuilder = PoolingAsyncClientConnectionManagerBuilder.create()
                .setMaxConnPerRoute(2000) // may be a bottleneck
                .setMaxConnPerRoute(2000);

        client = HttpAsyncClients.custom()
                .setIOReactorConfig(ioReactorConfig)
                .setConnectionManager(connectionManagerBuilder.build())
                .build();

        client.start();
    }

    /*
        Run Continuously
     */
    public void processNextTask() {
        DebugLogger.log("TweetThreadTask: Before attempting to increase request count.");

        try {
            TweetTask task = this.taskQueue.take();
            Proxy proxy = scraper.proxyService.getNewProxy(0);

            coroutineCount.incrementAndGet();

            final BasicHttpRequest request = BasicRequestBuilder.get()
                    .setHttpHost(new HttpHost("httpforever.com"))
                    .setPath("/")
                    .build();

            //System.out.println("Consumer Count: " + this.consumerQueue.size());

            TweetThreadRequestConsumer consumer = new TweetThreadRequestConsumer(this, coroutineCount, scraper);
            this.consumerQueue.add(consumer);

            final Future<Void> future = client.execute(
                    new BasicRequestProducer(request, null),
                    consumer, null);

        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // needs to be updated to accept different types of queries
    protected void removeConsumerFromQueue(TweetThreadRequestConsumer consumer) {
        this.consumerQueue.remove(consumer);
    }

    /*

        Run in the TweetThreadTaskProcessor when the scraper is stopped

     */
    public void closeRequestClient(){

        for (Iterator<TweetThreadRequestConsumer> iterator = this.consumerQueue.iterator(); iterator.hasNext(); ) {
            TweetThreadRequestConsumer item = iterator.next();
            item.shouldCancel = true;
        }


        double startSize = this.consumerQueue.size();

        if(startSize > 0){
            while(this.consumerQueue.size() > 0){
                double currentSize = this.consumerQueue.size();
                int n1 = (int) (50 * (currentSize / startSize));
                int n2 = (int) 50 - n1;
                System.out.println("(" + this.threadID + ") [" + "=".repeat(n2) + "-".repeat(n1) +  "]");

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        client.close(CloseMode.GRACEFUL);
    }
}

