package com.w1sh.medusa.listeners;

import com.w1sh.medusa.core.EventFactory;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.events.ChangePrefixEvent;
import com.w1sh.medusa.services.MessageService;
import discord4j.core.object.entity.Message;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.discordjson.json.ActivityUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class ChangePrefixEventListener implements CustomEventListener<ChangePrefixEvent> {

    private static final Logger log = LoggerFactory.getLogger(ChangePrefixEventListener.class);
    private final MessageService messageService;
    private final EventFactory eventFactory;

    public ChangePrefixEventListener(MessageService messageService, EventFactory eventFactory) {
        this.messageService = messageService;
        this.eventFactory = eventFactory;
    }

    @Override
    public Mono<Void> execute(ChangePrefixEvent event) {
        final String prefix = event.getArguments().get(0);
        eventFactory.setPrefix(prefix);
        log.info("Changed prefix to \"{}\"", prefix);

        return changePrefixSuccess(prefix, event)
                .flatMap(t -> changePrefix(event))
                .then();
    }

    public Mono<Message> changePrefixSuccess(String prefix, ChangePrefixEvent event){
        return messageService.send(event.getChannel(), MessageEnum.CHANGE_PREFIX_SUCCESS, prefix);
    }

    public Mono<Void> changePrefix(ChangePrefixEvent event){
        ActivityUpdateRequest activityUpdateRequest = Activity.watching(String.format("Cringe 2 | %shelp", eventFactory.getPrefix()));
        return event.getClient().updatePresence(Presence.online(activityUpdateRequest));
    }
}
