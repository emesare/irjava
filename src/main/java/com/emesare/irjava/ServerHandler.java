package com.emesare.irjava;

import com.emesare.irjava.command.CommandHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

// TODO: Make this a Thread or Runnable or something?
public class ServerHandler {
    // TODO: Add a config attribute (port and other configurable stuff)
    private final ServerConfig serverConfig;
    private final ArrayList<ClientHandler> clientHandlers;
    private final ArrayList<ChannelHandler> channelHandlers;
    private boolean halted;

    public ServerHandler() {
        this.serverConfig = new ServerConfig();
        this.clientHandlers = new ArrayList<>();
        this.channelHandlers = new ArrayList<>();
        this.halted = false;
    }

    public ServerHandler(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.clientHandlers = new ArrayList<>();
        this.channelHandlers = new ArrayList<>();
        this.halted = false;
    }

    public void run() {
        while (!this.halted) {
            try (ServerSocket serverSocket = new ServerSocket(this.serverConfig.getPortNumber())) {
                serverSocket.setReuseAddress(true); // Less dead-time between creation of connections.
                Socket socket = serverSocket.accept();
                System.out.println("remote connection created (" + socket.getInetAddress().getCanonicalHostName() + ")");
                new Thread(new ClientHandler(new CommandHandler(this), socket)).start(); // TODO: Server handler now needs to register a channel (OR CALLBACK?) with ClientHandler to send stuff back.
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // TODO: Explore this and maybe give this to the client handler IDFK?
    public void messageAll(Message message) {
        for (ClientHandler clientHandler : this.clientHandlers) {
            clientHandler.writeMessage(message);
        }
    }

    /**
     * Safely terminates all open client connections and quits. TODO: Do that :)
     */
    public void stop() {
        this.halted = true;
    }

    public ArrayList<String> getChannelNames() {
        return new ArrayList<String>(this.channelHandlers.stream().map(ChannelHandler::getChannel).map(Channel::getName).toList());
    }

    public ArrayList<ChannelHandler> getChannelHandlers() {
        return this.channelHandlers;
    }

    public void addChannelHandler(ChannelHandler channelHandler) {
        this.channelHandlers.add(channelHandler);
    }

    // TODO: Find more suitable method name.
    public boolean nicknameTaken(String nickname) {
        return this.clientHandlers.stream().anyMatch(clientHandler -> Objects.equals(clientHandler.getClient().getNickname(), nickname));
    }
}
