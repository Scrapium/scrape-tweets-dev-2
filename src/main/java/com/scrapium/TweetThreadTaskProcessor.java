package com.scrapium;

import com.scrapium.proxium.Proxy;
import com.scrapium.utils.DebugLogger;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.support.BasicRequestBuilder;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.apache.http.Consts;

import javax.net.ssl.*;
import java.io.IOException;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
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

    // Custom route planner class



    public TweetThreadTaskProcessor(int threadID, boolean running, Scraper scraper, BlockingQueue<TweetTask> taskQueue, AtomicInteger coroutineCount) {
        this.threadID = threadID;
        this.scraper = scraper;
        this.taskQueue = taskQueue;
        this.coroutineCount = coroutineCount;
        this.tweetThreadRunning = running;
        this.consumerQueue = new LinkedBlockingQueue<>();


    }

    static class CustomRoutePlanner implements HttpRoutePlanner {
        private final HttpHost proxyHost;

        CustomRoutePlanner(HttpHost proxyHost) {
            this.proxyHost = proxyHost;
        }

        @Override
        public HttpRoute determineRoute(HttpHost target, HttpContext context) throws HttpException {
            return new HttpRoute(target, null, proxyHost, "https".equalsIgnoreCase(target.getSchemeName()));
        }
    }

    /*
        Run Continuously
     */
    public void processNextTask() {
        DebugLogger.log("TweetThreadTask: Before attempting to increase request count.");

        try {

            Proxy proxy = scraper.proxyService.getNewProxy(0);

            TweetThreadRequestConsumer consumer = new TweetThreadRequestConsumer(this, proxy, coroutineCount, scraper);
            this.consumerQueue.add(consumer);
            TweetTask task = this.taskQueue.take();


            final SSLContext sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(null, new TrustAllStrategy())
                    .build();

            IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                    .setSoTimeout(Timeout.ofSeconds(3))
                    .build();

            final TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
                    .setSslContext(sslcontext)
                    .build();

            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(Timeout.ofMilliseconds(1000L))
                    .setConnectionRequestTimeout(Timeout.ofMilliseconds(1000L))
                    .setResponseTimeout(Timeout.ofMilliseconds(1000L))
                    .build();

            ConnectionConfig oConnectionConfig = ConnectionConfig.custom()
                    .setConnectTimeout(Timeout.ofMilliseconds(1000L))
                    .setSocketTimeout(Timeout.ofMilliseconds(1000L))
                    .setTimeToLive(Timeout.ofMilliseconds(1000L))
                    .build();

            PoolingAsyncClientConnectionManagerBuilder connectionManagerBuilder = PoolingAsyncClientConnectionManagerBuilder.create()
                    .setMaxConnPerRoute(2000)
                    .setTlsStrategy(tlsStrategy)
                    .setDefaultConnectionConfig(oConnectionConfig)
                    .setMaxConnTotal(2000)
                    ;

            final HttpHost httpProxy = new HttpHost("http", proxy.getHostName(), Integer.valueOf(proxy.getPort()));

            CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                    .setProxy(httpProxy)
                    .setIOReactorConfig(ioReactorConfig)
                    .setDefaultRequestConfig(config)
                    .setConnectionManager(connectionManagerBuilder.build())

                    .build();


            client.start();


            coroutineCount.incrementAndGet();


            

            final BasicHttpRequest request = BasicRequestBuilder.get()
                    .setHttpHost(new HttpHost("google.com"))
                    .setPath("/")
                    .build();

            System.out.println("making request");

            final Future<Void> future = client.execute(
                    new BasicRequestProducer(request, null),
                    consumer, null);





            /*
                try (final CloseableHttpClient httpclient = HttpClients.custom()
                        .setProxy(proxy)
                        .build()) {

                    final RequestConfig config = RequestConfig.custom()
                            .build();
                    final HttpGet request = new HttpGet("/get");
                    request.setConfig(config);

                    System.out.println("Executing request " + request.getMethod() + " " + request.getUri() +
                            " via " + proxy);

                    httpclient.execute(target, request, response -> {
                        System.out.println("----------------------------------------");
                        System.out.println(request + "->" + new StatusLine(response));
                        EntityUtils.consume(response.getEntity());
                        return null;
                    });
                }

             */




        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

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

        //client.close(CloseMode.GRACEFUL);
    }
}