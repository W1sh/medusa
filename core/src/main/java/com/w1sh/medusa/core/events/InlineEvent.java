package com.w1sh.medusa.core.events;

import discord4j.core.event.domain.message.MessageCreateEvent;

public abstract class InlineEvent extends Event {

    private boolean fragment;
    private String inlinePrefix;
    private String inlineArgument;
    private Integer inlineOrder;

    public InlineEvent(MessageCreateEvent event, String inlinePrefix) {
        super(event);
        this.inlinePrefix = inlinePrefix;
    }

    public boolean isFragment() {
        return fragment;
    }

    public void setFragment(boolean fragment) {
        this.fragment = fragment;
    }

    public String getInlinePrefix() {
        return inlinePrefix;
    }

    public void setInlinePrefix(String inlinePrefix) {
        this.inlinePrefix = inlinePrefix;
    }

    public String getInlineArgument() {
        return inlineArgument;
    }

    public void setInlineArgument(String inlineArgument) {
        this.inlineArgument = inlineArgument;
    }

    public Integer getInlineOrder() {
        return inlineOrder;
    }

    public void setInlineOrder(Integer inlineOrder) {
        this.inlineOrder = inlineOrder;
    }
}
