package com.w1sh.medusa.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document
public class Warning {

    @Id
    private String id;

    private String userId;

    private String channelId;

    private String guildId;

    public Warning(String userId, String channelId, String guildId) {
        this.userId = userId;
        this.channelId = channelId;
        this.guildId = guildId;
    }
}
