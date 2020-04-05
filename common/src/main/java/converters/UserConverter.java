package converters;

import com.w1sh.medusa.data.Audit;
import com.w1sh.medusa.data.User;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.r2dbc.mapping.SettableValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserConverter {

    private UserConverter(){}

    @ReadingConverter
    public static class UserReadConverter implements Converter<Row, User> {

        public User convert(Row source) {
            User user = new User();
            user.setId(source.get("id", Long.class));
            user.setUserId(source.get("user_id", Long.class));
            user.setRolls(source.get("rolls", Long.class));
            user.setDuelRolls(source.get("duel_rolls", Long.class));
            user.setPoints(source.get("points", Long.class));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            Audit audit = new Audit();

            String inboundCreatedOn = source.get("created_on", String.class);
            LocalDateTime createdOn = inboundCreatedOn != null ? LocalDateTime.parse(inboundCreatedOn, formatter) : null;
            audit.setCreatedOn(createdOn);

            String inboundUpdatedOn = source.get("updated_on", String.class);
            LocalDateTime updatedOn = inboundUpdatedOn != null ? LocalDateTime.parse(inboundUpdatedOn, formatter) : null;
            audit.setUpdatedOn(updatedOn);

            user.setAudit(audit);
            return user;
        }
    }

    @WritingConverter
    public static class UserWriteConverter implements Converter<User, OutboundRow> {

        public OutboundRow convert(User source) {
            OutboundRow row = new OutboundRow();
            row.put("id", SettableValue.fromOrEmpty(source.getId(), Long.class));
            row.put("user_id", SettableValue.from(source.getUserId()));
            row.put("rolls", SettableValue.from(source.getRolls()));
            row.put("duel_rolls", SettableValue.from(source.getDuelRolls()));
            row.put("points", SettableValue.from(source.getPoints()));
            row.put("created_on", SettableValue.fromOrEmpty(source.getAudit().getCreatedOn(), LocalDateTime.class));
            row.put("updated_on", SettableValue.fromOrEmpty(source.getAudit().getUpdatedOn(), LocalDateTime.class));
            return row;
        }
    }
}
