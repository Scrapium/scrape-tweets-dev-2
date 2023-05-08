package com.scrapium.proxium;


import com.scrapium.DatabaseConnection;
import com.scrapium.utils.TimeUtils;

import java.sql.*;
import java.util.*;

public class ProxyService {


    final private int LIST_SIZE = 10; // proxy size to reload into memory
    ArrayList<Proxy> proxyList;

    public ProxyService() {

        this.proxyList = new ArrayList<Proxy>();
    }

    public void syncAndRefresh() {
        sync();
        refresh();
    }

    // use synchronized (list) across threads.
    private void sync() {

        // Create an iterator for the ArrayList

        ArrayList<Proxy> proxyListClone = (ArrayList<Proxy>) this.proxyList.clone();

        refresh();

        Iterator<Proxy> iterator = proxyListClone.iterator();

        try (Connection connection = DatabaseConnection.getConnection()) {


            // Loop through the elements using the iterator
            while (iterator.hasNext()) {

                Proxy cachedProxy = iterator.next();
                Proxy realProxy = getProxyWithID(cachedProxy.getId());


                try {

                    // calculate difference since proxy was cached, add it to real proxy value

                    int newUsageCount = cachedProxy.getUsageCount() - cachedProxy.getUsageCount() + realProxy.getUsageCount();
                    Timestamp newNextAvailable = cachedProxy.getNextAvailable().after(realProxy.getNextAvailable()) ? cachedProxy.getNextAvailable() : realProxy.getNextAvailable();
                    String newGuestToken = cachedProxy.getGuestTokenUpdated().after(realProxy.getGuestTokenUpdated()) ? cachedProxy.getGuestToken() : realProxy.getGuestToken();
                    Timestamp newGuestTokenUpdated = cachedProxy.getGuestTokenUpdated().after(realProxy.getGuestTokenUpdated()) ? cachedProxy.getGuestTokenUpdated() : realProxy.getGuestTokenUpdated();
                    int newSuccessDelta = cachedProxy.getSuccessDelta() - cachedProxy.getOriginalSuccessDelta() + realProxy.getSuccessDelta();
                    Timestamp newLastUpdated = cachedProxy.getLastUpdated().after(realProxy.getLastUpdated()) ? cachedProxy.getLastUpdated() : realProxy.getLastUpdated();
                    int newFailedCount = cachedProxy.getFailedCount() - cachedProxy.getOriginalFailedCount() + realProxy.getFailedCount();

                    if(newUsageCount >= 100000000){ newUsageCount = 100000000; }
                    if(newSuccessDelta <= -50000) { newSuccessDelta = -50000; }
                    if(newSuccessDelta >= 50000) { newSuccessDelta = 50000; }
                    if(newFailedCount >= 500) { newFailedCount = 500; }

                    String updateSql = "UPDATE proxies SET usage_count=?, next_available=?, guest_token=?, guest_token_updated=?, success_delta=?, failed_count=?, last_updated=? WHERE id=?";

                    PreparedStatement statement = connection.prepareStatement(updateSql);

                    statement.setInt(1, newUsageCount); // set the new usage_count
                    statement.setTimestamp(2, newNextAvailable); // set the new next_available
                    statement.setString(3, newGuestToken); // set the new guest_token
                    statement.setTimestamp(4, newGuestTokenUpdated); // set the new guest_tokens_updated
                    statement.setInt(5, newSuccessDelta); // set the new success_delta
                    statement.setInt(6, newFailedCount); // set the new failed_count
                    statement.setTimestamp(7, newLastUpdated); // set the new failed_count
                    statement.setInt(8, cachedProxy.getId()); // set the id of the proxy to update

                    int rowsAffected = statement.executeUpdate();
                    //System.out.println("Rows affected: " + rowsAffected);

                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Proxy getNewProxy(int depth){

        //System.out.println("Requested new proxy");

        // add check for if no proxies are available.

        Random random = new Random();

        if(this.proxyList.size() == 0){
            try {
                System.out.println("Oops! Proxy cache is empty! A query condition has failed.");
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            return getNewProxy(depth + 1);
        }

        int randomIndex = random.nextInt(this.proxyList.size());
        Proxy proxy = this.proxyList.get(randomIndex);


        Collections.sort(proxyList, new Comparator<Proxy>() {
            @Override
            public int compare(Proxy p1, Proxy p2) {
                return p1.getSuccessDelta() - p2.getSuccessDelta();
            }
        });

        if(proxy.getFailedCount() > 100){
            proxy.resetFailedCount();
            proxy.incrementUsageCount();
            System.out.println("proxy failed exceed: setting next available.");
            proxy.setNextAvailable(TimeUtils.nowPlusMinutes(4));
            return getNewProxy(depth + 1);
        }

        if(proxy.getUsageCount() >= 800){
            proxy.setNextAvailable(TimeUtils.nowPlusMinutes(1));
            System.out.println("proxy usage exceed: setting next available.");
            return getNewProxy(depth + 1);
        }

        //System.out.println(proxy.getId());


        return proxy;
    }

    private Proxy getProxyWithID(int id){
        try (Connection connection = DatabaseConnection.getConnection()) {

            String query = "SELECT * FROM public.proxies as proxies WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(query);

            statement.setInt(1, id);

            ResultSet resultSet = statement.executeQuery();


            while (resultSet.next()) {

                Proxy proxy = new Proxy(
                        Integer.parseInt(resultSet.getString("id")),
                        resultSet.getString("conn_string"),
                        resultSet.getString("ip_address"),
                        resultSet.getString("port"),
                        resultSet.getBoolean("is_socks"),
                        resultSet.getInt("usage_count"),
                        resultSet.getTimestamp("next_available"),
                        resultSet.getString("guest_token"),
                        resultSet.getTimestamp("guest_token_updated"),
                        resultSet.getInt("success_delta"),
                        resultSet.getInt("failed_count"),
                        resultSet.getTimestamp("last_updated")
                );

                return proxy;

            }

            resultSet.close();
            statement.close();


        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    private void refresh() {

        // reset the proxy list to empty

        this.proxyList = new ArrayList<Proxy>();

        try (Connection connection = DatabaseConnection.getConnection()) {

            String query = "SELECT * FROM public.proxies as proxies WHERE next_available <= NOW() AND is_socks = false ORDER BY success_delta DESC LIMIT " + LIST_SIZE;
            //String query = "SELECT * FROM public.proxies as proxies WHERE is_socks = false ORDER BY success_delta DESC LIMIT " + LIST_SIZE;

            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Proxy proxy = new Proxy(
                        Integer.parseInt(resultSet.getString("id")),
                        resultSet.getString("conn_string"),
                        resultSet.getString("ip_address"),
                        resultSet.getString("port"),
                        resultSet.getBoolean("is_socks"),
                        resultSet.getInt("usage_count"),
                        resultSet.getTimestamp("next_available"),
                        resultSet.getString("guest_token"),
                        resultSet.getTimestamp("guest_token_updated"),
                        resultSet.getInt("success_delta"),
                        resultSet.getInt("failed_count"),
                        resultSet.getTimestamp("last_updated")
                );

                proxyList.add(proxy);
            }

            resultSet.close();
            statement.close();


        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


}
