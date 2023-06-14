package com.emesare.irjava.command;

import com.emesare.irjava.ClientHandler;
import com.emesare.irjava.Message;
import com.emesare.irjava.ServerHandler;

import java.util.ArrayList;
import java.util.HashMap;

// TODO: For all commands we need to store a few common replies in some object so we dont construct the message a billion times (maybe implement a cache as well?).

public class CommandHandler {
    private final HashMap<String, Command> commands;
    private final ServerHandler serverHandler;

    public CommandHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
        this.commands = new HashMap<>() {{
            put("PING", new PingCommand());
            put("PONG", new PongCommand());
            put("NICK", new NickCommand());
            put("USER", new UserCommand());
            put("JOIN", new JoinCommand());
            put("PART", new PartCommand());
            put("PRIVMSG", new MessageCommand());
//s            put("QUIT", new )
        }};
    }

    /**
     * Consumes a message for the serverHandler.
     *
     * @param clientHandler The client which sent the message.
     * @param message       The message which will be acted upon.
     */
    public void consumeMessage(ClientHandler clientHandler, Message message) {
        String messageCommand = message.getCommand();
        // TODO: Add a check to only allow unregistered clients to access a few whitelisted commands (NICK, USER, etc...)
        if (this.commands.containsKey(messageCommand)) {
            // TODO!!!: Passing around all these handlers is really awful.
            Message messageResponse = this.commands.get(messageCommand).handleMessage(this.serverHandler, clientHandler, message);
            if (messageResponse != null) {
                clientHandler.writeMessage(messageResponse);
            }
        } else {
            // Command not found, tell the client.
            System.out.println("client -> " + clientHandler.getClient().getNickname() + " unknown command -> " + messageCommand);
            clientHandler.writeMessage(new Message.Builder(Message.Builder.ServerReplyCode.ERR_UNKNOWNCOMMAND, "myServer", clientHandler.getClient().getNickname()).build());
        }
    }

    /**
     * Get command key names, useful for checking what commands are available.
     *
     * @return Array of command keys.
     */
    public ArrayList<String> getCommands() {
        return new ArrayList<>(this.commands.keySet());
    }
}
