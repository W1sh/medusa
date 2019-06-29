package handlers;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import entities.GenericRepository;
import entities.User;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import javax.persistence.PersistenceException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DatabaseHandler {

    public static void initializeDatabase(DiscordClient client){
        GenericRepository<User, Long> genericRepository = new GenericRepository<>(User.class);
        client.getGuilds()
                .flatMap(Guild::getMembers)
                .filter(member -> !member.isBot())
                .filter(member -> {
                    Tuple2<String, Long> tuple1 = Tuples.of("discordId", member.getId().asLong());
                    Tuple2<String, Long> tuple2 = Tuples.of("guildId", member.getGuildId().asLong());
                    try {
                        genericRepository.read(tuple1, tuple2);
                        return false;
                    } catch (PersistenceException e) {
                        //log exception, !Important
                        return true;
                    }
                })
                .doOnNext(member -> genericRepository.persist(new User(member)))
                .subscribe();
    }

    public static void initializeAutomaticPointIncrementation(){
        GenericRepository<User, Long> genericRepository = new GenericRepository<>(User.class);
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(() -> {
            System.out.println("TIME TO GET POINTS"); // flogger
            genericRepository.read().forEach(user -> {
                /*Query updateQuery = entityManager.createQuery("update User set points = :points where id = :id");
                updateQuery.setParameter("points", (user.getPoints() + 100));
                updateQuery.setParameter("id", user.getId());
                updateQuery.executeUpdate();*/
            });
        },1, 1, TimeUnit.HOURS);
    }
}
