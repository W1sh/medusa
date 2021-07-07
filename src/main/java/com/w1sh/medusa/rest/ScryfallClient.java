package com.w1sh.medusa.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.w1sh.medusa.rest.resources.Card;
import com.w1sh.medusa.rest.resources.ListResponse;
import com.w1sh.medusa.rest.resources.ScryfallException;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;
import reactor.util.retry.Retry;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public final class ScryfallClient {

    private static final String SEARCH_URL = "https://api.scryfall.com/cards/search?q=%s";
    private static final String NAMED_SEARCH_URL = "https://api.scryfall.com/cards/named?fuzzy=%s";
    private static final Logger log = LoggerFactory.getLogger(ScryfallClient.class);

    private final ObjectMapper objectMapper;
    private final HttpClient.ResponseReceiver<?> responseReceiver;

    public ScryfallClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.responseReceiver = HttpClient.create().get();
    }

    public Mono<ListResponse<Card>> getCardsByName(String name) {
        log.info("Querying Scryfall API for all cards with name like \"{}\"", name);
        final String uri = String.format(SEARCH_URL, URLEncoder.encode(name, StandardCharsets.UTF_8));
        return get(uri, new TypeReference<>() {});
    }

    public Mono<ListResponse<Card>> getUniquePrints(String uri) {
        log.info("Querying Scryfall API for unique prints");
        return get(uri, new TypeReference<>() {});
    }

    public Mono<Card> getCardByName(String name) {
        log.info("Querying Scryfall API for card with name similar to \"{}\"", name);
        final String uri = String.format(NAMED_SEARCH_URL, URLEncoder.encode(name, StandardCharsets.UTF_8));
        return get(uri, new TypeReference<>() {});
    }

    public <T> Mono<T> get(String uri, TypeReference<T> typeReference) {
        return responseReceiver.uri(uri)
                .responseSingle(((response, byteBuf) -> handleHttpResponse(response, byteBuf, typeReference)))
                .retryWhen(Retry.maxInARow(2))
                .timeout(Duration.ofSeconds(10));
    }

    private <T> Mono<T> handleHttpResponse(HttpClientResponse httpClientResponse, ByteBufMono byteBufMono, TypeReference<T> typeReference){
        log.info("Received response from Scryfall with status {}", httpClientResponse.status().toString());
        final String exceptionMessage = String.format("Failed to retrieve cards from Scryfall API with reason \"%s\"",
                httpClientResponse.status().toString());
        if(httpClientResponse.status().equals(HttpResponseStatus.OK)){
            return byteBufMono.asString()
                    .flatMap(json -> Mono.justOrEmpty(parse(json, typeReference)))
                    .switchIfEmpty(Mono.error(() -> new ScryfallException(exceptionMessage)));
        } else {
            return Mono.error(() -> new ScryfallException(exceptionMessage));
        }
    }

    private <T> T parse(String json, TypeReference<T> typeReference){
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("Error while parsing JSON received from Scryfall", e);
        }
        return null;
    }
}
