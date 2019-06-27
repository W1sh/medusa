package commands;

import discord4j.command.Command;

public abstract class AbstractCommand implements Command {

    public static final String COMMAND_PREFIX = "!";

    public abstract String getName();

    public abstract String getDescription();

    public abstract boolean isAdminOnly();

}
