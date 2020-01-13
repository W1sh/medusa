package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.StatusEvent;
import com.w1sh.medusa.core.data.Embed;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.Event;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.metrics.Trackers;
import discord4j.core.object.entity.MessageChannel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.awt.*;

@Component
public class StatusEventListener implements EventListener<StatusEvent> {

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
        return new Embed(messageChannel, embedCreateSpec -> {
            embedCreateSpec.setColor(Color.GREEN);
            embedCreateSpec.setTitle("Medusa - 1.0-SNAPSHOT");
            embedCreateSpec.addField("Uptime", Trackers.getUptime(), true);
            embedCreateSpec.addField("Memory Usage", String.format("%d / %d MB",
                    numberAsMegabytes(Runtime.getRuntime().freeMemory()),
                    numberAsMegabytes(Runtime.getRuntime().totalMemory())), true);
        });
    }

    private Long numberAsMegabytes(Long number){
        return number / (1024 * 1024);
    }
}
