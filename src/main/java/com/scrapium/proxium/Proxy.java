package com.scrapium.proxium;

import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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


    private final AtomicInteger usageCountMod;
    private AtomicLong nextAvailableMod;
    private final AtomicInteger successDeltaMod;
    private final AtomicInteger failedCountMod;

    private AtomicLong lastUpdatedMod;


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

            - usageCount * v

            - nextAvailable

            - guestToken
            - guestTokenUpdated

            - successDelta * v
            - failedCount * v
            
            - last updated

        */


        this.usageCountMod = new AtomicInteger(0);
        this.nextAvailableMod = new AtomicLong(lastUpdated.getTime());

        // guestToken, guestTokenUpdated

        this.successDeltaMod = new AtomicInteger(0);
        this.failedCountMod = new AtomicInteger(0);

        this.lastUpdatedMod = new AtomicLong(lastUpdated.getTime());
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
    public String getIpAddress() { return this.ipAddress; }
    public int getPort() {
        return Integer.parseInt(this.port);
    }

    public String getConnString() { return this.connString; }

    public void onSuccess(){
        //System.out.println("Proxy win");
        this.usageCountMod.incrementAndGet();
        this.successDeltaMod.incrementAndGet();
        this.onProxyModified();
    }
    public void onFailure() {
        //System.out.println("Proxy loss");
        this.usageCountMod.incrementAndGet();
        this.successDeltaMod.decrementAndGet();
        this.failedCountMod.incrementAndGet();
        this.onProxyModified();
    }

    public void onProxyModified(){
        this.setLastUpdated(new Timestamp(System.currentTimeMillis()));
    }

    public int getRealUsageCount(){ return this.usageCount + this.usageCountMod.get(); }
    public int getRealSuccessDelta(){ return this.successDelta + this.successDeltaMod.get(); }
    public int getRealFailedCount(){ return this.failedCount + this.failedCountMod.get(); }

    // increment setters
    public void incrementUsageCountMod(){ this.usageCountMod.incrementAndGet(); }
    public void incrementSuccessDeltaMod(){ this.successDeltaMod.incrementAndGet(); }
    public void incrementFailedCountMod(){ this.failedCountMod.incrementAndGet(); }

    // decrement setters
    public void decrementUsageCountMod() { this.usageCountMod.decrementAndGet(); }
    public void decrementSuccessDeltaMod() { this.successDeltaMod.decrementAndGet(); }
    public void decrementFailedCountMod() { this.failedCountMod.decrementAndGet(); }

    public void resetUsageCountMod(){ this.usageCountMod.set(0);}
    public void resetSuccessDeltaMod(){ this.successDeltaMod.set(0);}
    public void resetFailedCountMod(){ this.failedCountMod.set(0);}

    public void setLastUpdated(Timestamp ts ){
        if(ts.getTime() > this.lastUpdatedMod.get()){
            this.lastUpdatedMod.set(ts.getTime());
        }
    }
    public Timestamp getLastUpdated(){ return new Timestamp(this.lastUpdatedMod.get());}

    //
    public void setNextAvailable(Timestamp ts ){
        System.out.println("Setting proxy to " + ts);

        if(ts.getTime() > this.nextAvailableMod.get()) {
            this.nextAvailableMod.set(ts.getTime());
        }
    }
    public Timestamp getNextAvailable(){ return new Timestamp(this.nextAvailableMod.get());}


    public int getID(){  return this.id; }


    public boolean isSocks() {
        return this.isSocks;
    }
}