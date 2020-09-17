package com.w1sh.medusa.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.w1sh.medusa.resources.Card;
import com.w1sh.medusa.resources.ListResponse;
import com.w1sh.medusa.resources.ScryfallException;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;
import reactor.util.retry.Retry;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
public final class CardClient {

    private static final String SEARCH_URL = "https://api.scryfall.com/cards/search?q=%s";
    private static final String NAMED_SEARCH_URL = "https://api.scryfall.com/cards/named?fuzzy=%s";

    private final ObjectMapper objectMapper;
    private final HttpClient.ResponseReceiver<?> responseReceiver;

    public CardClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.responseReceiver = HttpClient.create().get();
    }

    public Mono<ListResponse<Card>> getCardsByName(String name) {
        return responseReceiver.uri(String.format(SEARCH_URL, URLEncoder.encode(name, StandardCharsets.UTF_8)))
                .responseSingle(((response, byteBuf) -> handleHttpResponse(response, byteBuf, new TypeReference<ListResponse<Card>>() {})))
                .retryWhen(Retry.maxInARow(3))
                .timeout(Duration.ofSeconds(10))
                .doOnNext(s -> log.info("Queried Scryfall API for all cards with name like \"{}\"", name));
    }

    public Mono<ListResponse<Card>> getUniquePrints(String uri) {
        return responseReceiver.uri(uri)
                .responseSingle(((response, byteBuf) -> handleHttpResponse(response, byteBuf, new TypeReference<ListResponse<Card>>() {})))
                .retryWhen(Retry.maxInARow(3))
                .timeout(Duration.ofSeconds(10))
                .doOnNext(s -> log.info("Queried Scryfall API for unique prints"));
    }

    public Mono<Card> getCardByName(String name) {
        return responseReceiver.uri(String.format(NAMED_SEARCH_URL, URLEncoder.encode(name, StandardCharsets.UTF_8)))
                .responseSingle(((response, byteBuf) -> handleHttpResponse(response, byteBuf, new TypeReference<Card>() {})))
                .retryWhen(Retry.maxInARow(3))
                .timeout(Duration.ofSeconds(10))
                .doOnNext(s -> log.info("Queried Scryfall API for card with name similar to \"{}\"", name));
    }

    private <T extends HttpClientResponse, U extends ByteBufMono, R> Mono<R> handleHttpResponse(T httpClientResponse,
                                                                                                U byteBufMono, TypeReference<R> typeReference){
        log.info("Received response from Scryfall with status {}", httpClientResponse.status().toString());
        if(httpClientResponse.status().equals(HttpResponseStatus.OK)){
            return byteBufMono.asString()
                    .map(json -> parse(json, typeReference))
                    .map(Optional::get);
        } else {
            final String exceptionMessage = String.format("Failed to retrieve cards from Scryfall API with reason \"%s\"",
                    httpClientResponse.status().toString());
            return Mono.error(new ScryfallException(exceptionMessage));
        }
    }

    private <T> Optional<T> parse(String json, TypeReference<T> typeReference){
        try {
            return Optional.of(objectMapper.readValue(json, typeReference));
        } catch (JsonProcessingException e) {
            log.error("Error while parsing JSON received from Scryfall", e);
        }
        return Optional.empty();
    }
}