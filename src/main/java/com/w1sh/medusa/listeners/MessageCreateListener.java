package com.w1sh.medusa.listeners;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.TextChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Slf4j
@Component
public class MessageCreateListener implements EventListener<MessageCreateEvent> {

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return event.getGuild()
                .flatMapMany(Guild::getChannels)
                .ofType(TextChannel.class)
                .filter(textChannel -> textChannel.getName().toLowerCase().contains("general"))
                .collectList()
                .doOnNext(textChannels -> Flux.fromIterable(textChannels)
                        .flatMap(textChannel -> textChannel.createMessage("Welcome"))
                        .elapsed()
                        .map(Tuple2::getT1)
                        .doOnNext(elapsed -> log.info("Answered request in {} milliseconds", elapsed))
                        .subscribe())
                .then();
    }
}
