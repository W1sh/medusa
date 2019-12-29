package com.w1sh.medusa.core.dispatchers;

import com.w1sh.medusa.core.data.Embed;
import com.w1sh.medusa.core.data.Response;
import com.w1sh.medusa.core.data.TextMessage;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;

@Component
public class ResponseDispatcher {

    private FluxProcessor<Response, Response> responseProcessor;

    public ResponseDispatcher() {
        this.responseProcessor = EmitterProcessor.create(false);
    }

    public void queue(MessageCreateEvent event, String content){
        Mono.just(event)
                .flatMap(ev -> ev.getMessage().getChannel())
                .map(messageChannel -> new TextMessage(messageChannel, content, false))
                .subscribe(responseProcessor::onNext);
    }

    public void queue(TextMessage textMessage){
        Mono.just(textMessage).subscribe(responseProcessor::onNext);
    }

    public void queue(Embed embed){
        Mono.just(embed).subscribe(responseProcessor::onNext);
    }

    public void flush(){
        responseProcessor.publish()
                .autoConnect()
                .filter(response -> !response.isFragment())
                .flatMap(this::send)
                .subscribe();
    }

    public void flush(Long bufferSize){
        responseProcessor.publish()
                .autoConnect()
                .take(bufferSize)
                .sort()
                .flatMap(this::send)
                .subscribe();
    }

    private Mono<Message> send(Response response){
        if(response instanceof TextMessage) {
            return response.getChannel().createMessage(((TextMessage) response).getContent());
        }else if(response instanceof Embed){
            return response.getChannel().createEmbed(((Embed) response).getEmbedCreateSpec());
        }else return Mono.empty();
    }
}
