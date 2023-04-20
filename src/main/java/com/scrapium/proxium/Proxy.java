package com.scrapium.proxium;

import java.sql.Timestamp;

public class Proxy {
    private int id = -1;
    private String connString;
    private String ipAddress;
    private String port;
    private boolean isSocks;
    private volatile int usageCount; // make it volatile for atomic updates
    private volatile Timestamp nextAvailable; // make it volatile for atomic updates
    private volatile String guestToken; // make it volatile for atomic updates
    private volatile Timestamp guestTokenUpdated; // make it volatile for atomic updates
    private volatile int successDelta; // make it volatile for atomic updates
    private volatile int failedCount; // make it volatile for atomic updates


    // Constructor with all parameters
    public Proxy(int id, String connString, String ipAddress, String port, boolean isSocks, int usageCount, Timestamp nextAvailable, String guestToken, Timestamp guestTokenUpdated, int successDelta, int failedCount) {
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
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
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
        return successDelta;
    }

    public void setSuccessDelta(int successDelta) {
        this.successDelta = successDelta;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
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