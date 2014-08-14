package com.capgemini

import groovy.sql.Sql
import io.searchbox.client.JestClient
import io.searchbox.client.JestResult
import io.searchbox.core.Index
import org.apache.log4j.Logger

import java.text.SimpleDateFormat

class FeedDOA{

    private static final Logger log = Logger.getLogger(FeedDOA.class)
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Feed feed;

    public FeedDOA(Feed feed){
        this.feed = feed;
    }

    public boolean save(){
        Sql db = AppDB.getSession()
        db.withTransaction {
            boolean inserted =
                    AppDB.execute(
                            "INSERT INTO news_article (" +
                                    "title, link, published_date, " +
                                    "author, description, " +
                                    "date_created, hash, article_content) " +
                            "VALUES(" +
                                    ":title, :link, :publishedDate, :author, " +
                                    ":description, :dateCreated, :hash, " +
                                    ":article_content)",
                            feed.getProperties()
                    )
            if (inserted) {
                JestClient client = ESHandler.getConnection()
                Map<Object, Object> articleMap = feed.getProperties();
                articleMap.put(
                        "dateCreated",
                        DATE_FORMAT.format(articleMap.get("dateCreated")))
                articleMap.put(
                        "publishedDate",
                        DATE_FORMAT.format(articleMap.get("publishedDate")))
                Index index = new Index.Builder(articleMap)
                        .index("articles")
                        .type("article")
                        .build();
                try{
                    JestResult result = client.execute(index);
                    if(result.succeeded){
                        db.commit()
                    } else {
                        throw new RuntimeException(result.errorMessage)
                    }
                } catch (Exception e){
                    log.error(e.message)
                    db.rollback()
                }
            } else {
                db.rollback()
            }
        }
    }
}