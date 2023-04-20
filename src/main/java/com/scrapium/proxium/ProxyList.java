package com.scrapium.proxium;


import com.scrapium.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ProxyList {

    final private int LIST_SIZE = 250;
    ArrayList<Proxy> proxyList;

    public ProxyList() {

        this.proxyList = new ArrayList<Proxy>();
    }

    public void syncAndRefresh() {
        refresh();
        sync();
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

    // use synchronized (list) across threads.
    private void sync() {
        System.out.println("called sync");
    }



    public Proxy get() {
        return null;
    }
}
