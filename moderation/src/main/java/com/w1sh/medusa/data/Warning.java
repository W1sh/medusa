package com.w1sh.medusa.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Table(value = "core.warnings")
public class Warning {

    @Id
    private Integer id;

    @Column(value = "fk_user")
    private User user;

    @Column(value = "channel_id")
    private String channelId;

    @Column(value = "created_on")
    private LocalDateTime createdOn;

    public Warning(User user, String channelId) {
        this.user = user;
        this.channelId = channelId;
    }
}
