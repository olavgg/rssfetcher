package com.capgemini;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import groovy.sql.Sql;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

    private static final Logger log = Logger.getLogger(FeedHandler.class);

    public static Map<String, String> hashIndex = new ConcurrentHashMap<>();

    public static void loadHashIndex(){
        Sql session = AppDB.getSession();
        hashIndex.clear();
        try {
            session.rows("SELECT hash FROM news_article LIMIT 10000")
                    .parallelStream()
                    .forEach( row ->
                            hashIndex.put((String)row.getProperty("hash"), ""));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("SQL query failed...");
        }
    }

    private FeedHandler(){}

    public static FeedHandler getInstance(){
        return new FeedHandler();
    }

    public void downloadRSSFeed(String stringUrl){
        log.info(stringUrl);
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
                feed.setArticleContent(getArticleContent(feed.getLink()));
                feed.save();
                hashIndex.put(feed.getHash(), "");
            }

        } catch (IOException | FeedException e) {
            e.printStackTrace();
        }
    }

    private String getArticleContent(String link) throws IOException{
        StringBuilder stringBuffer = new StringBuilder();
        Document doc = Jsoup.connect(link).get();
        Elements paragraphs = doc.select("p");
        for(Element element: paragraphs){
            stringBuffer.append(element.text());
            stringBuffer.append(" ");
        }
        return stringBuffer.toString();
    }

}
