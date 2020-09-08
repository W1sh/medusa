package com.w1sh.medusa.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Document
public class Channel {

    @Id
    private String id;

    @Indexed(unique = true)
    private String channelId;

    private String guildId;

    private List<Rule> rules;

    private List<String> blocklist;

    @CreatedDate
    private Instant createdOn;

    @LastModifiedDate
    private Instant updatedOn;

    public Channel(String channelId, String guildId) {
        this.channelId = channelId;
        this.guildId = guildId;
        this.rules = new ArrayList<>();
        this.blocklist = new ArrayList<>();
    }
}
