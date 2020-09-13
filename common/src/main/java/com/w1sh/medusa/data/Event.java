package com.w1sh.medusa.data;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.gateway.ShardInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A representation of a command that was directed at the bot.
 *
 * {@link #guildId} and {@link #member} may not be present if the message was sent in a private channel.
 */
@Data
@Document
@NoArgsConstructor
public class Event {

    @Id
    private String id;

    private String userId;

    private String guildId;

    private Instant createdOn;

    @Transient
    private Member member;

    @Transient
    private Message message;

    @Transient
    private ShardInfo shardInfo;

    @Transient
    private GatewayDiscordClient client;

    @Transient
    private List<String> arguments;

    public Event(MessageCreateEvent event){
        this.userId = event.getMember().map(User::getId).map(Snowflake::asString).orElse(null);
        this.guildId = event.getGuildId().map(Snowflake::asString).orElse(null);
        this.message = event.getMessage();
        this.shardInfo = event.getShardInfo();
        this.member = event.getMember().orElse(null);
        this.client = event.getClient();
        this.arguments = new ArrayList<>();
        this.createdOn = Instant.now();

        Objects.requireNonNull(userId);
        Objects.requireNonNull(guildId);
    }

    /**
     * Gets the {@link MessageChannel} the {@link Message} was created in, if present.
     * This may not be available if the {@code Message} was sent in a private channel.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link MessageChannel} the message was created in,
     * if present.
     */
    public Mono<MessageChannel> getChannel(){
        return message.getChannel();
    }

    /**
     * Gets the {@link GuildChannel} the {@link Message} was created in, if present.
     * This may not be available if the {@code Message} was sent in a private channel.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildChannel} the message was created in,
     * if present.
     */
    public Mono<GuildChannel> getGuildChannel(){
        return message.getChannel().ofType(GuildChannel.class);
    }

    /**
     * Gets the {@link Guild} the {@link Message} was created in, if present.
     * This may not be available if the {@code Message} was sent in a private channel.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} the message was created in,
     * if present.
     */
    public Mono<Guild> getGuild(){
        return message.getGuild();
    }

    /**
     * Gets the channelId of the {@link Channel} where the message was sent.
     *
     * @return The channelId of the {@link Channel}.
     */
    public String getChannelId(){
        return message.getChannelId().asString();
    }

    /**
     * Gets the nickname of the {@link Member} that sent the message, if present. Otherwise gets the display name
     * of the {@link Member}.
     *
     * @return The nickname of the {@link Member}, if present. Otherwise, the display name.
     */
    public String getNickname(){
        if (member == null) return "";
        return member.getNickname().orElse(member.getDisplayName());
    }
}
