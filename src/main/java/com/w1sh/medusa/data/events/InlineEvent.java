package com.w1sh.medusa.data.events;

import com.w1sh.medusa.data.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

import java.util.Objects;

public abstract class InlineEvent extends Event {

    public static final int MAX_ALLOWED_LENGTH = 128;

    private boolean fragment;

    private String inlineArgument;

    private Integer inlineOrder;

    public InlineEvent(MessageCreateEvent event) {
        super(event);
    }

    public boolean isInvalid(){
        return inlineArgument == null || inlineArgument.isBlank() || inlineArgument.length() > MAX_ALLOWED_LENGTH;
    }

    public boolean isFragment() {
        return this.fragment;
    }

    public String getInlineArgument() {
        return this.inlineArgument;
    }

    public Integer getInlineOrder() {
        return this.inlineOrder;
    }

    public void setFragment(boolean fragment) {
        this.fragment = fragment;
    }

    public void setInlineArgument(String inlineArgument) {
        this.inlineArgument = inlineArgument;
    }

    public void setInlineOrder(Integer inlineOrder) {
        this.inlineOrder = inlineOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InlineEvent that = (InlineEvent) o;
        return fragment == that.fragment && Objects.equals(inlineArgument, that.inlineArgument) &&
                Objects.equals(inlineOrder, that.inlineOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fragment, inlineArgument, inlineOrder);
    }

    public String toString() {
        return "InlineEvent(fragment=" + this.isFragment() + ", inlineArgument=" + this.getInlineArgument() +
                ", inlineOrder=" + this.getInlineOrder() + ")";
    }
}
