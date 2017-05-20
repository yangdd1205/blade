package com.blade.demo.model;

import java.io.Serializable;

/**
 * @author biezhi
 *         2017/5/20
 */
public class Article implements Serializable {

    private String title;
    private String author;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "Article(" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ')';
    }
}
