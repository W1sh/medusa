package com.w1sh.medusa.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.relational.core.mapping.Column;

@Data
@NoArgsConstructor
@Document
public class ChannelRule {

    @Id
    private String id;

    @Column(value = "channel_id")
    private String channel;

    private Rule rule;

    public ChannelRule(String channel, Rule rule) {
        this.channel = channel;
        this.rule = rule;
    }
}
