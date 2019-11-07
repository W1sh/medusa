package com.w1sh.medusa.entity.entities;

import discord4j.core.object.entity.Member;

import javax.persistence.*;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"discord_id", "guild_id"})
})
@NamedQuery(name = "User.findAll", query = "select u from User u")
@NamedQuery(name = "User.isPresentInGuildById", query = "select count(u) from User u where u.guildId = :gId and u.discordId = :dId")
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
        this.id = 0;
        this.discordId = member.getId().asLong();
        this.name = member.getUsername();
        this.discriminator = member.getDiscriminator();
        this.guildId = member.getGuildId().asLong();
        this.points = 0;
    }

    public User(long discordId, long guildId){
        this.discordId = discordId;
        this.name = "me";
        this.discriminator = "0001";
        this.guildId = guildId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDiscordId() {
        return discordId;
    }

    public void setDiscordId(long discordId) {
        this.discordId = discordId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
