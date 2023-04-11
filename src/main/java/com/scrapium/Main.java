package com.scrapium;

import com.scrapium.utils.TimeFormatter;

import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        runTest();
    }

    public static void runService(){

        // Scraper(consumerCount, maxCoroutineCount, conSocketTimeout)
        // consumerCount - The number of threads running scraper tasks
        // maxCoroutineCount - The max amount of asynchronous calls that should be made for each thread
        // conSocketTimeout - The amount of time before connectionSocketTimeout will occur.

        // calls
        // scraper.logger.successRequestCount.get() - Will get the amount of total successful requests since .scrape() is called.
        // scraper.logger.failedRequestCount.get() - Will get the amount of total failed requests since .scrape() is called.

        Scraper scraper = new Scraper(4, 800, 5);
        scraper.scrape();

    }

    public static void runTest() {
        Map<String, Integer> configResults = new HashMap<>();
        String bestConfigKey = "";
        int highestSuccessfulRequests = 0;

        double timePerTest = 2 * 60 * 1000; // 30 seconds

        int totalTestCount = (((6-1)/2) * ((1500-100)/250) * ((28 - 4)/10));
        int totalTestTime = (int) (totalTestCount * timePerTest);

        int testIter = 0;

        System.out.println("\n== Test started ==\n");
        System.out.println("- Total Tests = " + (totalTestCount));
        System.out.println("- Test will be completed " + TimeFormatter.timeToString((totalTestTime/1000)));

        for (int consumerCount = 1; consumerCount <= 6; consumerCount += 2) { // 1 -> 8
            for (int maxCoroutineCount = 100; maxCoroutineCount <= 1000; maxCoroutineCount += 250) { // 100 -> 2000
                for (int conSocketTimeout = 4; conSocketTimeout <= 28; conSocketTimeout += 10) { // 4 -> 28

                    Scraper scraper = new Scraper(consumerCount, maxCoroutineCount, conSocketTimeout);
                    scraper.scrape();

                    String configKey = String.format("c_%d_m_%d_t_%d", consumerCount, maxCoroutineCount, conSocketTimeout);

                    System.out.println("\n[" + testIter + "/" + totalTestCount + "] Starting test: "+ configKey);

                    try {
                        Thread.sleep((long) timePerTest);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    int successfulRequests = scraper.logger.successRequestCount.get();
                    int failedRequests = scraper.logger.failedRequestCount.get();


                    configResults.put(configKey, successfulRequests);

                    int timeRemaining = (int) (totalTestTime - testIter * timePerTest);


                    System.out.printf("\n[" + testIter + "/" + totalTestCount + "] Configuration: %s | Successful Requests: %d | Failed Requests: %d%n\n",
                            configKey, successfulRequests, failedRequests);
                    System.out.println("Test will be completed " + TimeFormatter.timeToString(timeRemaining/1000) + "\n");

                    scraper.stop();

                    testIter++;

                    if (successfulRequests > highestSuccessfulRequests) {
                        highestSuccessfulRequests = successfulRequests;
                        bestConfigKey = configKey;
                    }
                }
            }
        }

        System.out.println("\n== All Configuration Results ==");
        System.out.println("\n== C=threads m=coroutines t=timeout");

        for (Map.Entry<String, Integer> entry : configResults.entrySet()) {
            System.out.printf("Configuration: %s | Successful Requests: %d%n", entry.getKey(), entry.getValue());
        }

        System.out.printf("\nBest Configuration: %s | Highest Successful Requests: %d%n", bestConfigKey, highestSuccessfulRequests);
    }
}