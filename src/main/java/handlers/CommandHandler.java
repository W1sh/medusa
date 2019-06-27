package handlers;

import commands.AbstractCommand;
import discord4j.core.object.entity.Message;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandHandler {

    private static final Map<String, AbstractCommand> commands = new HashMap<>();

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
    }
}
