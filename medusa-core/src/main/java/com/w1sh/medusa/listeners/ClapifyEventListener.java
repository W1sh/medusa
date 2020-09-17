package com.w1sh.medusa.listeners;

import com.w1sh.medusa.events.ClapifyEvent;
import com.w1sh.medusa.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public final class ClapifyEventListener implements CustomEventListener<ClapifyEvent> {

    private final MessageService messageService;

    @Override
    public Mono<Void> execute(ClapifyEvent event) {
        final String content =  event.getArguments().stream()
                .filter(Predicate.not(String::isBlank))
                .map(String::toUpperCase)
                .collect(Collectors.joining(" :clap: ", ":clap: ", " :clap:"));

        return messageService.send(event.getChannel(), content)
                .then();
    }
}
