package com.w1sh.medusa.rest;

import org.springframework.stereotype.Component;
import reactor.netty.http.client.HttpClient;

@Component
public class CardClient {

    public String getCardByName(String name){
        /*return HttpClient.create()
                .get()
                .uri("https://api.scryfall.com/cards/search?q=asd")
                .responseContent()
                .aggregate()
                .asString()
                .block();*/
        return "Oops!";
    }
}
