package com.w1sh.medusa.core;

import com.w1sh.medusa.api.CommandEventFactory;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.core.listeners.impl.DisconnectListener;
import com.w1sh.medusa.core.listeners.impl.ReadyListener;
import com.w1sh.medusa.core.listeners.impl.VoiceStateUpdateListener;
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
    private final VoiceStateUpdateListener voiceStateUpdateListener;
    private final ReadyListener readyListener;
    private final DisconnectListener disconnectListener;
    private final CommandEventDispatcher commandEventDispatcher;

    public DiscordBot(DiscordClient client, VoiceStateUpdateListener voiceStateUpdateListener,
                      ReadyListener readyListener, DisconnectListener disconnectListener,
                      CommandEventDispatcher commandEventDispatcher) {
        this.client = client;
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

    private <T extends Event> void setupEventDispatcher(EventListener<T> eventListener){
        logger.info("Registering new listener to main dispatcher of type <{}>", eventListener.getClass().getSimpleName());
        client.getEventDispatcher()
                .on(eventListener.getEventType())
                .flatMap(eventListener::execute)
                .subscribe(null, throwable -> logger.error("Error when consuming events", throwable));
    }

    private void setupCommandEventDispatcher(){
        client.getEventDispatcher()
                .on(MessageCreateEvent.class)
                .filter(event -> event.getMember().isPresent())
                .filter(event -> event.getMember().map(user -> !user.isBot()).orElse(false)
                        && event.getMessage().getContent().orElse("").startsWith(CommandEventFactory.PREFIX))
                .subscribe(commandEventDispatcher::publish);
    }
}
