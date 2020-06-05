package com.w1sh.medusa.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;
import java.util.Objects;

@Table(value = "core.playlists")
public final class Playlist {

    @Id
    private Integer id;

    @Column(value = "fk_user")
    private User user;

    private String name;

    @Transient
    private List<Track> tracks;

    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY)
    private Audit audit;

    public Playlist() { }

    public Playlist(String user, String name, List<Track> tracks) {
        this.name = name;
        this.user = new User();
        this.user.setUserId(user);
        this.tracks = tracks;
        this.audit = new Audit();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
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
