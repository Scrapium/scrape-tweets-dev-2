package com.scrapium;

import com.scrapium.proxium.ProxyService;
import com.scrapium.utils.DebugLogger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxiesThread  extends ThreadBase implements Runnable {


    private final ProxyService proxyService;
    private final Scraper scraper;

    public ProxiesThread(Scraper scraper, ProxyService proxyService) {
        this.scraper = scraper;
        this.proxyService = proxyService;
    }

    @Override
    public void run() {
        while (this.running) {
            //this.proxyService.syncAndRefresh();
            //System.out.println("Synced proxies");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
