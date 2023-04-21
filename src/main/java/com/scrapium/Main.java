package com.scrapium;

import com.scrapium.tests.Benchmark;

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

        Scraper scraper = new Scraper(1, 10, 10);
        scraper.scrape();

    }


}