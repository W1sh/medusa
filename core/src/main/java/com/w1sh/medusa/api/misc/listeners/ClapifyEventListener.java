package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.ClapifyEvent;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.CustomEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public final class ClapifyEventListener implements CustomEventListener<ClapifyEvent> {

    private final ResponseDispatcher responseDispatcher;

    @Override
    public Mono<Void> execute(ClapifyEvent event) {
        return clapify(event)
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> clapify(ClapifyEvent event){
        String content =  event.getArguments().stream()
                .filter(Predicate.not(String::isBlank))
                .map(String::toUpperCase)
                .collect(Collectors.joining(" :clap: ", ":clap: ", " :clap:"));

        return event.getChannel().map(chan -> new TextMessage(chan, content, false));
    }

}
