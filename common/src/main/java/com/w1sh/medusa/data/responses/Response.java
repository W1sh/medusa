package com.w1sh.medusa.data.responses;

import discord4j.core.object.entity.channel.MessageChannel;

import java.util.Objects;

public abstract class Response implements Comparable<Response>{

    private final MessageChannel channel;
    private final boolean fragment;
    private final Integer order;

    public Response(MessageChannel channel, boolean fragment, Integer order) {
        this.channel = channel;
        this.fragment = fragment;
        this.order = order;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public boolean isFragment() {
        return fragment;
    }

    @Override
    public int compareTo(Response response) {
        return this.order.compareTo(response.order);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response response = (Response) o;
        return fragment == response.fragment &&
                Objects.equals(channel, response.channel) &&
                Objects.equals(order, response.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, fragment, order);
    }
}
