package handlers;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.presence.Status;
import entities.GenericRepository;
import entities.User;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DatabaseHandler {

    private static final GenericRepository<User, Long> genericRepository = new GenericRepository<>(User.class);

    public static void initializeDatabase(DiscordClient client){
        client.getGuilds()
                .flatMap(Guild::getMembers)
                .filter(member -> !member.isBot())
                .filter(member -> {
                    Presence presence = member.getPresence().block();
                    return presence != null && presence.getStatus().equals(Status.ONLINE);
                })
                .filter(member -> {
                    Tuple2<String, Long> tupleDiscordId = Tuples.of("discordId", member.getId().asLong());
                    Tuple2<String, Long> tupleGuildId = Tuples.of("guildId", member.getGuildId().asLong());
                    return genericRepository.read(tupleDiscordId, tupleGuildId) == null;
                })
                .doOnNext(member -> genericRepository.persist(new User(member)))
                .subscribe();
    }

    public static void initializeAutomaticPointIncrementation(){
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(() -> {
            System.out.println("TIME TO GET POINTS"); // flogger
            genericRepository.read().forEach(user -> {
                Tuple2<String, Long> tuple1 = Tuples.of("points", (long) (user.getPoints() + 100));
                Tuple2<String, Long> tuple2 = Tuples.of("id", (long) user.getId());
                // log update result
                System.out.println(genericRepository.update(tuple1, tuple2));
            });
        },1, 1, TimeUnit.HOURS);
    }
}
