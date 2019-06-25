package entities;

import discord4j.core.object.entity.Member;
import lombok.*;

import javax.persistence.*;

@Entity
@ToString
@EqualsAndHashCode (exclude={"id"})
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"discord_id", "guild_id"})
})
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    @Getter @Setter
    private int id;

    @Column(name = "discord_id", updatable = false, nullable = false)
    @Getter @Setter
    private long discordId;

    @Column(updatable = false, nullable = false)
    @Getter @Setter @NonNull
    private String name;

    @Column(updatable = false, nullable = false)
    @Getter @Setter @NonNull
    private String discriminator;

    @Column(name = "guild_id", updatable = false, nullable = false)
    @Getter @Setter
    private long guildId;

    @Getter @Setter
    private int points;

    public User() { }

    public User(Member member) {
        this.id = 0;
        this.discordId = member.getId().asLong();
        this.name = member.getUsername();
        this.discriminator = member.getDiscriminator();
        this.guildId = member.getGuildId().asLong();
        this.points = 0;
    }
}
