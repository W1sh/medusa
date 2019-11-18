package com.w1sh.medusa.api.dice;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DiceTest {

    private static Dice dice;

    @BeforeAll
    private static void setup() {
        dice = new Dice(new SecureRandom());
    }

    @Test
    void roll() {
        List<Integer> intStream = IntStream.generate(() -> dice.roll(0, 100))
                .limit(10000)
                .boxed()
                .collect(Collectors.toList());

        assertTrue(intStream.stream().allMatch(number -> number >= 0 && number <= 100));
    }
}
