package com.w1sh.medusa.mappers;

import com.w1sh.medusa.data.GuildUser;
import com.w1sh.medusa.data.User;
import discord4j.core.object.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class Member2GuildUserMapper implements Mapper<Member, GuildUser> {

    @Override
    public GuildUser map(Member source) {
        return map(source, new GuildUser());
    }

    @Override
    public GuildUser map(Member source, GuildUser destination) {
        String guildId = String.valueOf(source.getGuildId().asLong());
        destination.setGuildId(guildId);
        String userId = String.valueOf(source.getId().asLong());
        destination.setUser(new User(userId));
        return destination;
    }
}
