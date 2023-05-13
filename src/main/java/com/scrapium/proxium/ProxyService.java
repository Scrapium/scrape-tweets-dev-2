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


    final private int LIST_SIZE = 150; // proxy size to reload into memory
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

        if(proxy.getRealFailedCount() > 20){
            proxy.resetUsageCountMod();
            proxy.resetFailedCountMod();
            proxy.setNextAvailable(TimeUtils.nowPlusMinutes(5));
            //System.out.println("proxy failed exceed: setting next available.");
            return getNewProxy(depth + 1);
        }



        if(proxy.getRealUsageCount() >= 1500){
            proxy.resetUsageCountMod();
            proxy.resetFailedCountMod();
            proxy.setNextAvailable(TimeUtils.nowPlusMinutes(4));
            //System.out.println("proxy usage exceed: setting next available.");
            return getNewProxy(depth + 1);
        }

        return proxy;
    }

    private ArrayList<Proxy> grab_proxy_list() {

        ArrayList<Proxy> newProxyList = new ArrayList<Proxy>();

        try (Connection connection = DatabaseConnection.getConnection()) {

            String query = "SELECT * FROM public.proxies as proxies WHERE next_available <= NOW() AND is_socks = false ORDER BY last_updated DESC, success_delta DESC LIMIT " + this.LIST_SIZE;

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
