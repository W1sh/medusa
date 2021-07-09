package com.w1sh.medusa.commands;

import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import reactor.core.publisher.Mono;

public interface ApplicationCommandService {

    ApplicationCommandRequest buildApplicationCommandRequest();

    Mono<Void> reply(SlashCommandEvent event);

    String getName();
}
