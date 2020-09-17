package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.events.RollEvent;
import com.w1sh.medusa.services.MessageService;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Random;

import static com.w1sh.medusa.utils.Reactive.ifElse;
import static com.w1sh.medusa.utils.Reactive.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public final class RollEventListener implements CustomEventListener<RollEvent> {
    public static final String ROLL_ARGUMENT_DELIMITER = "-";

    private final Random random;
    private final MessageService messageService;

    @Override
    public Mono<Void> execute(RollEvent event) {
        return Mono.just(event)
                .filterWhen(this::validateRollArgument)
                .map(ev -> ev.getArguments().get(0).split(ROLL_ARGUMENT_DELIMITER))
                .map(limits -> roll(Integer.parseInt(limits[0]), Integer.parseInt(limits[1])))
                .flatMap(result -> sendResults(result, event));
    }

    private Integer roll(Integer min, Integer max){
        return random.nextInt(min + max + 1) + min;
    }

    private Mono<Void> sendResults(Integer result, RollEvent event){
        final Mono<Message> rollStartMessage = messageService.send(event.getChannel(), MessageEnum.ROLL_START);
        final Mono<Message> rollResultMessage = messageService.send(event.getChannel(), MessageEnum.ROLL_RESULT,
                event.getNickname(), String.valueOf(result));

        return Mono.when(rollStartMessage, rollResultMessage);
    }

    private Mono<Boolean> validateRollArgument(Event event){
        return Mono.justOrEmpty(event)
                .map(limits -> limits.getArguments().get(0).split(ROLL_ARGUMENT_DELIMITER))
                .filter(limits -> limits.length == 2)
                .map(limits -> new int[]{Integer.parseInt(limits[0]), Integer.parseInt(limits[1])})
                .filter(limits -> limits[1] > limits[0])
                .onErrorResume(throwable -> Mono.fromRunnable(() -> log.error("Failed to parse arguments", throwable)))
                .hasElement()
                .transform(ifElse(b -> Mono.empty(), b -> messageService.send(event.getChannel(), MessageEnum.ROLL_ERROR)))
                .transform(isEmpty());
    }
}
