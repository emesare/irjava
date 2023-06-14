package com.emesare.irjava.command;

import com.emesare.irjava.ClientHandler;
import com.emesare.irjava.Message;
import com.emesare.irjava.ServerHandler;

public interface Command {
    // TODO: Switch return to a callback func that can actually transmit to every other guest?
    // TODO: Remove Message return type, we barely use it do to the multi-message server commands.
    Message handleMessage(ServerHandler serverHandler, ClientHandler clientHandler, Message clientMessage);

    // TODO: constructMessage, i.e. PingCommand should not create a new message themselves? They might actually need to.
}