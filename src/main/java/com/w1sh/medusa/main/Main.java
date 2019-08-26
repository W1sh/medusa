package com.w1sh.medusa.main;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan ("com.w1sh.medusa")
public class Main implements CommandLineRunner {

    private final DiscordBot bot;

    public Main(DiscordBot bot) {
        this.bot = bot;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        bot.setupEventDispatcher();
    }
}
