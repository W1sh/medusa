package com.w1sh.medusa.converters;

import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.data.RuleEnum;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.data.Warning;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.r2dbc.mapping.SettableValue;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

public final class WarningConverter {

    private WarningConverter(){}

    @Component
    @ReadingConverter
    public static class WarningReadConverter implements Converter<Row, Warning> {

        public Warning convert(Row source) {
            Warning warning = new Warning();
            warning.setId(source.get("id", Integer.class));

            User user = new User();
            user.setId(source.get("fk_user", Integer.class));
            warning.setUser(user);

            warning.setChannelId(source.get("channel_id", String.class));
            warning.setCreatedOn(source.get("created_on", LocalDateTime.class));
            return warning;
        }
    }

    @Component
    @WritingConverter
    public static class WarningWriteConverter implements Converter<Warning, OutboundRow> {

        @Override
        public OutboundRow convert(Warning source) {
            OutboundRow row = new OutboundRow();
            row.put("id", SettableValue.fromOrEmpty(source.getId(), Integer.class));
            row.put("fk_user", SettableValue.from(source.getUser().getId()));
            row.put("channel_id", SettableValue.from(source.getChannelId()));
            row.put("created_on", SettableValue.from(LocalDateTime.now()));
            return row;
        }
    }
}
