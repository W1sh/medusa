package com.w1sh.medusa.core.dispatchers;

import com.w1sh.medusa.core.data.Message;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class MessageDispatcher {

    private FluxProcessor<Message, Message> messageProcessor;
    private Integer bufferSize;

    public MessageDispatcher() {
        this.messageProcessor = EmitterProcessor.create(false);
        this.bufferSize = 2;
    }

    public void queue(MessageCreateEvent event, String content){
        Mono.just(event)
                .flatMap(ev -> ev.getMessage().getChannel())
                .map(messageChannel -> new Message(messageChannel, content))
                .subscribe(messageProcessor::onNext);
    }

    public void flush(){
        messageProcessor.publish()
                .autoConnect()
                .bufferTimeout(2, Duration.ofSeconds(2))
                .flatMap(Flux::fromIterable)
                .flatMap(message -> message.getChannel().createMessage(message.getContent()))
                .subscribe();
    }

    public Integer getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }
}
