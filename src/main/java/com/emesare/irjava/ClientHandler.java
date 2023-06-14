package com.emesare.irjava;

import com.emesare.irjava.command.CommandHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
    // TODO: Replace socket with some generic stuff so we can communicate over any buffer?
    private final Socket socket;
    private final PrintWriter writer;
    private final BufferedReader reader;
    private final Client client;
    private final CommandHandler commandHandler;

    public ClientHandler(CommandHandler commandHandler, Socket socket) throws IOException {
        this.socket = socket;
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.client = new Client("", socket.getInetAddress().getCanonicalHostName(), "", ""); // Client hasn't registered yet! TODO: ClientHandler needs to get the client to register a new Client themselves instead of using setters everywhere!!
        this.commandHandler = commandHandler;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String line = this.reader.readLine();
                Message message = Message.fromString(line);
                if (message == null) {
                    System.out.println("(temp) message received was null!");
                    continue;
                }
                this.commandHandler.consumeMessage(this, message);

                // TODO: Replace with
                if (message.getCommand().equals("QUIT")) {
                    System.out.println("(temp) client quit!");
                    // TODO: How can we tell every other client about this, maybe the ClientThread stores a channel like in Go?
                    break;
                }
            }

            close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeMessage(Message message) {
        this.writer.println(message);
    }

    private void close() throws IOException {
        this.reader.close();
        this.writer.close();
        this.socket.close();
    }

    // TODO: Do we need this?
    public Client getClient() {
        return this.client;
    }
}