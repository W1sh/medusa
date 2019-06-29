package handlers;

import discord4j.command.CommandBootstrapper;
import discord4j.command.util.AbstractCommandDispatcher;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class CommandHandler {

    private CommandHandler(){}

    private static class SimpleCommandDispatcher extends AbstractCommandDispatcher {
        private final String prefix;

        public SimpleCommandDispatcher(String prefix) {
            this.prefix = prefix;
        }

        @Override
        protected Publisher<String> getPrefixes(MessageCreateEvent event) {
            return Mono.just(prefix);
        }
    }

    public static void setupCommands(DiscordClient client) {
        SimpleCommandDispatcher dispatcher = new SimpleCommandDispatcher("!"); //Handles triggering commands using our ! prefix
        CommandBootstrapper bootstrapper = new CommandBootstrapper(dispatcher); //This mediates all internal logic for commands
        //bootstrapper.addProvider(new MyCommandProvider()); //Register our command provider
        bootstrapper.attach(client).subscribe(); //Attach the provider to the client and activate it
    }

    /*public static void executeCommand(Message command){
        String commandName = command.getContent().map(s -> s.split(" ")[0]).orElse("NOT FOUND");
        commandName = commandName.substring(1);
        if(commands.containsKey(commandName)){
            //String response = commands.get(commandName).execute();
            //command.getChannel().flatMap(messageChannel -> messageChannel.createMessage(response)).subscribe();public class SimpleCommandDispatcher extends AbstractCommandDispatcher {
        private final String prefix;

        public SimpleCommandDispatcher(String prefix) {
            this.prefix = prefix;
        }

        @Override
        protected Publisher<String> getPrefixes(MessageCreateEvent event) {
            return Mono.just(prefix);
        }
    }

    public static void setupCommands(DiscordClient client) {
        SimpleCommandDispatcher dispatcher = new SimpleCommandDispatcher("!"); //Handles triggering commands using our ! prefix
        CommandBootstrapper bootstrapper = new CommandBootstrapper(dispatcher); //This mediates all internal logic for commands
        bootstrapper.addProvider(new MyCommandProvider()); //Register our command provider
        bootstrapper.attach(client).subscribe(); //Attach the provider to the client and activate it
    }
        }
    }

    public static void loadCommands() {
        Reflections reflections = new Reflections("commands");
        Set<Class<? extends AbstractCommand>> classes = reflections.getSubTypesOf(AbstractCommand.class);
        classes.forEach(item -> {
            try {
                AbstractCommand abstractCommand = item.getConstructor().newInstance();
                if(!commands.containsKey(abstractCommand.getName())){
                    commands.put(abstractCommand.getName(), abstractCommand);
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        });
    }*/
}
