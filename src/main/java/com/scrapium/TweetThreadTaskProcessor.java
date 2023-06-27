package com.scrapium;

import com.scrapium.proxium.Proxy;
import com.scrapium.threads.LoggingThread;
import com.scrapium.utils.DebugLogger;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.asynchttpclient.*;
import org.asynchttpclient.proxy.ProxyServer;
import org.asynchttpclient.proxy.ProxyType;

import javax.net.ssl.*;

import static org.asynchttpclient.Dsl.*;


import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Base64;

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

    private SSLContext createSslContext() throws Exception {
        X509TrustManager tm = new X509TrustManager() {

            public void checkClientTrusted(X509Certificate[] xcs,
                                           String string) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] xcs,
                                           String string) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[] { tm }, null);
        return ctx;
    }

    public TweetThreadTaskProcessor(int threadID, boolean running, Scraper scraper, BlockingQueue<TweetTask> taskQueue, AtomicInteger coroutineCount) {
        this.threadID = threadID;
        this.scraper = scraper;
        this.taskQueue = taskQueue;
        this.coroutineCount = coroutineCount;
        this.tweetThreadRunning = running;



        AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder()
                .setConnectTimeout(8000)
                .setRequestTimeout(8000)
                .setReadTimeout(5000)
                .setMaxConnections(10000)
                .setMaxRequestRetry(1)
                .build();

        this.c = asyncHttpClient(config);

    }

    /*
        Run Continuously
     */
    public void processNextTask(){
        DebugLogger.log("TweetThreadTask: Before attempting to increase request count.");

        Proxy proxy = this.scraper.proxyService.getNewProxy();

        if(proxy != null){


            Request request1 = new RequestBuilder("GET")
                    .setUrl("http://httpforever.com")
                    .setProxyServer(new ProxyServer.Builder(proxy.getIP(), proxy.getPort()).build())

                   /* .setProxyServer(
                            new ProxyServer.Builder("193.202.84.117", 8080)
                                    .setProxyType(ProxyType.HTTP)
                                    .setRealm(new Realm.Builder("mix1015B1GYKS", "4DtgsBJE")
                                            .setScheme(Realm.AuthScheme.BASIC)))*/

                    .build();

            c.executeRequest(request1, new handler(c, proxy, this));
        } else {
            System.out.println("No proxies are available!");
        }

    }

    public Scraper getScraper(){
        return this.scraper;
    }

    public LoggingThread getLogger(){
        return this.scraper.logger;
    }

    public int getCoroutineCount() { return this.coroutineCount.get(); }

    public void incrementCoroutineCount() { this.coroutineCount.incrementAndGet(); }
    public void decrementCoroutineCount() { this.coroutineCount.decrementAndGet(); }


}