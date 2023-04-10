package com.scrapium;

import com.scrapium.utils.DebugLogger;
import org.apache.hc.client5.http.async.methods.AbstractCharResponseConsumer;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http.support.BasicRequestBuilder;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOReactorShutdownException;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TweetThreadTaskProcessor {
    private final Scraper scraper;
    private final BlockingQueue<TweetTask> taskQueue;
    private AtomicInteger coroutineCount;

    private int requestCount;

    final IOReactorConfig ioReactorConfig;
    final CloseableHttpAsyncClient client;

    public TweetThreadTaskProcessor(Scraper scraper, BlockingQueue<TweetTask> taskQueue, AtomicInteger coroutineCount) {
        this.scraper = scraper;
        this.taskQueue = taskQueue;
        this.coroutineCount = coroutineCount;

        ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(this.scraper.conSocketTimeout))
                .build();

        // Create a custom connection manager with custom limits
        PoolingAsyncClientConnectionManagerBuilder connectionManagerBuilder = PoolingAsyncClientConnectionManagerBuilder.create()
                .setMaxConnPerRoute(100)
                .setMaxConnPerRoute(100);

        client = HttpAsyncClients.custom()
                .setIOReactorConfig(ioReactorConfig)
                .setConnectionManager(connectionManagerBuilder.build())
                .build();

        client.start();
    }

    public void processNextTask() {
        DebugLogger.log("TweetThreadTask: Before attempting to increase request count.");

        try {
            TweetTask task = this.taskQueue.take();

            coroutineCount.incrementAndGet();

            final BasicHttpRequest request = BasicRequestBuilder.get()
                    .setHttpHost(new HttpHost("conpiler.io"))
                    .setPath("/")
                    .build();

            final Future<Void> future = client.execute(
                    new BasicRequestProducer(request, null),
                    new AbstractCharResponseConsumer<Void>() {

                        @Override
                        protected void start(
                                final HttpResponse response,
                                final ContentType contentType) throws HttpException, IOException {

                            coroutineCount.decrementAndGet();

                            if (response.getCode() != 200) {
                                scraper.logger.increaseFailedRequestCount();

                                failed(new Exception("HTTP response code was not 200: " + response.getCode()));

                                return;
                            } else {
                                scraper.logger.increaseSuccessRequestCount();
                            }

                        }

                        @Override
                        protected int capacityIncrement() {
                            return Integer.MAX_VALUE;
                        }

                        @Override
                        protected void data(final CharBuffer data, final boolean endOfStream) throws IOException {
                            while (data.hasRemaining()) {
                                data.get();
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
                        public void failed(final Exception cause) {
                            // TODO: FIX FAILED
                            // System.out.println(request + "->" + cause);
                        }

                        @Override
                        public void releaseResources() {

                        }

                    }, null);

        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void closeRequestClient(){
        client.close(CloseMode.GRACEFUL);
    }
}
