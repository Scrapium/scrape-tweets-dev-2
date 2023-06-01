package com.scrapium.proxium;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Proxy {

    private int id;
    private String connectionString;
    private AtomicInteger usageCount;
    private AtomicInteger successCount;
    private AtomicInteger failedCount;
    private AtomicInteger failStreak;
    private AtomicLong cooldownUntil;


    public Proxy(int id, String connectionString, int _usageCount, int _successCount, int _failedCount, int _failStreak, Timestamp _cooldownUntil) {
        this.id = id;
        this.connectionString = connectionString;
        this.usageCount = new AtomicInteger(_usageCount);
        this.successCount = new AtomicInteger(_successCount);
        this.failedCount = new AtomicInteger(_failedCount);
        this.failStreak = new AtomicInteger(_failStreak);

        long coolUntil = ( _cooldownUntil == null ) ? System.currentTimeMillis() : _cooldownUntil.getTime();

        this.cooldownUntil = new AtomicLong(coolUntil);

    }

    public void onSuccess(){
        this.usageCount.incrementAndGet();
        this.successCount.incrementAndGet();
        this.failStreak.set(0);
        this.cooldownUntil.set(System.currentTimeMillis());
    }

    public void onFailure() {
        this.usageCount.incrementAndGet();
        this.failedCount.incrementAndGet();
        this.failStreak.incrementAndGet();

        int baseCooldownTime = 1000;
        double exponentialFactor = 1.5;
        long cooldownTime = baseCooldownTime * (long) Math.pow(exponentialFactor, failStreak.get() - 1);
        long cooldownUntil = System.currentTimeMillis() + cooldownTime;
        // TODO: check new cooldownuntil time is not more than 1 hour

        //System.out.println("back off time = " + (cooldownTime / 1000));

        if(cooldownUntil > this.cooldownUntil.get()){
            this.cooldownUntil.set(System.currentTimeMillis() + cooldownTime);
        }
    }

    public String getConnectionString(){
        return this.connectionString;
    }
    public String getIP() {
        return extractWithPattern(this.connectionString, "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})");
    }
    public int getPort() {
        return Integer.valueOf(extractWithPattern(this.connectionString, "(?<=:)(\\d+)"));
    }

    public static String extractWithPattern(String input, String pattern) {
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(input);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }


    public int getUsageCount() {
        return this.usageCount.get();
    }

    public int getSuccessCount() {
        return this.successCount.get();
    }

    public int getFailedCount() {
        return this.failedCount.get();
    }

    public int getFailStreak() {
        return this.failStreak.get();
    }

    public Timestamp getCooldownUntil() {
        return new Timestamp(this.cooldownUntil.get());
    }

    public int getID() {
        return this.id;
    }

    public void debug_incrementUsageCount() {
        this.usageCount.incrementAndGet();
    }
}