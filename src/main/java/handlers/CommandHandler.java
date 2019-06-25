package handlers;

import commands.Command;
import discord4j.core.object.entity.Message;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandHandler {

    private static final Map<String, Command> commands = new HashMap<>();

    private CommandHandler(){}

    public static void executeCommand(Message command){
        String commandName = command.getContent().map(s -> s.split(" ")[0]).orElse("NOT FOUND");
        commandName = commandName.substring(1);
        if(commands.containsKey(commandName)){
            String response = commands.get(commandName).execute();
            command.getChannel().flatMap(messageChannel -> messageChannel.createMessage(response)).subscribe();
        }
    }

    public static void loadCommands() {
        Reflections reflections = new Reflections("commands");
        Set<Class<? extends Command>> classes = reflections.getSubTypesOf(Command.class);
        classes.forEach(item -> {
            try {
                Command command = item.getConstructor().newInstance();
                if(!commands.containsKey(command.getName())){
                    commands.put(command.getName(), command);
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        });
    }
}
