package com.scrapium;
public class Main {

    // Define the thread pool with a fixed number of threads

    public static void main(String[] args) {
        Scraper scraper = new Scraper(10);
        scraper.scrape();
    }

}
