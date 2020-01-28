package com.w1sh.medusa.mongo.entities;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public final class Playlist {

    @Id
    private String id;
    private String name;
    private Long user;
    private List<Track> tracks;

    public Playlist(Long user, String name, List<Track> tracks) {
        this.id = ObjectId.get().toString();
        this.name = name;
        this.user = user;
        this.tracks = tracks;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUser() {
        return user;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }

    public Long getFullDuration(){
        return tracks.stream()
                .map(Track::getDuration)
                .reduce(Long::sum).orElse(0L);
    }

}
