package com.w1sh.medusa.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@Document
public class User {

    @Id
    private String id;

    private String userId;

    private String guildId;

    private Long points;

    private Instant createdOn;

    private Instant updatedOn;

    public User(String userId, String guildId) {
        this.userId = userId;
        this.guildId = guildId;
    }

    public Long getPoints() {
        return points != null ? points : 0;
    }

}
