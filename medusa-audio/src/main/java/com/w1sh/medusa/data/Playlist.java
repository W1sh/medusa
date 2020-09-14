package com.w1sh.medusa.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@Document
public final class Playlist {

    @Id
    private String id;

    private String userId;

    private String name;

    private List<Track> tracks;

    @CreatedDate
    private Instant createdOn;

    @LastModifiedDate
    private Instant updatedOn;

    public Playlist(String user, String name, List<Track> tracks) {
        this.name = name;
        this.userId = user;
        this.tracks = tracks;
    }

    public Long getFullDuration(){
        if(tracks == null) return 0L;
        return tracks.stream()
                .map(Track::getDuration)
                .reduce(Long::sum).orElse(0L);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist playlist = (Playlist) o;
        return Objects.equals(id, playlist.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
