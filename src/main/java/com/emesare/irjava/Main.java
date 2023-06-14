package com.emesare.irjava;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("(temp) irjava IRC server started!");
        // TODO: Add mechanism to stop loop and close down server (Partly done, fork off the serverHandler to another thread to actually interrupt it).
        ServerHandler serverHandler = new ServerHandler();
        serverHandler.run();
    }
}
