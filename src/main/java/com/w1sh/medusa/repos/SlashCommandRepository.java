package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.SlashCommand;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SlashCommandRepository {

    Mono<Long> countAll();

    void saveAll(List<SlashCommand> slashCommands);
}
