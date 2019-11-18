package com.w1sh.medusa.api.dice;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class Dice {

    private final Random random;

    public Dice(Random random){
        this.random = random;
    }

    public Integer roll(Integer min, Integer max) {
        return random.nextInt((min + max + 1)) + min;
    }
}
