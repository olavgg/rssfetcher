package com.capgemini;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User: olavgjerde
 * Date: 12/08/14
 * Time: 13:12
 */
public class Feed {

    private String title;
    private String link;
    private String author;
    private Timestamp publishedDate;
    private String description;
    private Timestamp dateCreated;
    private String hash;

    private String articleContent;

    public Feed(){

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Timestamp getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(Timestamp publishedDate) {
        this.publishedDate = publishedDate;
    }

    public void setPublishedDate(Date publishedDate) {
        this.publishedDate = new Timestamp(
                publishedDate == null ?
                        new Timestamp(0).getTime() : publishedDate.getTime());
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getDateCreated() {
        return dateCreated == null ?
                new Timestamp(new Date().getTime()) : dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getArticleContent() {
        return articleContent;
    }

    public void setArticleContent(String articleContent) {
        this.articleContent = articleContent;
    }

    public String getHash() {
        if(hash == null){
            setHash();
        }
        return hash;
    }

    public void setHash() {
        this.hash = Hashing.sha1()
                .hashString(this.link, Charsets.UTF_8)
                .toString();
    }

    public Map<Object, Object> getProperties(){
        Map<Object, Object> map = new HashMap<>();
        map.put("title", getTitle());
        map.put("link", getLink());
        map.put("publishedDate", getPublishedDate());
        map.put("author", getAuthor());
        map.put("description", getDescription());
        map.put("dateCreated", getDateCreated());
        map.put("hash", getHash());
        map.put("article_content", getArticleContent());
        return map;
    }

    public boolean save(){
        FeedDOA feedDOA = new FeedDOA(this);
        return feedDOA.save();
    }
}
