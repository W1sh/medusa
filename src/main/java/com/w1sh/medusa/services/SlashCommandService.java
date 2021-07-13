package com.w1sh.medusa.services;

import com.w1sh.medusa.data.SlashCommand;
import com.w1sh.medusa.repos.SlashCommandRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public final class SlashCommandService {

    private static final Logger log = LoggerFactory.getLogger(SlashCommandService.class);
    private final SlashCommandRepository slashCommandRepository;
    private final List<SlashCommand> slashCommands;

    @Value("${medusa.commands.save.interval}")
    private String saveInterval;
    @Value("${medusa.commands.save.delay}")
    private String saveDelay;

    public SlashCommandService(SlashCommandRepository slashCommandRepository) {
        this.slashCommandRepository = slashCommandRepository;
        this.slashCommands = new ArrayList<>();
    }

    @PostConstruct
    private void init(){
        scheduleBatchSave();
    }

    public void save(SlashCommand slashCommand){
        slashCommands.add(slashCommand);
    }

    public Mono<Long> countAll(){
        log.info("Counting all slash commands in database");
        return slashCommandRepository.countAll()
                .mergeWith(Mono.justOrEmpty((long) slashCommands.size()))
                .reduce(0L, Long::sum)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to count all events", t)));
    }

    private void scheduleBatchSave() {
        log.info("Registering periodically batch save of slash commands with delay of {} hours and interval of {} hours",
                saveDelay, saveInterval);
        Schedulers.boundedElastic().schedulePeriodically(this::saveAllCached, Integer.parseInt(saveDelay),
                Integer.parseInt(saveInterval), TimeUnit.HOURS);
    }

    public void saveAllCached() {
        if (slashCommands.isEmpty()) return;
        log.info("Starting batch save of {} slash commands", slashCommands.size());
        slashCommandRepository.saveAll(slashCommands);
        slashCommands.clear();
    }
}
