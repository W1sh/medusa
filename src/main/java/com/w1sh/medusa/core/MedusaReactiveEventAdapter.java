package com.w1sh.medusa.core;

import com.w1sh.medusa.commands.SlashCommandServiceFactory;
import com.w1sh.medusa.data.SlashCommand;
import com.w1sh.medusa.services.SlashCommandService;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class MedusaReactiveEventAdapter extends ReactiveEventAdapter {

    private static final Logger log = LoggerFactory.getLogger(MedusaReactiveEventAdapter.class);

    private final SlashCommandServiceFactory slashCommandServiceFactory;
    private final SlashCommandService slashCommandService;

    public MedusaReactiveEventAdapter(SlashCommandServiceFactory slashCommandServiceFactory, SlashCommandService slashCommandService) {
        this.slashCommandServiceFactory = slashCommandServiceFactory;
        this.slashCommandService = slashCommandService;
    }

    @NonNull
    @Override
    public Publisher<?> onReady(ReadyEvent event) {
        return Mono.justOrEmpty(event.getGuilds().size())
                .flatMap(size -> event.getClient().getEventDispatcher()
                        .on(GuildCreateEvent.class)
                        .take(size)
                        .last())
                .doOnNext(ev -> log.info("All guilds have been received, the client is fully connected"))
                .flatMap(ev -> ev.getClient().getGuilds().count())
                .doOnNext(guilds -> log.info("Currently serving {} guilds", guilds))
                .then();
    }

    @NonNull
    @Override
    public Publisher<?> onSlashCommand(@NonNull SlashCommandEvent event) {
        slashCommandService.save(new SlashCommand(event));
        return slashCommandServiceFactory.getService(event.getCommandName()).reply(event);
    }

    @NonNull
    @Override
    public Publisher<?> onDisconnect(DisconnectEvent event) {
        Throwable cause = event.getCause().orElse(null);
        log.error("Disconnected from gateway with status {}", event.getStatus(), cause);
        return super.onDisconnect(event);
    }
}
