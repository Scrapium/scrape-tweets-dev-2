package com.scrapium.proxium;

import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicInteger;

public class Proxy {



    private int id = -1;
    private String connString;
    private String ipAddress;
    private String port;
    private boolean isSocks;
    private int usageCount;
    private Timestamp nextAvailable;
    private String guestToken;
    private Timestamp guestTokenUpdated;
    private int successDelta;
    private int failedCount;
    private Timestamp lastUpdated;


    private final AtomicInteger usageCountChange;
    private final AtomicInteger successDeltaChange;
    private final AtomicInteger failedCountChange;

    // Constructor with all parameters
    public Proxy(int id, String connString, String ipAddress, String port, boolean isSocks, int usageCount, Timestamp nextAvailable, String guestToken, Timestamp guestTokenUpdated, int successDelta, int failedCount, Timestamp lastUpdated) {
        this.id = id;
        this.connString = connString;
        this.ipAddress = ipAddress;
        this.port = port;
        this.isSocks = isSocks;
        this.usageCount = usageCount;
        this.nextAvailable = nextAvailable;
        this.guestToken = guestToken;
        this.guestTokenUpdated = guestTokenUpdated;
        this.successDelta = successDelta;
        this.failedCount = failedCount;
        this.lastUpdated = lastUpdated;

        /*

            Live Updated values:

            - usageCount *

            - nextAvailable
            - guestToken
            - guestTokenUpdated

            - successDelta *
            - failedCount *

        */

        this.usageCountChange = new AtomicInteger(0);
        this.successDeltaChange = new AtomicInteger(0);
        this.failedCountChange = new AtomicInteger(0);
    }

    @Override
    public String toString() {
        return "Proxy{" +
                "id=" + id +
                ", connString='" + connString + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", port='" + port + '\'' +
                ", isSocks=" + isSocks +
                ", usageCount=" + usageCount +
                ", nextAvailable=" + nextAvailable +
                ", guestToken='" + guestToken + '\'' +
                ", guestTokenUpdated=" + guestTokenUpdated +
                ", successDelta=" + successDelta +
                ", failedCount=" + failedCount +
                '}';
    }



    public String getHostName() {
        return this.ipAddress;
    }

    public int getPort() {
        return Integer.parseInt(this.port);
    }

    public void onSuccess(){
        //System.out.println("Proxy win");
        this.usageCountChange.incrementAndGet();
        this.successDeltaChange.incrementAndGet();
        this.onProxyModified();
    }
    public void onFailure() {
        //System.out.println("Proxy loss");
        this.usageCountChange.incrementAndGet();
        this.successDeltaChange.decrementAndGet();
        this.failedCountChange.incrementAndGet();
        this.onProxyModified();
    }

    public void onProxyModified(){
        this.setLastUpdated(new Timestamp(System.currentTimeMillis()));
    }

    private void setLastUpdated(Timestamp timestamp) {
        // TODO: Update the last updated time in a synchronized way using atomic long
    }

    public int getRealUsageCount(){ return this.usageCount + this.usageCountChange.get(); }
    public int getSuccessDelta(){ return this.successDelta + this.successDeltaChange.get(); }
    public int getRealFailedCount(){ return this.failedCount + this.failedCountChange.get(); }




}