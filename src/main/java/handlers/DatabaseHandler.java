package handlers;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import entities.GenericRepository;
import entities.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DatabaseHandler {

    public static void initializeDatabase(EntityManager entityManager, DiscordClient client){
        client.getGuilds()
                .flatMap(Guild::getMembers)
                .filter(member -> !member.isBot())
                .filter(member -> {
                    Query findUser = entityManager.createQuery(
                            "from User user where user.discordId = :discordId and user.guildId = :guildId");
                    findUser.setParameter("discordId", member.getId().asLong());
                    findUser.setParameter("guildId", member.getGuildId().asLong());
                    return findUser.getResultList().isEmpty();
                })
                .doOnNext(member -> {
                    try {
                        entityManager.persist(new User(member));
                    }catch (PersistenceException e){
                        //flogger?
                    }
                })
                .subscribe();
    }

    @SuppressWarnings(value = "unchecked")
    public static void initializeAutomaticPointIncrementation(EntityManager entityManager){
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(() -> {
            System.out.println("TIME TO GET POINTS"); // flogger
            GenericRepository<User, Long> genericRepository = new GenericRepository<>(User.class, entityManager);
            genericRepository.read().forEach(user -> {
                Query updateQuery = entityManager.createQuery("update User set points = :points where id = :id");
                updateQuery.setParameter("points", (user.getPoints() + 100));
                updateQuery.setParameter("id", user.getId());
                updateQuery.executeUpdate();
            });
        },1, 1, TimeUnit.HOURS);
    }
}
