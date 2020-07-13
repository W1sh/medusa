package com.w1sh.medusa.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Table(value = "core.channels_rules")
public class ChannelRule {

    @Id
    private Integer id;

    @Column(value = "channel_id")
    private String channel;

    @Column(value = "rule_id")
    private Rule rule;

    public ChannelRule(String channel, Rule rule) {
        this.channel = channel;
        this.rule = rule;
    }
}
