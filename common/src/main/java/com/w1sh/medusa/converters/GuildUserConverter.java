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

public class GuildUserConverter {

    private GuildUserConverter(){}

    @ReadingConverter
    public static class GuildUserReadConverter implements Converter<Row, GuildUser> {

        @Override
        public GuildUser convert(Row source) {
            GuildUser guildUser = new GuildUser();
            guildUser.setId(source.get("id", Integer.class));

            User user = new User();
            user.setId(source.get("fk_user", Integer.class));
            guildUser.setUser(user);

            guildUser.setGuildId(source.get("guild_id", String.class));
            guildUser.setPoints(source.get("points", Long.class));

            Audit audit = new Audit();
            audit.setCreatedOn(source.get("created_on", LocalDateTime.class));
            audit.setUpdatedOn(source.get("updated_on", LocalDateTime.class));
            guildUser.setAudit(audit);

            return guildUser;
        }
    }

    @WritingConverter
    public static class GuildUserWriteConverter implements Converter<GuildUser, OutboundRow> {

        public OutboundRow convert(GuildUser source) {
            OutboundRow row = new OutboundRow();
            row.put("id", SettableValue.fromOrEmpty(source.getId(), Integer.class));
            row.put("fk_user", SettableValue.from(source.getUser().getId()));
            row.put("guild_id", SettableValue.from(source.getGuildId()));
            row.put("points", SettableValue.from(source.getPoints()));
            row.put("created_on", SettableValue.from(source.getAudit().getCreatedOn()));
            row.put("updated_on", SettableValue.from(LocalDateTime.now()));
            return row;
        }
    }
}
