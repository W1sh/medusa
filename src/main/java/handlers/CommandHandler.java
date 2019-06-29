package handlers;

import commands.PingCommand;
import discord4j.command.CommandBootstrapper;
import discord4j.command.CommandProvider;
import discord4j.command.ProviderContext;
import discord4j.command.util.AbstractCommandDispatcher;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class CommandHandler {

    private CommandHandler(){}

    private static class SimpleCommandDispatcher extends AbstractCommandDispatcher {
        private final String prefix;

        SimpleCommandDispatcher(String prefix) {
            this.prefix = prefix;
        }

        @Override
        protected Publisher<String> getPrefixes(MessageCreateEvent event) {
            return Mono.just(prefix);
        }
    }

    private static class SimpleCommandProvider implements CommandProvider {

        @Override
        public Publisher<ProviderContext> provide(MessageCreateEvent context, String commandName, int commandStartIndex, int commandEndIndex) {
            return Mono.just(commandName)
                    .map(command -> ProviderContext.of(new PingCommand()))
                    .flux();
        }
    }

    public static void setupCommands(DiscordClient client) {
        SimpleCommandDispatcher dispatcher = new SimpleCommandDispatcher("!"); //Handles triggering commands using our ! prefix
        CommandBootstrapper bootstrapper = new CommandBootstrapper(dispatcher); //This mediates all internal logic for commands
        bootstrapper.addProvider(new SimpleCommandProvider()); //Register our command provider
        bootstrapper.attach(client).subscribe(); //Attach the provider to the client and activate it
    }
}
