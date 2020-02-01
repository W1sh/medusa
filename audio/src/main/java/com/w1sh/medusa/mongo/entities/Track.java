package com.w1sh.medusa.mongo.entities;

import org.springframework.data.annotation.Id;

public final class Track {

    @Id
    private String id;
    private String author;
    private String title;
    private String uri;
    private Long duration;

    public Track(String author, String title, String uri, Long duration) {
        this.author = author;
        this.title = title;
        this.uri = uri;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
}
