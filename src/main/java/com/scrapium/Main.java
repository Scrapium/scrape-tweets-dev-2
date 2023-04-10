package com.scrapium;

public class Main {
    public static void main(String[] args) {

        Scraper scraper = new Scraper(2, 250);

        // Handle the SIGINT signal (CTRL + C)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gracefully...");
            scraper.stop();
        }));

        // Handle the SIGTSTP signal (CTRL + Z)
        CustomSignalHandler.handleTSTPSignal(() -> {
            System.out.println("SIGTSTP signal received!");
            // Perform cleanup and shutdown tasks here
            System.exit(0);
        });

        scraper.scrape();

    }
}