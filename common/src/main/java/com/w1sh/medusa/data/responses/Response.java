package com.w1sh.medusa.data.responses;

import discord4j.core.object.entity.channel.MessageChannel;
import lombok.Data;

@Data
public abstract class Response implements Comparable<Response>{

    private final MessageChannel channel;
    private final boolean fragment;
    private final Integer order;

    @Override
    public int compareTo(Response response) {
        return this.order.compareTo(response.order);
    }
}
