package commands;

public abstract class Command {

    public static final String COMMAND_PREFIX = "!";

    Command() { }

    public abstract String execute();

    public abstract String getName();

    public abstract String getDescription();

    public abstract boolean isAdminOnly();
}
