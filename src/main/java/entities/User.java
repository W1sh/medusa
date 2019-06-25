package entities;

import discord4j.core.object.entity.Member;

import javax.persistence.*;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"discord_id", "guild_id"})
})
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private int id;

    @Column(name = "discord_id", updatable = false, nullable = false)
    private long discordId;

    @Column(updatable = false, nullable = false)
    private String name;

    @Column(updatable = false, nullable = false)
    private String discriminator;

    @Column(name = "guild_id", updatable = false, nullable = false)
    private long guildId;

    private int points;

    public User() { }

    public User(Member member) {
        this.discordId = member.getId().asLong();
        this.name = member.getUsername();
        this.discriminator = member.getDiscriminator();
        this.guildId = member.getGuildId().asLong();
        this.points = 0;
    }

    public int getId() {
        return id;
    }

    public long getDiscordId() {
        return discordId;
    }

    public String getName() {
        return name;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public long getGuildId() {
        return guildId;
    }

    public int getPoints() {
        return points;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", discordId=" + discordId +
                ", name='" + name + '\'' +
                ", discriminator='" + discriminator + '\'' +
                ", guild=" + guildId +
                ", points=" + points +
                '}';
    }

}
