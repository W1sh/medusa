package com.w1sh.medusa.core;

import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.listeners.impl.DisconnectListener;
import com.w1sh.medusa.listeners.impl.MessageCreateListener;
import com.w1sh.medusa.listeners.impl.ReadyListener;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.Event;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@AllArgsConstructor
@Component
public class DiscordBot {

    private final DiscordClient client;
    private final MessageCreateListener messageCreateListener;
    private final ReadyListener readyListener;
    private final DisconnectListener disconnectListener;

    @PostConstruct
    public void init(){
        setupEventDispatcher(messageCreateListener);
        setupEventDispatcher(disconnectListener);
        setupEventDispatcher(readyListener);

        client.login().block();
    }

    private <T extends Event> void setupEventDispatcher(EventListener<T> eventListener){
        client.getEventDispatcher()
                .on(eventListener.getEventType())
                .flatMap(event -> eventListener.execute(client, event))
                .subscribe(null, throwable -> log.error("Error when consuming events", throwable));
}

    public void setupEventDispatcher(){
        /*client.getEventDispatcher()
                .on(messageCreateListener.getEventType())
                .flatMap(messageCreateListener::execute)
                .subscribe(null, (throwable) -> log.error("Error when consuming MessageCreateEvent", throwable));

        client.getEventDispatcher()
                .on(readyListener.getEventType())
                .flatMap(readyListener::execute)
                .subscribe(null, (throwable) -> log.error("Error when consuming ReadyEvent", throwable));

        client.getEventDispatcher()
                .on(disconnectListener.getEventType())
                .flatMap(disconnectListener::execute)
                .subscribe(null, (throwable) -> log.error("Error when consuming DisconnectEvent", throwable));*/

        /*client.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(ready -> {
                    // bad implementation
                    // should only be added to database after trying to betting
                    databaseHandler.initializeDatabase(client);
                    databaseHandler.initializeAutomaticPointIncrementation();
                    //CommandHandler.setupCommands(client);
                    log.info("Logged in as " + ready.getSelf().getUsername());
                    log.info("Currently serving " + ready.getGuilds().size() + " servers");
                }, error -> {

                });*/

        /*client.getEventDispatcher().on(DisconnectEvent.class)
                //.doOnNext(disconnectEvent -> em.getTransaction().commit())
                .subscribe();*/

        /*client.getEventDispatcher().on(GuildCreateEvent.class)
                .map(event -> Tuples.of("guildId", event.getGuild().getId().asLong()))
                //.filter(tuple -> !userRepository.isPresent(tuple))
                //.doOnNext(tuple -> )
                .subscribe();*/

        /*client.getEventDispatcher().on(GuildDeleteEvent.class)
                .filter(guildDeleteEvent -> !guildDeleteEvent.isUnavailable())
                .map(GuildDeleteEvent::getGuild)
                //.doOnNext(unregister guild)
                .subscribe();*/

        /*client.getEventDispatcher().on(MemberJoinEvent.class)
                //.doOnEach(memberJoinEventSignal ->)
                .map(MemberJoinEvent::getMember)
                //.map(User::new)
                //.doOnNext(userRepository::persist)
                .onErrorResume(e -> Mono.empty())
                .subscribe();*/

        /*client.getEventDispatcher().on(MemberLeaveEvent.class)
                .map(event -> {
                    if(event.getMember().isPresent()){
                        return Tuples.of(event.getGuildId().asLong(),
                                event.getMember().map(discord4j.core.object.entity.User::getId).get().asLong());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .doOnNext(tuple-> userRepository.delete(
                        Tuples.of("discordId", tuple.getT2()),
                        Tuples.of("guildId", tuple.getT1())))
                .onErrorResume(e -> Mono.empty())
                .subscribe();*/
    }
}
