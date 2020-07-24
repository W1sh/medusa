package com.w1sh.medusa.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Document
public class Channel {

    @Id
    private String id;

    private String channelId;

    private String guildId;

    private List<Rule> rules;

    public Channel(String channelId, String guildId) {
        this.channelId = channelId;
        this.guildId = guildId;
        this.rules = new ArrayList<>();
    }

    public Channel(String channelId, String guildId, Rule rule) {
        this.channelId = channelId;
        this.guildId = guildId;
        this.rules = new ArrayList<>();
        rules.add(rule);
    }
}
