package com.w1sh.medusa.data.converters;

import com.w1sh.medusa.data.Audit;
import com.w1sh.medusa.data.Playlist;
import com.w1sh.medusa.data.User;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.r2dbc.mapping.SettableValue;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

public class PlaylistConverter {

    private PlaylistConverter() {}

    @Component
    @ReadingConverter
    public static class PlaylistReadConverter implements Converter<Row, Playlist> {

        @Override
        public Playlist convert(Row source) {
            Playlist playlist = new Playlist();
            playlist.setId(source.get("id", Integer.class));
            playlist.setName(source.get("name", String.class));

            User user = new User();
            user.setId(source.get("fk_user", Integer.class));
            playlist.setUser(user);

            Audit audit = new Audit();
            audit.setCreatedOn(source.get("created_on", LocalDateTime.class));
            audit.setUpdatedOn(source.get("updated_on", LocalDateTime.class));
            playlist.setAudit(audit);
            return playlist;
        }
    }

    @Component
    @WritingConverter
    public static class PlaylistWriteConverter implements Converter<Playlist, OutboundRow> {

        @Override
        public OutboundRow convert(Playlist source) {
            OutboundRow row = new OutboundRow();
            row.put("id", SettableValue.fromOrEmpty(source.getId(), Integer.class));
            row.put("name", SettableValue.from(source.getName()));
            row.put("user", SettableValue.from(source.getUser().getId()));
            row.put("created_on", SettableValue.from(source.getAudit().getCreatedOn()));
            row.put("updated_on", SettableValue.from(LocalDateTime.now()));
            return row;
        }
    }
}
