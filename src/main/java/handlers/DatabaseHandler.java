package handlers;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.presence.Status;
import entity.entities.User;
import entity.repositories.impl.UserRepository;
import entity.services.impl.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.concurrent.TimeUnit;

public class DatabaseHandler {

    private static final UserRepository userRepository = UserRepository.getInstance();
    private static final UserService service = new UserService();

    private DatabaseHandler(){}

    public static void initializeDatabase(DiscordClient client){
        client.getGuilds()
                .flatMap(Guild::getMembers)
                .filter(member -> !member.isBot())
                .map(User::new)
                .flatMap(user -> Mono.when(service.persist(user)))
                .subscribe();
    }

    public static void initializeAutomaticPointIncrementation() {
        Schedulers.single().schedulePeriodically(() -> {
            System.out.println("TIME TO GET POINTS"); // flogger
            /*userRepository.read().forEach(user -> {
                Tuple2<String, Long> tuple1 = Tuples.of("points", (long) (user.getPoints() + 100));
                Tuple2<String, Long> tuple2 = Tuples.of("id", (long) user.getId());
                // log update result
                System.out.println(userRepository.update(tuple1, tuple2));
            });*/
            UserService userService = new UserService();
            Flux.fromStream(userRepository.read()).map(userService::update).subscribe();
        }, 1, 1, TimeUnit.HOURS);
    }
}
