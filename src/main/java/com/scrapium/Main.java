package com.scrapium;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    // Define the thread pool with a fixed number of threads

    public static void main(String[] args) {
        Scraper scraper = new Scraper();
        scraper.scrape();
    }

}
