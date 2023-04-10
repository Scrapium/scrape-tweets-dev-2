package com.scrapium;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        double maxTestDurationMinutes = 0.5;
        long testDurationMillis = (long) (maxTestDurationMinutes * 60 * 1000);
        Map<String, Double> results = new HashMap<>();

        for (int consumerCount = 1; consumerCount <= 5; consumerCount++) {
            for (int maxCoroutineCount = 100; maxCoroutineCount <= 500; maxCoroutineCount += 100) {
                String configKey = "C" + consumerCount + "_M" + maxCoroutineCount;
                System.out.println("Testing configuration: " + configKey);

                Scraper scraper = new Scraper(consumerCount, maxCoroutineCount);

                // Handle the SIGINT signal (CTRL + C)
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("Shutting down gracefully...");
                    scraper.stop();
                }));

                String osName = System.getProperty("os.name");
                if (!osName.toLowerCase().contains("windows")) {
                    // the environment is not Windows

                    // Handle the SIGTSTP signal (CTRL + Z)
                    CustomSignalHandler.handleTSTPSignal(() -> {
                        System.out.println("SIGTSTP signal received!");
                        System.exit(0);
                    });
                }





                scraper.scrape();

                try {
                    Thread.sleep(testDurationMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                double successPS = scraper.logger.successRequestCount.get() / (double) testDurationMillis * 1000;
                results.put(configKey, successPS);

                scraper.stop();

                try {
                    Thread.sleep(8000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        String bestConfiguration = null;
        double maxSuccessPS = 0;

        for (Map.Entry<String, Double> entry : results.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " successPS");
            if (entry.getValue() > maxSuccessPS) {
                maxSuccessPS = entry.getValue();
                bestConfiguration = entry.getKey();
            }
        }

        System.out.println("\nBest configuration: " + bestConfiguration + " with " + maxSuccessPS + " successPS");
    }
}