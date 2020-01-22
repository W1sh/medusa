package com.w1sh.medusa.mongo.entities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
public final class Playlist {

    @Id
    private String id;
    private Long user;
    private List<Track> tracks;

    public Playlist(Long user, List<AudioTrack> tracks) {
        this.id = ObjectId.get().toString();
        this.user = user;
        this.tracks = new ArrayList<>();
        tracks.forEach(track -> this.tracks.add(
                new Track(track.getInfo().author, track.getInfo().title, track.getInfo().uri, track.getInfo().length)));
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

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }
}
