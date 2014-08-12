package com.capgemini;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import groovy.sql.Sql;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: olavgjerde
 * Date: 08/08/14
 * Time: 14:53
 */
public class FeedHandler {

    public static Map<String, String> hashIndex = new ConcurrentHashMap<>();

    public static void loadHashIndex(){
        Sql session = AppDB.getSession();
        hashIndex.clear();
        try {
            session.rows("SELECT hash FROM news_article")
                    .parallelStream().forEach( row -> {
                hashIndex.put((String)row.getProperty("hash"), "");
            });
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("SQL query failed...");
        }
    }

    private FeedHandler(){}

    public static FeedHandler getInstance(){
        return new FeedHandler();
    }

    public Properties getProperties(){
        final String resourceName = "rss.properties";
        ClassLoader loader =
                Thread.currentThread().getContextClassLoader();
        Properties properties = new Properties();
        try(InputStream resourceStream =
                    loader.getResourceAsStream(resourceName)) {
            properties.load(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public void downloadRSSFeed(String stringUrl){
        System.out.println(stringUrl);
        try {
            URL url = new URL(stringUrl);
            HttpURLConnection httpcon = (HttpURLConnection)url.openConnection();
            // Reading the feed
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed syndFeed = input.build(
                    new InputStreamReader(
                            httpcon.getInputStream()));
            List<SyndEntry> entries = syndFeed.getEntries();
            for(SyndEntry entry : entries){
                Feed feed = new Feed();
                feed.setLink(entry.getLink());
                if(hashIndex.containsKey(feed.getHash())){
                    continue;
                }
                feed.setTitle(entry.getTitle());
                feed.setAuthor(entry.getAuthor());
                feed.setPublishedDate(entry.getPublishedDate());
                feed.setDescription(entry.getDescription().getValue());
                feed.save();
                hashIndex.put(feed.getHash(), "");
            }

        } catch (IOException | FeedException e) {
            e.printStackTrace();
        }
    }

}
