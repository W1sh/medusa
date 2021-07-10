package com.w1sh.medusa.commands;

import com.w1sh.medusa.core.Instance;
import com.w1sh.medusa.services.EventService;
import com.w1sh.medusa.services.MessageService;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Color;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class StatusCommandService implements ApplicationCommandService {

    private static final String COMMAND_NAME = "status";

    private final EventService eventService;

    @Value("${medusa.version}")
    private String version;

    public StatusCommandService(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public ApplicationCommandRequest buildApplicationCommandRequest() {
        return ApplicationCommandRequest.builder()
                .name(COMMAND_NAME)
                .description("Check the status of the bot")
                .build();
    }

    @Override
    public Mono<Void> reply(SlashCommandEvent event) {
        return Mono.zip(event.getClient().getGuilds().count(), event.getClient().getUsers().count(), eventService.countAll())
                .flatMap(tuple -> event.reply(spec -> {
                    spec.addEmbed(embedCreateSpec -> {
                        embedCreateSpec.setColor(Color.GREEN);
                        embedCreateSpec.setTitle(String.format("Medusa - Shard %d/%d",
                                event.getShardInfo().getIndex() + 1,
                                event.getShardInfo().getCount()));
                        embedCreateSpec.addField("Uptime", Instance.getUptime(), true);
                        embedCreateSpec.addField("Memory Usage", String.format("%d MB / %d MB",
                                numberAsMegabytes(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()),
                                numberAsMegabytes(Runtime.getRuntime().totalMemory())), true);
                        embedCreateSpec.addField(MessageService.ZERO_WIDTH_SPACE, MessageService.ZERO_WIDTH_SPACE, true);
                        embedCreateSpec.addField("Guilds", String.format("%d (%d Avg Users/Guild)",
                                tuple.getT1(), tuple.getT2() / tuple.getT1()), true);
                        embedCreateSpec.addField("Users", tuple.getT2().toString(), true);
                        embedCreateSpec.addField("Total events", tuple.getT3().toString(), true);
                        embedCreateSpec.setFooter(String.format("Version: %s", version), null);
                    });
                }));
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    private Long numberAsMegabytes(Long number){
        return number / (1024 * 1024);
    }
}
