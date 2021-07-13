package com.w1sh.medusa;

import com.w1sh.medusa.core.Instance;
import com.w1sh.medusa.services.SlashCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration;

import javax.annotation.PreDestroy;

@SpringBootApplication(exclude = MongoReactiveRepositoriesAutoConfiguration.class)
public class Main implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private final Instance instance;
    private final SlashCommandService slashCommandService;

    public Main(Instance instance, SlashCommandService slashCommandService) {
        this.instance = instance;
        this.slashCommandService = slashCommandService;
    }

    public static void main(String[] args) {
        Thread.currentThread().setName("medusa-main");
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        instance.initialize();
        for (int i = 0; i < args.length; ++i) {
            log.info("args[{}]: {}", i, args[i]);
        }
    }

    @PreDestroy
    public void onDestroy() {
        log.info("Shutting down Medusa - live for {}", Instance.getUptime());
        slashCommandService.saveAllCached();
        log.info("Shutdown complete");
    }

}
