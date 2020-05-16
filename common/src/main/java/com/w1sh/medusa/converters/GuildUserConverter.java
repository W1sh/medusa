package com.w1sh.medusa.converters;

import com.w1sh.medusa.data.Audit;
import com.w1sh.medusa.data.GuildUser;
import com.w1sh.medusa.data.User;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.r2dbc.mapping.SettableValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GuildUserConverter {

    private GuildUserConverter(){}

    @ReadingConverter
    public static class GuildUserReadConverter implements Converter<Row, GuildUser> {

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        @Override
        public GuildUser convert(Row source) {
            GuildUser user = new GuildUser();
            user.setId(source.get("id", Integer.class));
            user.setUser(source.get("user", User.class));
            user.setPoints(source.get("points", Long.class));

            Audit audit = new Audit();

            String inboundCreatedOn = source.get("created_on", String.class);
            LocalDateTime createdOn = inboundCreatedOn != null ? LocalDateTime.parse(inboundCreatedOn, FORMATTER) : null;
            audit.setCreatedOn(createdOn);

            String inboundUpdatedOn = source.get("updated_on", String.class);
            LocalDateTime updatedOn = inboundUpdatedOn != null ? LocalDateTime.parse(inboundUpdatedOn, FORMATTER) : null;
            audit.setUpdatedOn(updatedOn);

            user.setAudit(audit);
            return user;
        }
    }

    @WritingConverter
    public static class GuildUserWriteConverter implements Converter<GuildUser, OutboundRow> {

        public OutboundRow convert(GuildUser source) {
            OutboundRow row = new OutboundRow();
            row.put("id", SettableValue.fromOrEmpty(source.getId(), Integer.class));
            row.put("user", SettableValue.from(source.getUser().getId()));
            row.put("guild_id", SettableValue.from(source.getGuildId()));
            row.put("points", SettableValue.from(source.getPoints()));
            row.put("created_on", SettableValue.fromOrEmpty(source.getAudit().getCreatedOn(), LocalDateTime.class));
            row.put("updated_on", SettableValue.fromOrEmpty(source.getAudit().getUpdatedOn(), LocalDateTime.class));
            return row;
        }
    }
}
