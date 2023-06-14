package com.emesare.irjava;

import java.util.ArrayList;

public class ChannelHandler {
    private final Channel channel;
    private final ArrayList<ClientHandler> clientHandlers;


    public ChannelHandler(Channel channel) {
        this.channel = channel;
        this.clientHandlers = new ArrayList<>();
    }

    public Channel getChannel() {
        return this.channel;
    }

    public boolean removeClient(Client client) {
        return this.clientHandlers.removeIf(clientHandler -> clientHandler.getClient().equals(client));
    }

    public boolean removeClientHandler(ClientHandler clientHandler) {
        return this.clientHandlers.remove(clientHandler);
    }

    public ArrayList<String> getClientHostMasks() {
        return new ArrayList<String>(this.clientHandlers.stream().map(ClientHandler::getClient).map(Client::getHostmask).toList());
    }

    public boolean addClientHandler(ClientHandler clientHandler) {
        if (!this.clientHandlers.contains(clientHandler)) {
            return this.clientHandlers.add(clientHandler);
        } else {
            return false; // Client handler is already present, no need to add it again.
        }
    }

    // TODO: Rename this method to something nicer.
    public boolean clientHandlerPresent(ClientHandler clientHandler) {
        return this.clientHandlers.contains(clientHandler);
    }

    public void messageAll(Message message) {
        for (ClientHandler clientHandler : this.clientHandlers) {
            clientHandler.writeMessage(message);
        }
    }

    // TODO: Rename this more appropriately, as well as renaming `skippedClientHandler`
    public void messageAllExcept(Message message, ClientHandler skippedClientHandler) {
        for (ClientHandler clientHandler : this.clientHandlers) {
            if (!clientHandler.equals(skippedClientHandler)) {
                clientHandler.writeMessage(message);
            }
        }
    }
}
