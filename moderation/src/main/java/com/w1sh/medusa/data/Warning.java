package com.w1sh.medusa.data;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@Document
public class Warning {

    @Id
    private String id;

    private String userId;

    private String channelId;

    private String guildId;

    @CreatedDate
    private Instant createdOn;

    public Warning(MessageCreateEvent event) {
        this.channelId = event.getMessage().getChannelId().asString();
        this.userId = event.getMember().map(member -> member.getId().asString()).orElse("");
        this.guildId = event.getGuildId().map(Snowflake::asString).orElse("");
    }
}
