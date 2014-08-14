package com.capgemini;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.IndicesExists;
import io.searchbox.indices.mapping.PutMapping;
import org.apache.log4j.Logger;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;

/**
 * User: olavgjerde
 * Date: 13/08/14
 * Time: 13:06
 */
public class ESHandler {

    private static final Logger log = Logger.getLogger(ESHandler.class);

    private static JestClientFactory factory = new JestClientFactory();
    private static JestClient client;
    public static final String IDX_NAME = "articles";

    public static boolean initIndex(){
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder(
                            "http://"+
                            App.config.getProperty("elasticsearch.host")+
                            ":9200")
                .multiThreaded(true)
                .build());
        client = factory.getObject();
        try {
            IndicesExists indicesExists =
                    new IndicesExists.Builder(IDX_NAME).build();
            JestResult result = client.execute(indicesExists);
            if (!result.isSucceeded()) {
                ESHandler es = new ESHandler();
                if(!es.createIndex() || !es.createMapping()){
                    DeleteIndex deleteIndex = new DeleteIndex.Builder(IDX_NAME).build();
                    try {
                        client.execute(deleteIndex);
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    public static void shutdown(){
        client.shutdownClient();
    }

    public ESHandler(){}

    public static JestClient getConnection(){ return client; }

    private boolean createIndex(){

        ImmutableSettings.Builder settingsBuilder =
                ImmutableSettings.settingsBuilder();
        settingsBuilder.put("number_of_shards", 20);
        settingsBuilder.put("number_of_replicas", 1);
        settingsBuilder.put("refresh_interval", "1s");
        settingsBuilder.put("store.type", "niofs");
        settingsBuilder.put("store.compress.stored", true);
        settingsBuilder.put("store.compress.tv", true);
        settingsBuilder.put("gateway.type", "none");
        settingsBuilder.put(
                "analysis.analyzer.norwegianAnalyzer.type",
                "custom");
        settingsBuilder.put(
                "analysis.analyzer.norwegianAnalyzer.tokenizer",
                "keyword");
        settingsBuilder.put(
                "analysis.analyzer.norwegianAnalyzer.filter",
                "norwegian");
        settingsBuilder.put(
                "analysis.filter.norwegian.type",
                "icu_collation");
        settingsBuilder.put(
                "analysis.filter.norwegian.language",
                "nb");
        try {
            client.execute(
                    new CreateIndex.Builder(IDX_NAME)
                            .settings(settingsBuilder
                                    .build()
                                    .getAsMap())
                            .build());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createMapping() throws Exception{
        XContentBuilder builder;
        try {
            builder = XContentFactory.jsonBuilder()
            .startObject()
                .startObject("article")
                    .startObject("_source")
                        .field("compress", "true")
                    .endObject()
                    .startObject("_all")
                        .field("enabled", "true")
                    .endObject()
                    .startObject("properties")
                        .startObject("id")
                        .field("type", "integer")
                        .field("store", "yes")
                        .field("index", "not_analyzed")
                    .endObject()
                    .startObject("title")
                        .field("type", "string")
                        .field("store", "yes")
                        .field("index", "analyzed")
                        .field("null_value", "")
                    .endObject()
                    .startObject("dateCreated")
                        .field("type", "date")
                        .field("format", "yyyy-MM-dd HH:mm:ss")
                        .field("store", "yes")
                        .field("index", "not_analyzed")
                    .endObject()
                    .startObject("publishedDate")
                        .field("type", "date")
                        .field("format", "yyyy-MM-dd HH:mm:ss")
                        .field("store", "yes")
                        .field("index", "not_analyzed")
                    .endObject()
                    .startObject("author")
                        .field("type", "string")
                        .field("store", "yes")
                        .field("index", "not_analyzed")
                    .endObject()
                    .startObject("description")
                        .field("type", "string")
                        .field("store", "yes")
                        .field("index", "analyzed")
                    .endObject()
                    .startObject("article_content")
                        .field("type", "string")
                        .field("store", "yes")
                        .field("index", "analyzed")
                    .endObject()
                    .startObject("link")
                        .field("type", "string")
                        .field("store", "yes")
                        .field("index", "not_analyzed")
                    .endObject()
                    .startObject("hash")
                        .field("type", "string")
                        .field("store", "yes")
                        .field("index", "not_analyzed")
                    .endObject()
                .endObject()
            .endObject()
        .endObject();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        PutMapping putMapping = new PutMapping.Builder(
                IDX_NAME,
                "article",
                builder.prettyPrint().string()
        ).build();
        JestResult result = client.execute(putMapping);
        if(result.isSucceeded()){
            return true;
        }
        log.error(result.getErrorMessage());
        return false;
    }
}
