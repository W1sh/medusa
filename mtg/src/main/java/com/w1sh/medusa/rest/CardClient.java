package com.w1sh.medusa.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.w1sh.medusa.resources.Card;
import com.w1sh.medusa.resources.ListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CardClient {

    private static final Logger logger = LoggerFactory.getLogger(CardClient.class);

    private final ObjectMapper objectMapper;

    public CardClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Mono<ListResponse<Card>> getCardsByName(String name){
        return HttpClient.create()
                .get()
                .uri("https://api.scryfall.com/cards/search?q=" + URLEncoder.encode(name, StandardCharsets.UTF_8))
                .responseContent()
                .aggregate()
                .asString()
                .map(this::parseMultiple);
    }

    public Mono<Card> getCardByName(String name){
        return HttpClient.create()
                .get()
                .uri("https://api.scryfall.com/cards/named?fuzzy=" + URLEncoder.encode(name, StandardCharsets.UTF_8))
                .responseContent()
                .aggregate()
                .asString()
                .map(this::parse);
    }

    private Card parse(String json){
        try {
            return objectMapper.readValue(json, Card.class);
        } catch (JsonProcessingException e) {
            logger.error("Error while parsing JSON received from Scryfall", e);
        }
        return null;
    }

    private ListResponse<Card> parseMultiple(String json){
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            logger.error("Error while parsing JSON received from Scryfall", e);
        }
        return new ListResponse<>();
    }
}
