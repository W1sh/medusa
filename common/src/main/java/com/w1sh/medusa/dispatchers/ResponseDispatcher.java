package com.w1sh.medusa.dispatchers;

import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import discord4j.core.object.entity.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;

@Component
public class ResponseDispatcher {

    private final FluxProcessor<Response, Response> responseProcessor;
    private final Flux<Response> responseFlux;

    public ResponseDispatcher() {
        this.responseProcessor = UnicastProcessor.create();
        this.responseFlux = responseProcessor.publish().autoConnect();
    }

    public void queue(Response response) { Mono.just(response).subscribe(responseProcessor::onNext); }

    public void flush(){
        responseFlux.filter(response -> !response.isFragment())
                .flatMap(this::send)
                .subscribe()
                .dispose();
    }

    public void flush(Long bufferSize){
        responseFlux.take(bufferSize)
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
