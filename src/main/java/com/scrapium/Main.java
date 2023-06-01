package com.scrapium;

import com.scrapium.proxium.Proxy;
import com.scrapium.proxium.ProxyService;
import com.scrapium.proxium.loadProxies.ProxyLoader;
import com.scrapium.tests.Benchmark;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
// makes sense to use PostgreSQL for data, and Redis for caching & analytics


public class Main {

    public static void main(String[] args) {
        runService();
    }

    public static void runTest(){
        Benchmark.runTest();
    }

    public static void runService(){

        // Scraper(consumerCount, maxCoroutineCount, conSocketTimeout)
        
        // consumerCount - The number of threads running scraper tasks
        // maxCoroutineCount - The max amount of asynchronous calls that should be made for each thread
        // conSocketTimeout - The amount of time before connectionSocketTimeout will occur.

        // calls

        // scraper.logger.successRequestCount.get() - Will get the amount of total successful requests since .scrape() is called.
        // scraper.logger.failedRequestCount.get() - Will get the amount of total failed requests since .scrape() is called.

        // note: The last parameter of Scrape() is not currently used.

        Scraper scraper = new Scraper(1, 100, 10);
        scraper.scrape();

        // To load the proxies into the database, run this function instead of scraper.scrape()
        //ProxyLoader.loadProxies();

    }


}