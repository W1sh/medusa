package com.w1sh.medusa.commands;

import com.w1sh.medusa.core.Instance;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class UptimeCommandService implements ApplicationCommandService {

    private static final String COMMAND_NAME = "uptime";
    private static final String CONTENT = "Medusa has been online for %s";

    @Override
    public ApplicationCommandRequest buildApplicationCommandRequest() {
        return ApplicationCommandRequest.builder()
                .name(COMMAND_NAME)
                .description("Check how long the bot has been running")
                .build();
    }

    @Override
    public Mono<Void> reply(SlashCommandEvent event) {
        return event.reply(String.format(CONTENT, Instance.getUptime()));
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }
}
