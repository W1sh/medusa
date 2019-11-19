package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.CommandEvent;
import com.w1sh.medusa.api.misc.events.UnsupportedEvent;
import com.w1sh.medusa.core.data.Emoji;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.utils.Messager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UnsupportedEventListener implements EventListener<UnsupportedEvent, Void> {

    public UnsupportedEventListener(CommandEventDispatcher eventDispatcher) {
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<UnsupportedEvent> getEventType() {
        return UnsupportedEvent.class;
    }

    @Override
    public Mono<Void> execute(UnsupportedEvent event) {
        return event.getMessage().getChannel()
                .doOnNext(channel -> Messager.send(event.getClient(), channel,
                        String.format("%s Unsupported command! Use `%shelp` to find out what commands are supported",
                                Emoji.CROSS_MARK.getShortcode(), CommandEvent.PREFIX))
                        .subscribe())
                .then();
    }
}
