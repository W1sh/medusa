package com.w1sh.medusa.listeners;

import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.channel.TextChannelCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;

/**
 * An event listener that listens to {@link Event} which are dispatched by Discord on users actions.
 * See for example {@link TextChannelCreateEvent} or {@link GuildDeleteEvent}
 *
 * @param <T> The subclass of {@link Event} to listen to.
 */
public interface DiscordEventListener<T extends Event> extends EventListener<T, Event> { }
