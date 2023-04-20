package com.scrapium.proxium;


import com.scrapium.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class ProxyList {

    ArrayList<Proxy> proxyList;

    public ProxyList() {
        this.proxyList = new ArrayList<Proxy>();
    }

    public void syncAndUpdate() {
        sync();
        update();
    }

    // use synchronized (list) across threads.
    private void sync() {
        try (Connection connection = DatabaseConnection.getConnection()) {


        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void update() {
        System.out.println("called update");

    }



    public Proxy get() {
        return null;
    }
}
