package com.w1sh.medusa.mongo.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public final class Playlist {

    @Id
    private String id;
    private Long user;
    private List<String> tracks;

    public Playlist(Long user, List<String> tracks) {
        this.user = user;
        this.tracks = tracks;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUser() {
        return user;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    public List<String> getTracks() {
        return tracks;
    }

    public void setTracks(List<String> tracks) {
        this.tracks = tracks;
    }
}
