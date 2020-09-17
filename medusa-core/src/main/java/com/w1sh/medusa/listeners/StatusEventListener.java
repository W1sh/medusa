package com.w1sh.medusa.listeners;

import com.w1sh.medusa.core.Instance;
import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.events.StatusEvent;
import com.w1sh.medusa.services.EventService;
import com.w1sh.medusa.services.MessageService;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public final class StatusEventListener implements CustomEventListener<StatusEvent> {

    @Value("${medusa.version}")
    private String version;

    private final MessageService messageService;
    private final EventService eventService;

    @Override
    public Mono<Void> execute(StatusEvent event) {
        return createStatusEmbedSpec(event)
                .flatMap(embedCreateSpec -> messageService.send(event.getChannel(), embedCreateSpec))
                .then();
    }

    private Mono<Consumer<EmbedCreateSpec>> createStatusEmbedSpec(Event event) {
        return Mono.zip(event.getClient().getGuilds().count(), event.getClient().getUsers().count(),eventService.countAll())
                .map(tuple -> embedCreateSpec -> {
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
                            tuple.getT1(), tuple.getT2()/tuple.getT1()), true);
                    embedCreateSpec.addField("Users", tuple.getT2().toString(), true);
                    embedCreateSpec.addField("Total events", tuple.getT3().toString(), true);
                    embedCreateSpec.setFooter(String.format("Version: %s", version), null);
                });
    }

    private Long numberAsMegabytes(Long number){
        return number / (1024 * 1024);
    }
}
