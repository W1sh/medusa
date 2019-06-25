package commands;

public class HelpCommand extends Command{

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "show all commands and what they do.";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public String execute() {
        return "!ping -> Requests a \"Pong!\" response from the bot.";
    }
}
