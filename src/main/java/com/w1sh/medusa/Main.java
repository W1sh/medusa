package com.w1sh.medusa;

import com.w1sh.medusa.core.Instance;
import com.w1sh.medusa.services.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PreDestroy;

@SpringBootApplication(exclude = MongoReactiveRepositoriesAutoConfiguration.class)
@PropertySource(value = "classpath:messages_en.properties")
public class Main implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private final Instance instance;
    private final EventService eventService;

    public Main(Instance instance, EventService eventService) {
        this.instance = instance;
        this.eventService = eventService;
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
        eventService.saveAllCached();
        log.info("Shutdown complete");
    }

}
