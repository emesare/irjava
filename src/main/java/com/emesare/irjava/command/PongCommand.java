package com.emesare.irjava.command;

import com.emesare.irjava.ClientHandler;
import com.emesare.irjava.Message;
import com.emesare.irjava.ServerHandler;

public class PongCommand implements Command {
    @Override
    public Message handleMessage(ServerHandler serverHandler, ClientHandler clientHandler, Message clientMessage) {
        // TODO: Pong sends no message back maybe we should just log (time between last ping message we sent (that matches params)?)?
        System.out.println("Pong!");
        return null;
    }
}
