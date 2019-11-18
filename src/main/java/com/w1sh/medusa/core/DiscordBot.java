package com.w1sh.medusa.core;

import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.listeners.impl.*;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.Event;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DiscordBot {

    private static final Logger logger = LoggerFactory.getLogger(DiscordBot.class);

    private final DiscordClient client;
    private final GenericEventListener genericEventListener;
    private final MessageCreateListener messageCreateListener;
    private final VoiceStateUpdateListener voiceStateUpdateListener;
    private final ReadyListener readyListener;
    private final DisconnectListener disconnectListener;

    public DiscordBot(DiscordClient client, GenericEventListener genericEventListener,
                      MessageCreateListener messageCreateListener, ReadyListener readyListener,
                      DisconnectListener disconnectListener) {
        this.client = client;
        this.genericEventListener = genericEventListener;
        this.messageCreateListener = messageCreateListener;
        this.readyListener = readyListener;
        this.disconnectListener = disconnectListener;
    }

    @PostConstruct
    public void init(){
        log.info("Setting up client...");
        setupEventDispatcher(messageCreateListener);
        setupEventDispatcher(disconnectListener);
        setupEventDispatcher(readyListener);
        setupEventDispatcher(voiceStateUpdateListener);

        client.login().block();
    }

    private <T extends Event, S> void setupEventDispatcher(EventListener<T, S> eventListener){
        log.info("Adding new listener to main dispatcher of type <{}>", eventListener.getClass().getSimpleName());
        client.getEventDispatcher()
                .on(eventListener.getEventType())
                .flatMap(event -> genericEventListener.execute(client, event))
                .ofType(eventListener.getEventType())
                .flatMap(event -> eventListener.execute(client, event))
                .subscribe(null, throwable -> log.error("Error when consuming events", throwable));
    }
}
