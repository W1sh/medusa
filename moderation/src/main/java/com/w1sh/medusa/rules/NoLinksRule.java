package com.w1sh.medusa.rules;

import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.function.Function;
import java.util.regex.Pattern;

@Component
public class NoLinksRule implements Function<MessageCreateEvent, Mono<? extends Response>> {

    private static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
    private final Pattern p = Pattern.compile(URL_REGEX);

    @Override
    public Mono<? extends Response> apply(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getMessage().getContent())
                .flatMapIterable(message -> Arrays.asList(message.split(" ")))
                .filter(s -> p.matcher(s).find())
                .next()
                .doOnNext(ignored -> event.getMessage().delete().subscribe())
                .flatMap(ignored -> createNoLinksMessage(event));
    }

    private Mono<TextMessage> createNoLinksMessage(MessageCreateEvent event){
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s**, no links are allowed on this channel",
                        event.getMember().map(Member::getDisplayName).orElse("")), false));
    }
}
