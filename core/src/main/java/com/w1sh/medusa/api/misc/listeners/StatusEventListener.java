package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.StatusEvent;
import com.w1sh.medusa.core.data.Embed;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.Event;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.metrics.Trackers;
import com.w1sh.medusa.utils.ResponseUtils;
import discord4j.core.object.entity.MessageChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.awt.*;

@Component
public final class StatusEventListener implements EventListener<StatusEvent> {

    @Value("${medusa.version}")
    private String version;

    private final ResponseDispatcher responseDispatcher;

    public StatusEventListener(CommandEventDispatcher eventDispatcher, ResponseDispatcher responseDispatcher) {
        this.responseDispatcher = responseDispatcher;
        EventFactory.registerEvent(StatusEvent.KEYWORD, StatusEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<StatusEvent> getEventType() {
        return StatusEvent.class;
    }

    @Override
    public Mono<Void> execute(StatusEvent event) {
        return event.getMessage().getChannel()
                .map(messageChannel -> createStatusEmbed(messageChannel, event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Embed createStatusEmbed(MessageChannel messageChannel, Event event){
        Long guilds = Trackers.getGuilds(event.getClient());
        Long users = Trackers.getUsers(event.getClient());

        return new Embed(messageChannel, embedCreateSpec -> {
            embedCreateSpec.setColor(Color.GREEN);
            embedCreateSpec.setTitle(String.format("Medusa - Shard %d/%d",
                    event.getClient().getServiceMediator().getClientConfig().getShardIndex() + 1,
                    event.getClient().getServiceMediator().getClientConfig().getShardCount()));
            embedCreateSpec.addField("Uptime", Trackers.getUptime(), true);
            embedCreateSpec.addField("Memory Usage", String.format("%d MB / %d MB",
                    numberAsMegabytes(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()),
                    numberAsMegabytes(Runtime.getRuntime().totalMemory())), true);
            embedCreateSpec.addField(ResponseUtils.ZERO_WIDTH_SPACE, ResponseUtils.ZERO_WIDTH_SPACE, true);
            embedCreateSpec.addField("Guilds", String.format("%d (%d Avg Users/Guild)",
                    guilds, users/guilds), true);
            embedCreateSpec.addField("Users", users.toString(), true);
            embedCreateSpec.addField("Total events", Trackers.getTotalEventCount().toString(), true);
            embedCreateSpec.setFooter(String.format("Version: %s", version), null);
        });
    }

    private Long numberAsMegabytes(Long number){
        return number / (1024 * 1024);
    }
}
