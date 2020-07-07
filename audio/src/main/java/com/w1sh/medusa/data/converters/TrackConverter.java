package com.w1sh.medusa.data.converters;

import com.w1sh.medusa.data.Track;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.r2dbc.mapping.SettableValue;
import org.springframework.stereotype.Component;

public final class TrackConverter {

    private TrackConverter() {}

    @Component
    @ReadingConverter
    public static class TrackReadConverter implements Converter<Row, Track> {

        @Override
        public Track convert(Row source) {
            Track track = new Track();
            track.setId(source.get("id", Integer.class));
            track.setAuthor(source.get("author", String.class));
            track.setTitle(source.get("title", String.class));
            track.setUri(source.get("uri", String.class));
            track.setDuration(source.get("duration", Long.class));
            return track;
        }
    }

    @Component
    @WritingConverter
    public static class TrackWriteConverter implements Converter<Track, OutboundRow> {

        @Override
        public OutboundRow convert(Track source) {
            OutboundRow row = new OutboundRow();
            row.put("id", SettableValue.fromOrEmpty(source.getId(), Integer.class));
            row.put("author", SettableValue.fromOrEmpty(source.getAuthor(), String.class));
            row.put("title", SettableValue.fromOrEmpty(source.getTitle(), String.class));
            row.put("uri", SettableValue.fromOrEmpty(source.getUri(), String.class));
            row.put("duration", SettableValue.fromOrEmpty(source.getDuration(), Long.class));
            return row;
        }
    }
}
