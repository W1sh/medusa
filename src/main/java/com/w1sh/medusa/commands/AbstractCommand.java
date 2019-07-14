package com.w1sh.medusa.commands;

import discord4j.command.Command;

public abstract class AbstractCommand implements Command {

    public abstract String getName();

    public abstract String getDescription();

    public abstract boolean isAdminOnly();

}
