package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.ChangePrefixEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.utils.Messenger;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ChangePrefixEventListener implements EventListener<ChangePrefixEvent> {

    public ChangePrefixEventListener(CommandEventDispatcher eventDispatcher) {
        EventFactory.registerEvent(ChangePrefixEvent.KEYWORD, ChangePrefixEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<ChangePrefixEvent> getEventType() {
        return ChangePrefixEvent.class;
    }

    @Override
    public Mono<Void> execute(ChangePrefixEvent event) {
        return Mono.just(event)
                .filterWhen(this::validate)
                .map(ev -> ev.getMessage().getContent().orElse("").split(" ")[1])
                .doOnNext(prefix -> {
                    EventFactory.setPrefix(prefix);
                    Messenger.send(event, String.format("Changed prefix to \"%s\"", prefix)).subscribe();
                    event.getClient().updatePresence(Presence.online(Activity.watching(String.format("Cringe 2 | %shelp", EventFactory.getPrefix()))));
                })
                .then();
    }

    public Mono<Boolean> validate(ChangePrefixEvent event) {
        return Mono.justOrEmpty(event.getMessage().getContent())
                .map(message -> message.split(" "))
                .filter(strings -> strings.length == 2)
                .hasElement();
    }
}
