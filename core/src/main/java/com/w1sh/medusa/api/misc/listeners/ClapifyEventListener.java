package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.ClapifyEvent;
import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public final class ClapifyEventListener implements EventListener<ClapifyEvent> {

    private final ResponseDispatcher responseDispatcher;

    public ClapifyEventListener(CommandEventDispatcher eventDispatcher, ResponseDispatcher responseDispatcher) {
        this.responseDispatcher = responseDispatcher;
        EventFactory.registerEvent(ClapifyEvent.KEYWORD, ClapifyEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<ClapifyEvent> getEventType() {
        return ClapifyEvent.class;
    }

    @Override
    public Mono<Void> execute(ClapifyEvent event) {
        return Mono.just(event)
                .map(ev -> ev.getArguments().values())
                .flatMap(words -> clapify(event, words))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> clapify(ClapifyEvent event, Collection<String> words){
        String content =  words.stream()
                .filter(Predicate.not(String::isBlank))
                .map(String::toUpperCase)
                .collect(Collectors.joining(" :clap: ", ":clap: ", " :clap:"));

        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, content, false));
    }

}
