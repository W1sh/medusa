package commands;

public class PingCommand extends Command {

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String getDescription() {
        return "A simple answer from the bot.";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public String execute() {
        return "Pong!";
    }
}
