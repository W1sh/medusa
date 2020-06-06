package com.w1sh.medusa.data;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(value = "core.guilds_users")
public class GuildUser {

    @Id
    private Integer id;

    @Column(value = "fk_user")
    private User user;

    @Column(value = "guild_id")
    private String guildId;

    private Long points;

    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY)
    private Audit audit;

    public GuildUser() {
        this.audit = new Audit();
    }

    public GuildUser(User user, String guildId) {
        this.user = user;
        this.guildId = guildId;
        this.audit = new Audit();
    }

    public Long getPoints() {
        return points != null ? points : 0;
    }

}
