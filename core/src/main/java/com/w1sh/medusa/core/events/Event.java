package com.w1sh.medusa.core.events;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;

import java.util.ArrayList;
import java.util.List;

public abstract class Event extends MessageCreateEvent {

    private Integer numAllowedArguments;
    private boolean inline;
    private boolean fragment;
    private String inlinePrefix;
    private String inlineArgument;
    private Integer inlineOrder;
    private List<Permission> permissions;

    public Event(MessageCreateEvent event){
        super(event.getClient(), event.getMessage(), event.getGuildId().map(Snowflake::asLong).orElse(null),
                event.getMember().orElse(null));
        this.permissions = new ArrayList<>();
        this.permissions.add(Permission.SEND_MESSAGES);
        this.fragment = false;
    }

    public Event(MessageCreateEvent event, Integer numAllowedArguments) {
        this(event);
        this.numAllowedArguments = numAllowedArguments;
    }

    public Event(MessageCreateEvent event, List<Permission> permissions){
        this(event);
        this.permissions.addAll(permissions);
    }

    // Inline event constructor
    public Event(MessageCreateEvent event, boolean inline, String inlinePrefix) {
        this(event);
        this.inline = inline;
        this.inlinePrefix = inlinePrefix;
    }

    public Integer getNumAllowedArguments() {
        return numAllowedArguments;
    }

    public void setNumAllowedArguments(Integer numAllowedArguments) {
        this.numAllowedArguments = numAllowedArguments;
    }

    public boolean isInline() {
        return inline;
    }

    public void setInline(boolean inline) {
        this.inline = inline;
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

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public boolean isFragment() {
        return fragment;
    }

    public void setFragment(boolean fragment) {
        this.fragment = fragment;
    }

    public Integer getInlineOrder() {
        return inlineOrder;
    }

    public void setInlineOrder(Integer inlineOrder) {
        this.inlineOrder = inlineOrder;
    }
}
