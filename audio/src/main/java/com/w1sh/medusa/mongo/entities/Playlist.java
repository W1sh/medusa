package com.w1sh.medusa.mongo.entities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document
public class Playlist {

    @Id
    private Integer id;
    private Long user;
    private Set<AudioTrack> tracks;
}
