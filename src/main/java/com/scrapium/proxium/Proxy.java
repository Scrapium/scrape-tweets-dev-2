package com.scrapium.proxium;

import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicInteger;

public class Proxy {


    private int id = -1;
    private String connString;
    private String ipAddress;
    private String port;
    private boolean isSocks;
    private volatile AtomicInteger usageCount;
    private volatile Timestamp nextAvailable;
    private volatile String guestToken;
    private volatile Timestamp guestTokenUpdated;
    private volatile AtomicInteger successDelta;
    private volatile AtomicInteger failedCount;



    private volatile Timestamp lastUpdated; // make it volatile for atomic updates

    public int getOriginalUsageCount() {
        return originalUsageCount;
    }

    public void setOriginalUsageCount(int originalUsageCount) {
        this.originalUsageCount = originalUsageCount;
    }

    private volatile int originalUsageCount;

    private volatile int originalSuccessDelta;
    private volatile int originalFailedCount;



    // Constructor with all parameters
    public Proxy(int id, String connString, String ipAddress, String port, boolean isSocks, int usageCount, Timestamp nextAvailable, String guestToken, Timestamp guestTokenUpdated, int successDelta, int failedCount, Timestamp lastUpdated) {
        this.id = id;
        this.connString = connString;
        this.ipAddress = ipAddress;
        this.port = port;
        this.isSocks = isSocks;

        //this.usageCount = usageCount; // mod

        this.usageCount = new AtomicInteger(0);

        this.nextAvailable = nextAvailable; // update latest
        this.guestToken = guestToken; // update latest
        this.guestTokenUpdated = guestTokenUpdated; // update latest
        //this.successDelta = successDelta;   // mod
        //this.failedCount = failedCount; // mod



        this.successDelta = new AtomicInteger(0);
        this.failedCount = new AtomicInteger(0);


        this.originalUsageCount = usageCount;
        this.originalSuccessDelta = successDelta;
        this.originalFailedCount = failedCount;

        this.lastUpdated = lastUpdated;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getConnString() {
        return connString;
    }

    public void setConnString(String connString) {
        this.connString = connString;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getHostName(){ return ipAddress; }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public boolean isSocks() {
        return isSocks;
    }

    public void setSocks(boolean socks) {
        isSocks = socks;
    }

    public int getUsageCount() {
        return usageCount.get();
    }

    public void incrementUsageCount() {
        usageCount.incrementAndGet();
    }

    public void resetUsageCount() {
        usageCount.set(0);
    }

    public Timestamp getNextAvailable() {
        return nextAvailable;
    }

    public void setNextAvailable(Timestamp nextAvailable) {
        this.nextAvailable = nextAvailable;
    }

    public String getGuestToken() {
        return guestToken;
    }

    public void setGuestToken(String guestToken) {
        this.guestToken = guestToken;
    }

    public Timestamp getGuestTokenUpdated() {
        return guestTokenUpdated;
    }

    public void setGuestTokenUpdated(Timestamp guestTokenUpdated) {
        this.guestTokenUpdated = guestTokenUpdated;
    }

    public int getSuccessDelta() {
        return successDelta.get();
    }

    public void incrementSuccessDelta(){
        this.successDelta.incrementAndGet();
    }

    public void resetSuccessDelta(){
        this.successDelta.set(0);
    }

    public int getFailedCount() {
        return failedCount.get();
    }

    public void incrementFailedCount() {
        this.failedCount.incrementAndGet();
    }

    public void resetFailedCount() {
        this.failedCount.set(0);
    }

    public int getOriginalSuccessDelta() {
        return originalSuccessDelta;
    }

    public void setOriginalSuccessDelta(int originalSuccessDelta) {
        this.originalSuccessDelta = originalSuccessDelta;
    }

    public int getOriginalFailedCount() {
        return originalFailedCount;
    }

    public void setOriginalFailedCount(int originalFailedCount) {
        this.originalFailedCount = originalFailedCount;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        if(lastUpdated.after(this.lastUpdated)){
            this.lastUpdated = lastUpdated;
        }
    }


    public void onSuccess(){
        this.setLastUpdated(new Timestamp(System.currentTimeMillis()));
        this.usageCount.incrementAndGet();
        this.successDelta.incrementAndGet();
    }

    public void onFailure(){
        this.setLastUpdated(new Timestamp(System.currentTimeMillis()));
        this.usageCount.incrementAndGet();
        this.successDelta.decrementAndGet();
        this.failedCount.incrementAndGet();
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
}