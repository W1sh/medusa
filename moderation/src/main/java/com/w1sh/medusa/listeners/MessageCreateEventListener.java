package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.RuleEnum;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.services.ChannelRuleService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public final class MessageCreateEventListener implements EventListener<MessageCreateEvent> {

    private final Map<RuleEnum, Function<MessageCreateEvent, Mono<? extends Response>>> rulesImplementation = new EnumMap<>(RuleEnum.class);
    private final ApplicationContext context;
    private final ResponseDispatcher responseDispatcher;
    private final ChannelRuleService channelRuleService;

    @PostConstruct
    private void init(){
        rulesImplementation.put(RuleEnum.NO_LINKS, context.getBean("noLinksRule", Function.class));
        rulesImplementation.put(RuleEnum.NO_GAMBLING, context.getBean("noGamblingRule", Function.class));
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return event.getMessage().getChannel()
                .flatMap(messageChannel -> channelRuleService.findAllByChannel(messageChannel.getId().asString()))
                .flatMapIterable(Function.identity())
                .flatMap(channelRule -> rulesImplementation.get(channelRule.getRule().getRuleValue()).apply(event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }
}
