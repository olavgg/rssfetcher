package com.capgemini;

import groovy.sql.GroovyRowResult;
import groovy.sql.Sql;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: olavgjerde
 * Date: 25/08/14
 * Time: 10:53
 */
public class ReIndex {

    private static final Logger log = Logger.getLogger(ReIndex.class);
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        new AppLogger();
        AppDB.initDBPool();
        if(!ESHandler.initIndex()){
            System.exit(0);
        }
        reindex();
    }

    public static void reindex(){
        Sql session = AppDB.getSession();
        try {
            session.rows("SELECT title, author, article_content, " +
                    "date_created, description, hash, link, published_date " +
                    "FROM news_article")
                    .stream()
                    .forEach(ReIndex::insertDocument);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("SQL query failed...");
        }
    }

    public static void insertDocument(GroovyRowResult row){
        if(documentExists((String)row.getProperty("hash"))){
            return;
        }
        JestClient client = ESHandler.getConnection();
        Map<Object, Object> articleMap = new HashMap<>();
        articleMap.put(
                "dateCreated",
                DATE_FORMAT.format(row.getProperty("date_created")));
        articleMap.put(
                "publishedDate",
                DATE_FORMAT.format(row.getProperty("published_date")));
        articleMap.put("title", row.getProperty("title"));
        articleMap.put("description", row.getProperty("description"));
        articleMap.put("article_content", row.getProperty("article_content"));
        articleMap.put("author",row.getProperty("author"));
        articleMap.put("link", row.getProperty("link"));
        articleMap.put("hash", row.getProperty("hash"));
        Index index = new Index.Builder(articleMap)
                .index("articles")
                .type("article")
                .build();
        try{
            JestResult result = client.execute(index);
            if(!result.isSucceeded()){
                log.info("inserting document failed.");
                System.out.println(result.getErrorMessage());
            }
        } catch (Exception e){
            log.error("connection error");
            log.error(e.getMessage());
        }
    }

    public static boolean documentExists(String hash){
        JestClient client = ESHandler.getConnection();
        String query = "{\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must\": [\n" +
                "        {\n" +
                "          \"term\": {\n" +
                "            \"article.hash\": \""+ hash +"\"\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";
        Search search = new Search.Builder(query)
                .addIndex("articles")
                .build();
        try {
            SearchResult result = client.execute(search);
            List hits = result.getHits(Object.class);
            if(hits.isEmpty()){
                return false;
            }
        } catch(Exception e){
            log.error(e.getMessage());
        }
        return true;
    }
}
