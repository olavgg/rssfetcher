package com.capgemini;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.*;

/**
 * User: olavgjerde
 * Date: 08/08/14
 * Time: 12:26
 */
public class RSSFetcher {

    public static void main(String[] args){

        AppDB.initDBPool();

        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        Future<Void> hashIndexTash = executorService.submit(() -> {
            FeedHandler.loadHashIndex();
            return null;
        });

        Set<Callable<Void>> callables = new HashSet<>();

        FeedHandler feedHandler = FeedHandler.getInstance();
        Properties properties = feedHandler.getProperties();
        for(String key : properties.stringPropertyNames()) {
            final String url = properties.getProperty(key);
            callables.add(() -> {
                feedHandler.downloadRSSFeed(url);
                return null;
            });
        }

        List<Future<Void>> futures = null;
        try {
            hashIndexTash.get();
            futures = executorService.invokeAll(callables);
            for(Future<Void> future : futures) {
                Void result = future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        AppDB.closeDB();
    }
}
