package com.scrapium;

public class Main {

    public static void main(String[] args) {

        // Scraper(consumerCount, maxCoroutineCount, conSocketTimeout)
        // consumerCount - The number of threads running scraper tasks
        // maxCoroutineCount - The max amount of asynchronous calls that should be made for each thread
        // conSocketTimeout - The amount of time before connectionSocketTimeout will occur.

        Scraper scraper = new Scraper(4, 800, 5);
        scraper.scrape();


    }
}