package com.scrapium;

public class Main {
    public static void main(String[] args) {
        Scraper scraper = new Scraper(12, 500);
        scraper.scrape();
    }
}