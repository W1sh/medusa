package com.w1sh.medusa.data.responses;

import discord4j.core.object.entity.MessageChannel;

import java.util.Objects;

public abstract class Response implements Comparable<Response>{

    private MessageChannel channel;
    private boolean fragment;
    private Integer order;

    public Response(MessageChannel channel, boolean fragment) {
        this.channel = channel;
        this.fragment = fragment;
        this.order = 1;
    }

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

    public Integer getOrder() {
        return order;
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
