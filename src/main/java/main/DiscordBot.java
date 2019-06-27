package main;

import commands.AbstractCommand;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import handlers.CommandHandler;
import handlers.DatabaseHandler;
import reactor.core.publisher.Mono;
import utils.Vault;

import javax.persistence.*;

class DiscordBot {

    private final String token = Vault.fetch("discord_token");
    private final DiscordClient client = new DiscordClientBuilder(token).build();

    public DiscordBot() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PersistenceUnit");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        client.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(ready -> {
                    DatabaseHandler.initializeDatabase(em, client);
                    DatabaseHandler.initializeAutomaticPointIncrementation(em);
                    System.out.println("Logged in as " + ready.getSelf().getUsername());
                    System.out.println("Currently serving " + ready.getGuilds().size() + " servers");
                });

        client.getEventDispatcher().on(DisconnectEvent.class)
                .doOnNext(disconnectEvent -> em.getTransaction().commit())
                .subscribe();

        client.getEventDispatcher().on(GuildCreateEvent.class)
                .map(GuildCreateEvent::getGuild)
                //.doOnNext(/*register guild*/)
                .subscribe();

        client.getEventDispatcher().on(GuildDeleteEvent.class)
                .filter(guildDeleteEvent -> !guildDeleteEvent.isUnavailable())
                .map(GuildDeleteEvent::getGuild)
                //.doOnNext(/*register guild*/)
                .subscribe();

        client.getEventDispatcher().on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false) &&
                        message.getContent().map(content -> content.startsWith(AbstractCommand.COMMAND_PREFIX)).orElse(false))
                .doOnNext(CommandHandler::executeCommand)
                .onErrorResume(e -> Mono.empty())
                .subscribe();

        client.getEventDispatcher().on(MemberJoinEvent.class)
                .map(MemberJoinEvent::getMember)
                //.doOnNext(/*do something*/)
                .onErrorResume(e -> Mono.empty())
                .subscribe();

        client.getEventDispatcher().on(MemberLeaveEvent.class)
                .map(MemberLeaveEvent::getUser)
                //.doOnNext(/*do something*/)
                .onErrorResume(e -> Mono.empty())
                .subscribe();

        client.login().block();
    }
}
