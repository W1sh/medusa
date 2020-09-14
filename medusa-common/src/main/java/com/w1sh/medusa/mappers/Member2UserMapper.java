package com.w1sh.medusa.mappers;

import com.w1sh.medusa.data.User;
import discord4j.core.object.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class Member2UserMapper implements Mapper<Member, User> {

    @Override
    public User map(Member source) {
        return map(source, new User());
    }

    @Override
    public User map(Member source, User destination) {
        String guildId = String.valueOf(source.getGuildId().asLong());
        destination.setGuildId(guildId);
        String userId = String.valueOf(source.getId().asLong());
        destination.setUserId(userId);
        return destination;
    }
}
