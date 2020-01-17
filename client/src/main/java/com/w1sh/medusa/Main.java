package com.w1sh.medusa;

import com.w1sh.medusa.metrics.Trackers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import javax.annotation.PreDestroy;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@EnableReactiveMongoRepositories
@PropertySource(value = "classpath:text-constants.properties")
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static Instant startInstant;

    public static void main(String[] args) {
        startInstant = Instant.now();
        Trackers.setStartInstant(startInstant);
        String now = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date.from(startInstant));
        logger.info("Booting Medusa - {}", now);
        Thread.currentThread().setName("medusa-main");
        SpringApplication.run(Main.class, args);
    }

    @PreDestroy
    public void onDestroy(){
        logger.info("Closing Medusa - Alive for {}h", Duration.between(startInstant, Instant.now()).toHours());
    }
}
