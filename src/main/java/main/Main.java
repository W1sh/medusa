package main;

import handlers.CommandHandler;

public class Main {
    public static void main(String[] args) {
        CommandHandler.loadCommands();
        DiscordBot bot = new DiscordBot();
    }
}
