package com.w1sh.medusa.utils;

import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.services.MessageService;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class CardUtils {

    public static final int MAX_ALLOWED_LENGTH = 128;

    private final MessageService messageService;

    public Mono<Message> createErrorEmbed(InlineEvent event){
        final Consumer<EmbedCreateSpec> specConsumer = embedCreateSpec -> {
            embedCreateSpec.setColor(Color.GREEN);
            embedCreateSpec.setDescription(String.format(":x: Sorry **%s**, I failed to find the card you requested, be more specific or try another card.",
                    event.getNickname()));
        };
        final Response response = Response.with(specConsumer, event.getChannel(), event.getChannelId(),
                event.isFragment(), event.getInlineOrder());
        return messageService.sendOrQueue(event.getChannel(), response);
    }

    public Mono<String> validateArgument(InlineEvent event){
        if(!event.hasArgument()) return Mono.empty();
        return event.getInlineArgument().length() < MAX_ALLOWED_LENGTH ? Mono.just(event.getInlineArgument()) : Mono.empty();
    }
}
