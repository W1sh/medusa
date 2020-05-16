package com.w1sh.medusa.converters;

import com.w1sh.medusa.data.User;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.r2dbc.mapping.SettableValue;

public class UserConverter {

    private UserConverter(){}

    @ReadingConverter
    public static class UserReadConverter implements Converter<Row, User> {

        public User convert(Row source) {
            User user = new User();
            user.setId(source.get("id", Integer.class));
            user.setUserId(source.get("user_id", String.class));
            return user;
        }
    }

    @WritingConverter
    public static class UserWriteConverter implements Converter<User, OutboundRow> {

        public OutboundRow convert(User source) {
            OutboundRow row = new OutboundRow();
            row.put("id", SettableValue.fromOrEmpty(source.getId(), Integer.class));
            row.put("user_id", SettableValue.from(source.getUserId()));
            return row;
        }
    }
}
