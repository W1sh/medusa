package com.w1sh.medusa.api.dice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class Dice {

    private static final Logger logger = LoggerFactory.getLogger(Dice.class);
    private final Random random;

    public Dice(Random random){
        this.random = random;
    }

    public Integer roll(Integer min, Integer max) {
        return random.nextInt((min + max + 1)) + min;
    }

    public Integer roll(String min, String max) {
        try {
            return random.nextInt((Integer.parseInt(min) + Integer.parseInt(max) + 1)) + Integer.parseInt(min);
        } catch (NumberFormatException e){
            logger.info("Failed to parse string to number", e);
            return 0;
        }
    }
}
