package com.w1sh.medusa.core;

import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.events.CommandEvent;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.core.listeners.impl.*;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DiscordBot {

    private static final Logger logger = LoggerFactory.getLogger(DiscordBot.class);

    private final DiscordClient client;
    private final GenericEventListener genericEventListener;
    private final VoiceStateUpdateListener voiceStateUpdateListener;
    private final ReadyListener readyListener;
    private final DisconnectListener disconnectListener;
    private final CommandEventDispatcher commandEventDispatcher;

    public DiscordBot(DiscordClient client, GenericEventListener genericEventListener, VoiceStateUpdateListener voiceStateUpdateListener,
                      ReadyListener readyListener, DisconnectListener disconnectListener, CommandEventDispatcher commandEventDispatcher) {
        this.client = client;
        this.genericEventListener = genericEventListener;
        this.voiceStateUpdateListener = voiceStateUpdateListener;
        this.readyListener = readyListener;
        this.disconnectListener = disconnectListener;
        this.commandEventDispatcher = commandEventDispatcher;
    }

    @PostConstruct
    public void init(){
        logger.info("Setting up client...");
        setupEventDispatcher(disconnectListener);
        setupEventDispatcher(readyListener);
        setupEventDispatcher(voiceStateUpdateListener);

        setupCommandEventDispatcher();

        client.login().block();
    }

    private <T extends Event, S> void setupEventDispatcher(EventListener<T, S> eventListener){
        logger.info("Adding new listener to main dispatcher of type <{}>", eventListener.getClass().getSimpleName());
        client.getEventDispatcher()
                .on(eventListener.getEventType())
                .flatMap(event -> genericEventListener.execute(client, event))
                .ofType(eventListener.getEventType())
                .flatMap(event -> eventListener.execute(client, event))
                .subscribe(null, throwable -> logger.error("Error when consuming events", throwable));
    }

    private void setupCommandEventDispatcher(){
        client.getEventDispatcher()
                .on(MessageCreateEvent.class)
                .filter(event -> event.getMember().isPresent())
                .filter(event -> event.getMember().map(user -> !user.isBot()).orElse(false))
                .filter(event -> event.getMessage().getContent().orElse("").startsWith(CommandEvent.PREFIX))
                .doOnNext(commandEventDispatcher::publish)
                .subscribe(null, throwable -> logger.error("Error when publishing to commandEventDispatcher", throwable));
    }
}
