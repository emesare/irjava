package com.emesare.irjava.command;

import com.emesare.irjava.ClientHandler;
import com.emesare.irjava.Message;
import com.emesare.irjava.ServerHandler;

public class PingCommand implements Command {

    @Override
    public Message handleMessage(ServerHandler serverHandler, ClientHandler clientHandler, Message clientMessage) {
        // TODO: Replace this with something less hardcoded
        return new Message.Builder("PONG").parameters(clientMessage.getParameters()).build();
    }
}
