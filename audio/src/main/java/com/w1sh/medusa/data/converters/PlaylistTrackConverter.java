package com.w1sh.medusa.data.converters;

import com.w1sh.medusa.data.PlaylistTrack;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.r2dbc.mapping.SettableValue;
import org.springframework.stereotype.Component;

public class PlaylistTrackConverter {

    private PlaylistTrackConverter() {}

    @Component
    @ReadingConverter
    public static class PlaylistTrackReadConverter implements Converter<Row, PlaylistTrack> {

        @Override
        public PlaylistTrack convert(Row source) {
            PlaylistTrack playlist = new PlaylistTrack();
            playlist.setId(source.get("id", Integer.class));
            playlist.setPlaylistId(source.get("fk_playlist", Integer.class));
            playlist.setTrackId(source.get("fk_track", Integer.class));
            return playlist;
        }
    }

    @Component
    @WritingConverter
    public static class PlaylistTrackWriteConverter implements Converter<PlaylistTrack, OutboundRow> {

        @Override
        public OutboundRow convert(PlaylistTrack source) {
            OutboundRow row = new OutboundRow();
            row.put("id", SettableValue.fromOrEmpty(source.getId(), Integer.class));
            row.put("fk_playlist", SettableValue.from(source.getPlaylistId()));
            row.put("fk_track", SettableValue.from(source.getTrackId()));
            return row;
        }
    }
}
