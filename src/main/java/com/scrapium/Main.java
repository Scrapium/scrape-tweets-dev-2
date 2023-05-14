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

        //Scraper scraper = new Scraper(1, 1, 10);

        // settings for .env
        // t3.xlarge - 6/1000


        //Scraper scraper = new Scraper(1, 2000, 10);
        //scraper.scrape();

        //ProxyLoader.findProxies();
        // check proxies
        //ProxyLoader.loadProxies();



        /*
        ProxyLoader.findProxies();
        ProxyLoader.loadProxies();

        String query = "UPDATE test_proxy SET usage_count = 0, success_count = 0, failed_count = 0, fail_streak = 0, cooldown_until = NULL";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            int affectedRows = preparedStatement.executeUpdate(); // Execute the update
            System.out.println("Rows affected: " + affectedRows);
            Thread.sleep(1000);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } */




        Scraper scraper = new Scraper(4, 800, 10);
        scraper.scrape();
    }


}