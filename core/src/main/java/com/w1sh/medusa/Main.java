package com.w1sh.medusa;

import com.w1sh.medusa.core.Instance;
import com.w1sh.medusa.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PreDestroy;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@SpringBootApplication
@PropertySource(value = "classpath:text-constants.properties")
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String now = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date.from(Instance.START_INSTANCE));
        logger.info("Booting Medusa - {}", now);
        Thread.currentThread().setName("medusa-main");
        SpringApplication.run(Main.class, args);
    }

    @PreDestroy
    public void onDestroy(){
        String duration = ResponseUtils.formatDuration(Duration.between(Instance.START_INSTANCE, Instant.now()).toMillis());
        logger.info("Closing Medusa - Alive for {}", duration);
    }
}
