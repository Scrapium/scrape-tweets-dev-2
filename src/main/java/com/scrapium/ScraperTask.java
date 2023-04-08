package com.scrapium;

public class ScraperTask implements Runnable {

    private int taskId;

    public ScraperTask(int taskId) {
        this.taskId = taskId;
    }

    @Override
    public void run() {
        // Perform the scraping task here
        System.out.println("Scraping task " + taskId + " completed");
    }
}