package com.scrapium.proxium;


import com.scrapium.DatabaseConnection;
import com.scrapium.utils.TimeUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class ProxyList {

    final private int LIST_SIZE = 3;
    ArrayList<Proxy> proxyList;

    public ProxyList() {

        this.proxyList = new ArrayList<Proxy>();
    }

    public void syncAndRefresh() {
        sync();
        refresh();
    }

    // use synchronized (list) across threads.
    private void sync() {
        System.out.println("-- called sync -- ");
        /*
            this.usageCount = usageCount; // mod
            this.nextAvailable = nextAvailable; // update latest
            this.guestToken = guestToken; // update latest
            this.guestTokenUpdated = guestTokenUpdated; // update latest
            this.successDelta = successDelta;   // mod
            this.failedCount = failedCount; // mod
         */
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

                    int newUsageCount = cachedProxy.getUsageCount() - cachedProxy.getOriginalUsageCount() + realProxy.getUsageCount();
                    Timestamp newNextAvailable = cachedProxy.getNextAvailable().after(realProxy.getNextAvailable()) ? cachedProxy.getNextAvailable() : realProxy.getNextAvailable();
                    String newGuestToken = cachedProxy.getGuestTokenUpdated().after(realProxy.getGuestTokenUpdated()) ? cachedProxy.getGuestToken() : realProxy.getGuestToken();
                    Timestamp newGuestTokenUpdated = cachedProxy.getGuestTokenUpdated().after(realProxy.getGuestTokenUpdated()) ? cachedProxy.getGuestTokenUpdated() : realProxy.getGuestTokenUpdated();
                    int newSuccessDelta = cachedProxy.getSuccessDelta() - cachedProxy.getOriginalSuccessDelta() + realProxy.getSuccessDelta();
                    int newFailedCount = cachedProxy.getFailedCount() - cachedProxy.getOriginalFailedCount() + realProxy.getSuccessDelta();

                    String updateSql = "UPDATE proxies SET usage_count=?, next_available=?, guest_token=?, guest_token_updated=?, success_delta=?, failed_count=? WHERE id=?";
                    PreparedStatement statement = connection.prepareStatement(updateSql);
                    statement.setInt(1, newUsageCount); // set the new usage_count
                    statement.setTimestamp(2, newNextAvailable); // set the new next_available
                    statement.setString(3, newGuestToken); // set the new guest_token
                    statement.setTimestamp(4, newGuestTokenUpdated); // set the new guest_tokens_updated
                    statement.setInt(5, newSuccessDelta); // set the new success_delta
                    statement.setInt(6, newFailedCount); // set the new failed_count
                    statement.setInt(7, cachedProxy.getId()); // set the id of the proxy to update

                    int rowsAffected = statement.executeUpdate();
                    //System.out.println("Rows affected: " + rowsAffected);

                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Proxy getNewProxy(int depth){

        Random random = new Random();
        int randomIndex = random.nextInt(this.proxyList.size());

        Proxy proxy = this.proxyList.get(randomIndex);

        for (Proxy proxy2: proxyList){
            // run an in-memory "SQL query"
        }

        //if(depth < 100){
            if(proxy.getUsageCount() > 500){
                proxy.setUsageCount(0);
                proxy.setFailedCount(0);
                proxy.setNextAvailable(TimeUtils.nowPlusMinutes(15));
                return getNewProxy(depth + 1);
            }

            if(proxy.getUsageCount() > 500){
                proxy.setUsageCount(0);
                proxy.setFailedCount(0);
                proxy.setNextAvailable(TimeUtils.nowPlusMinutes(10));
                return getNewProxy(depth + 1);
            }
       // }



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
                        resultSet.getInt("failed_count")
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

            String query = "SELECT * FROM public.proxies as proxies WHERE next_available <= NOW() ORDER BY success_delta DESC LIMIT " + LIST_SIZE;

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
                        resultSet.getInt("failed_count")
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
