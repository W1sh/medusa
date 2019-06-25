package main;

import commands.Command;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import entities.User;
import handlers.CommandHandler;
import reactor.core.publisher.Mono;
import utils.Vault;

import javax.persistence.*;

class DiscordBot {

    public DiscordBot() {
        String token = Vault.fetch("discord_token");
        final DiscordClient client = new DiscordClientBuilder(token).build();

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PersistenceUnit");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        client.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(ready -> {
                    client.getGuilds()
                            .flatMap(Guild::getMembers)
                            .filter(member -> !member.isBot())
                            .filter(member -> {
                                Query findUser = em.createQuery(
                                        "from User user where user.discordId = :discordId and user.guildId = :guildId");
                                findUser.setParameter("discordId", member.getId().asLong());
                                findUser.setParameter("guildId", member.getGuildId().asLong());
                                System.out.println(new User(member).toString());
                                return findUser.getResultList().isEmpty();
                            })
                            .doOnNext(member -> {
                                try {
                                    em.persist(new User(member));
                                }catch (PersistenceException e){
                                    //flogger?
                                }
                            })
                            .subscribe();
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
                        message.getContent().map(content -> content.startsWith(Command.COMMAND_PREFIX)).orElse(false))
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
