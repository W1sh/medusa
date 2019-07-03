package handlers;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.presence.Status;
import entity.entities.User;
import entity.repositories.implementations.UserRepository;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DatabaseHandler {

    private static final UserRepository userRepository = UserRepository.getInstance();

    private DatabaseHandler(){}

    public static void initializeDatabase(DiscordClient client){
        client.getGuilds()
                .flatMap(Guild::getMembers)
                .filter(member -> !member.isBot())
                .filter(member -> {
                    Presence presence = member.getPresence().block();
                    return presence != null && presence.getStatus().equals(Status.ONLINE);
                })
                .filter(member -> {
                    final Tuple2<String, Long>[] tuples = new Tuple2[2];
                    tuples[0] = Tuples.of("discordId", member.getId().asLong());
                    tuples[1] = Tuples.of("guildId", member.getGuildId().asLong());
                    return userRepository.read(tuples) == null;
                })
                .doOnNext(member -> userRepository.persist(new User(member)))
                .subscribe();
    }

    public static void initializeAutomaticPointIncrementation(){
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(() -> {
            System.out.println("TIME TO GET POINTS"); // flogger
            userRepository.read().forEach(user -> {
                Tuple2<String, Long> tuple1 = Tuples.of("points", (long) (user.getPoints() + 100));
                Tuple2<String, Long> tuple2 = Tuples.of("id", (long) user.getId());
                // log update result
                System.out.println(userRepository.update(tuple1, tuple2));
            });
        },1, 1, TimeUnit.HOURS);
    }
}
