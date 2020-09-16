package com.w1sh.medusa;

import com.w1sh.medusa.core.Instance;
import com.w1sh.medusa.services.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PreDestroy;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Slf4j
@SpringBootApplication(exclude = MongoReactiveRepositoriesAutoConfiguration.class)
@PropertySource(value = "classpath:messages_en.properties")
@RequiredArgsConstructor
public class Main implements CommandLineRunner {

    private final Instance instance;
    private final AudioConnectionManager audioConnectionManager;

    public static void main(String[] args) {
        String now = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date.from(Instance.START_INSTANCE));
        log.info("Booting Medusa - {}", now);
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
    public void onDestroy(){
        audioConnectionManager.shutdown();
        String duration = MessageService.formatDuration(Duration.between(Instance.START_INSTANCE, Instant.now()).toMillis());
        log.info("Closing Medusa - Alive for {}", duration);
    }

}
