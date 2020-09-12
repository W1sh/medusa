package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.StatusEvent;
import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.CustomEventListener;
import com.w1sh.medusa.metrics.Trackers;
import com.w1sh.medusa.utils.ResponseUtils;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class StatusEventListener implements CustomEventListener<StatusEvent> {

    @Value("${medusa.version}")
    private String version;

    private final ResponseDispatcher responseDispatcher;

    @Override
    public Mono<Void> execute(StatusEvent event) {
        return event.getChannel()
                .flatMap(messageChannel -> createStatusEmbed(messageChannel, event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<Embed> createStatusEmbed(MessageChannel messageChannel, Event event){
        return event.getClient().getGuilds().count()
                .zipWith(event.getClient().getUsers().count())
                .map(tuple -> new Embed(messageChannel, embedCreateSpec -> {
                    embedCreateSpec.setColor(Color.GREEN);
                    embedCreateSpec.setTitle(String.format("Medusa - Shard %d/%d",
                            event.getShardInfo().getIndex() + 1,
                            event.getShardInfo().getCount()));
                    embedCreateSpec.addField("Uptime", Trackers.getUptime(), true);
                    embedCreateSpec.addField("Memory Usage", String.format("%d MB / %d MB",
                            numberAsMegabytes(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()),
                            numberAsMegabytes(Runtime.getRuntime().totalMemory())), true);
                    embedCreateSpec.addField(ResponseUtils.ZERO_WIDTH_SPACE, ResponseUtils.ZERO_WIDTH_SPACE, true);
                    embedCreateSpec.addField("Guilds", String.format("%d (%d Avg Users/Guild)",
                            tuple.getT1(), tuple.getT2()/tuple.getT1()), true);
                    embedCreateSpec.addField("Users", tuple.getT2().toString(), true);
                    embedCreateSpec.addField("Total events", Trackers.getTotalEventCount().toString(), true);
                    embedCreateSpec.setFooter(String.format("Version: %s", version), null);
                }));
    }

    private Long numberAsMegabytes(Long number){
        return number / (1024 * 1024);
    }
}
