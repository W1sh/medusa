package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;

import java.time.Duration;
import java.util.Locale;
import java.util.function.Consumer;

@Slf4j
@Component
public class MessageService {

    private final FluxProcessor<Response, Response> responseProcessor;
    private final Flux<Response> responseFlux;
    private final Cache<String, Message> messageCache;
    private final MessageSource messageSource;

    public MessageService(ResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
        this.responseProcessor = UnicastProcessor.create();
        this.responseFlux = responseProcessor.publish().autoConnect();
        this.messageCache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(6))
                .expireAfterWrite(Duration.ofDays(1))
                .build();
    }

    public void queue(Response response) { Mono.just(response).subscribe(responseProcessor::onNext); }

    public Mono<Message> send(Mono<MessageChannel> channelMono, Consumer<EmbedCreateSpec> embedCreateSpec) {
        return channelMono.flatMap(channel -> channel.createEmbed(embedCreateSpec))
                .doOnNext(m -> messageCache.put(m.getId().asString(), m));
    }

    public Mono<Message> send(Mono<MessageChannel> channelMono, String message) {
        return channelMono.flatMap(channel -> channel.createMessage(message))
                .doOnNext(m -> messageCache.put(m.getId().asString(), m));
    }

    public Mono<Message> send(Mono<MessageChannel> channelMono, MessageEnum messageEnum) {
        return channelMono.flatMap(channel -> channel.createMessage(getMessage(messageEnum.getMessageKey(), null)))
                .doOnNext(message -> messageCache.put(message.getId().asString(), message));
    }

    public Mono<Message> send(Mono<MessageChannel> channelMono, MessageEnum messageEnum, String... args) {
        return channelMono.flatMap(channel -> channel.createMessage(getMessage(messageEnum.getMessageKey(), args)))
                .doOnNext(message -> messageCache.put(message.getId().asString(), message));
    }

    public void flush(){
        responseFlux.filter(response -> !response.isFragment())
                .flatMap(this::send)
                .doOnNext(message -> messageCache.put(message.getId().asString(), message))
                .subscribe()
                .dispose();
    }

    public void flush(Long bufferSize){
        responseFlux.take(bufferSize)
                .sort()
                .flatMap(this::send)
                .doOnNext(message -> messageCache.put(message.getId().asString(), message))
                .subscribe();
    }

    private String getMessage(String messageKey, String[] args) {
        return messageSource.getMessage(messageKey, args, Locale.ENGLISH);
    }

    private Mono<Message> send(Response response){
        if(response instanceof TextMessage) {
            return response.getChannel().createMessage(((TextMessage) response).getContent());
        }else if(response instanceof Embed){
            return response.getChannel().createEmbed(((Embed) response).getEmbedCreateSpec());
        }else return Mono.empty();
    }
}
