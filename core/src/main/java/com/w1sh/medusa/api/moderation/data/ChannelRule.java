package com.w1sh.medusa.api.moderation.data;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(value = "core.channels_rules")
public class ChannelRule {

    @Id
    private Integer id;

    @Column(value = "channel_id")
    private String channel;

    @Column(value = "rule_id")
    private Rule rule;
}
