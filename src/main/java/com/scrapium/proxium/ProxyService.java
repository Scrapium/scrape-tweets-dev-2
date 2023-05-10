package com.scrapium.proxium;


import com.scrapium.DatabaseConnection;
import com.scrapium.utils.TimeUtils;

import java.sql.*;
import java.util.*;


// TODO:
// TODO:
// TODO:
// TODO:
// TODO:
// TODO:     Update sync so that it modifies pre-existing data for multi-instance environments.
// TODO:     Requires updating getNewProxy function too
// TODO:        - remove Mod values.
// TODO:
// TODO:
// TODO:
// TODO:



public class ProxyService {

    public static Proxy getNullProxy(){
        return new Proxy(
                -1,
                "",
                "",
                "12345",
                false,
                0,
                new Timestamp(0),
                "",
                new Timestamp(0),
                0,
                0,
                new Timestamp(0)
        );
    }


    final private int LIST_SIZE = 50; // proxy size to reload into memory
    ArrayList<Proxy> proxyList;

    public ProxyService() {
        this.proxyList = new ArrayList<Proxy>();
    }

    public void syncAndRefresh() {
        do_sync();
    }



    /*

            Live Updated values:

            - usageCount *

            - nextAvailable

            - guestToken
            - guestTokenUpdated

            - successDelta *
            - failedCount *

    */



    private void do_sync(){



        ArrayList<Proxy> proxyListClone = (ArrayList<Proxy>) this.proxyList.clone();

        this.proxyList = this.grab_proxy_list();

        Iterator<Proxy> iterator = proxyListClone.iterator();

        try (Connection connection = DatabaseConnection.getConnection()) {

            List<Integer> updatedIDs = new ArrayList<>();

            // Loop through the elements using the iterator
            while (iterator.hasNext()) {

                Proxy cachedProxy = iterator.next();

                try {

                    //System.out.println("Updating proxy = " + cachedProxy.getID());

                    String updateSql = "UPDATE proxies SET usage_count=?, next_available=?, success_delta=?, failed_count=?, last_updated=? WHERE id=?";

                    PreparedStatement statement = connection.prepareStatement(updateSql);

                    //System.out.println(cachedProxy.getID());
                   // System.out.println(cachedProxy.getRealSuccessDelta());

                    statement.setInt(1, cachedProxy.getRealUsageCount()); // set the new usage_count
                    statement.setTimestamp(2, cachedProxy.getNextAvailable()); // set the new usage_count
                    statement.setInt(3, cachedProxy.getRealSuccessDelta()); // set the new success_delta
                    statement.setInt(4, cachedProxy.getRealFailedCount()); // set the new failed_count
                    statement.setTimestamp(5, cachedProxy.getLastUpdated()); // set the new failed_count
                    statement.setInt(6, cachedProxy.getID()); // set the id of the proxy to update

                    updatedIDs.add(cachedProxy.getID());

                    int rowsAffected = statement.executeUpdate();

                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

            }

            //System.out.println("Updated IDS: " + updatedIDs.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public Proxy getNewProxy(int depth){

        // add check for if no proxies are available.

        Random random = new Random();

        if(this.proxyList.size() == 0){
            try {
                System.out.println("WARNING: Proxy cache is empty, no proxies available!");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            return getNewProxy(depth + 1);
        }

        Collections.sort(proxyList, new Comparator<Proxy>() {
            @Override
            public int compare(Proxy p1, Proxy p2) {
                return p1.getRealSuccessDelta() - p2.getRealSuccessDelta();
            }
        });

        int topSize = 1;

        if(this.proxyList.size() > LIST_SIZE){
            topSize = LIST_SIZE;
        } else {
            topSize = this.proxyList.size();
        }


        int randomIndex = random.nextInt(LIST_SIZE);
        Proxy proxy = this.proxyList.get(randomIndex);

        //System.out.println(proxy.getID());

        //System.out.println("Proxy chosen, " + randomIndex + "/" + this.proxyList.size() );


        if(proxy.getRealFailedCount() > 50){
            ///proxy.resetUsageCountMod();
            proxy.resetFailedCountMod();
            proxy.setNextAvailable(TimeUtils.nowPlusMinutes(5));
            //System.out.println("proxy failed exceed: setting next available.");
            return getNewProxy(depth + 1);
        }



        if(proxy.getRealUsageCount() >= 800){
            proxy.resetUsageCountMod();
            proxy.resetFailedCountMod();
            proxy.setNextAvailable(TimeUtils.nowPlusMinutes(4));
            //System.out.println("proxy usage exceed: setting next available.");
            return getNewProxy(depth + 1);
        }

        return proxy;
    }

    /*
    // use synchronized (list) across threads.
    private void sync() {

        // Create an iterator for the ArrayList

        ArrayList<Proxy> proxyListClone = (ArrayList<Proxy>) this.proxyList.clone();

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
    */

    private ArrayList<Proxy> grab_proxy_list() {

        ArrayList<Proxy> newProxyList = new ArrayList<Proxy>();

        try (Connection connection = DatabaseConnection.getConnection()) {

            String query = "SELECT * FROM public.proxies as proxies WHERE next_available <= NOW() AND is_socks = false ORDER BY success_delta DESC";// + LIST_SIZE;

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

                //System.out.println("Loaded new proxy from public.proxies ID = " + proxy.getID());

                newProxyList.add(proxy);
            }

            resultSet.close();
            statement.close();

            return newProxyList;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }





}
