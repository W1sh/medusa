package com.w1sh.medusa.mappers;

import com.w1sh.medusa.data.User;
import discord4j.core.object.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class Member2UserMapper implements Mapper<Member, User> {

    @Override
    public User map(Member source, User destination) {
        destination.setUserId(source.getGuildId().asLong());
        return destination;
    }
}
