package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.ChangePrefixEvent;
import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class ChangePrefixEventListener implements EventListener<ChangePrefixEvent> {

    private final ResponseDispatcher responseDispatcher;

    public ChangePrefixEventListener(ResponseDispatcher responseDispatcher) {
        this.responseDispatcher = responseDispatcher;
    }

    @Override
    public Class<ChangePrefixEvent> getEventType() {
        return ChangePrefixEvent.class;
    }

    @Override
    public Mono<Void> execute(ChangePrefixEvent event) {
        return Mono.just(event)
                .map(ev -> ev.getArguments().get(0))
                .doOnNext(EventFactory::setPrefix)
                .flatMap(prefix -> changePrefixSuccess(prefix, event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .flatMap(t -> changePrefix(event))
                .then();
    }

    public Mono<TextMessage> changePrefixSuccess(String prefix, ChangePrefixEvent event){
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format("Changed prefix to \"%s\"", prefix), false));
    }

    public Mono<Void> changePrefix(ChangePrefixEvent event){
        Activity activity = Activity.watching(String.format("Cringe 2 | %shelp", EventFactory.getPrefix()));
        return event.getClient().updatePresence(Presence.online(activity));
    }
}
