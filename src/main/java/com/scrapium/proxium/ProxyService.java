package com.scrapium.proxium;

import com.scrapium.DatabaseConnection;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class ProxyService {


    /*

    UPDATE test_proxy
        SET usage_count = 0,
        success_count = 0,
        failed_count = 0,
        fail_streak = 0,
        cooldown_until = NULL,
        last_used = NULL;

     */

    private ArrayList<Proxy> proxies;

    public ProxyService (){
        this.proxies = new ArrayList<Proxy>();
    }

    private void refreshProxies() throws SQLException {

        String query = "SELECT id, connection_string, usage_count, success_count, failed_count, fail_streak, cooldown_until " +
                "FROM test_proxy " +
                "WHERE (cooldown_until IS NULL OR NOW() > cooldown_until) " +
                "ORDER BY CASE WHEN usage_count = 0 THEN 1 ELSE success_count / usage_count END DESC" +
                "LIMIT 150";

        List<Proxy> fetchedProxies = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                fetchedProxies.add(new Proxy(
                        resultSet.getInt("id"),
                        resultSet.getString("connection_string"),
                        resultSet.getInt("usage_count"),
                        resultSet.getInt("success_count"),
                        resultSet.getInt("failed_count"),
                        resultSet.getInt("fail_streak"),
                        resultSet.getTimestamp("cooldown_until")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(fetchedProxies.size() == 0){
            System.out.println("Proxies list empty!");
        }

        synchronized (proxies) {
            this.proxies.clear();
            this.proxies.addAll(fetchedProxies);
        }
    }

    private void syncProxiesWithDB() throws SQLException {
        String query = "UPDATE test_proxy SET usage_count = ?, success_count = ?, failed_count = ?, fail_streak = ?, cooldown_until = ?, last_used = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection()){

            PreparedStatement preparedStatement = connection.prepareStatement(query);

            synchronized (proxies) {
                for (Proxy proxy : proxies) {
                    preparedStatement.setInt(1, proxy.getUsageCount());
                    preparedStatement.setInt(2, proxy.getSuccessCount());
                    preparedStatement.setInt(3, proxy.getFailedCount());
                    preparedStatement.setInt(4, proxy.getFailStreak());
                    preparedStatement.setTimestamp(5, proxy.getCooldownUntil());
                    preparedStatement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                    preparedStatement.setInt(7, proxy.getID());
                    preparedStatement.addBatch();
                }
            }



            int[] affectedRows = preparedStatement.executeBatch(); // Execute the batch
            System.out.println("Updated " + affectedRows.length + " row(s) in the proxies table.");

            refreshProxies();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Proxy getNewProxy() {
        synchronized (proxies) {
            if(proxies.size() == 0){
                return null;
            }

            Proxy proxy = null;

            while(
                    proxy == null ||
                    proxy.getCooldownUntil().after(new Timestamp(System.currentTimeMillis()))
            ){


                Random random = new Random();
                int randomIndex = random.nextInt(proxies.size());
                proxy = proxies.get(randomIndex);

            }


            return proxy;
        }
    }

    public void syncAndRefresh() throws SQLException {
        System.out.println("> ran syncAndRefresh (proxy list length = " + proxies.size() + ")");
        syncProxiesWithDB();

    }
}