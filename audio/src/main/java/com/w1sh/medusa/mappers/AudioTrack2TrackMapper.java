package com.w1sh.medusa.mappers;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.w1sh.medusa.data.Track;
import org.springframework.stereotype.Component;

@Component
public final class AudioTrack2TrackMapper implements Mapper<AudioTrack, Track> {

    @Override
    public Track map(AudioTrack source) {
        return map(source, new Track());
    }

    @Override
    public Track map(AudioTrack source, Track destination) {
        destination.setAuthor(source.getInfo().author);
        destination.setDuration(source.getInfo().length);
        destination.setTitle(source.getInfo().title);
        destination.setUri(source.getInfo().uri);
        return destination;
    }
}
