package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.GuildUser;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GuildUserRepository extends ReactiveCrudRepository<GuildUser, Integer> {

    @Query(value = "SELECT * FROM core.guilds_users WHERE user = :userId AND guild_id = :guildId")
    Mono<GuildUser> findByUserIdAndGuildId(Integer userId, String guildId);

    @Query(value = "SELECT * FROM core.guilds_users WHERE guild_id = :guildId")
    Flux<GuildUser> findByGuildId(String guildId);

    @Query(value = "SELECT * FROM core.guilds_users WHERE guild_id = :guildId ORDER BY points DESC")
    Flux<GuildUser> findAllByGuildIdOrderByPoints(String guildId);
}
