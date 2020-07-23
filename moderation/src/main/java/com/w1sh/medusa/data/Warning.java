package com.w1sh.medusa.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document
public class Warning {

    @Id
    private String id;

    @Column(value = "user_id")
    private String userId;

    @Column(value = "channel_id")
    private String channelId;

    @Column(value = "guild_id")
    private String guildId;

    @CreatedDate
    @Column(value = "created_on")
    private LocalDateTime createdOn;

    public Warning(String userId, String channelId, String guildId) {
        this.userId = userId;
        this.channelId = channelId;
        this.guildId = guildId;
    }
}
