package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.ChangePrefixEvent;
import com.w1sh.medusa.core.EventFactory;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.listeners.CustomEventListener;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.discordjson.json.ActivityUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class ChangePrefixEventListener implements CustomEventListener<ChangePrefixEvent> {

    private final MessageService messageService;
    private final EventFactory eventFactory;

    @Override
    public Mono<Void> execute(ChangePrefixEvent event) {
        return Mono.just(event)
                .map(ev -> ev.getArguments().get(0))
                .doOnNext(eventFactory::setPrefix)
                .flatMap(prefix -> changePrefixSuccess(prefix, event))
                .doOnNext(messageService::queue)
                .doAfterTerminate(messageService::flush)
                .flatMap(t -> changePrefix(event))
                .then();
    }

    public Mono<TextMessage> changePrefixSuccess(String prefix, ChangePrefixEvent event){
        return event.getChannel().map(chan -> new TextMessage(chan, String.format("Changed prefix to \"%s\"", prefix), false));
    }

    public Mono<Void> changePrefix(ChangePrefixEvent event){
        ActivityUpdateRequest activityUpdateRequest = Activity.watching(String.format("Cringe 2 | %shelp", eventFactory.getPrefix()));
        return event.getClient().updatePresence(Presence.online(activityUpdateRequest));
    }
}
