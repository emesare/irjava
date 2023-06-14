package com.emesare.irjava.command;

import com.emesare.irjava.Client;
import com.emesare.irjava.ClientHandler;
import com.emesare.irjava.Message;
import com.emesare.irjava.ServerHandler;

import java.util.ArrayList;

/**
 * Command used by clients to set their nickname.
 *
 * <a href="https://modern.ircdocs.horse/#nick-message">Reference</a>
 */
public class NickCommand implements Command {
    @Override
    public Message handleMessage(ServerHandler serverHandler, ClientHandler clientHandler, Message clientMessage) {
        ArrayList<String> params = clientMessage.getParameters();

        if (params.size() > 0) {
            String nickname = params.get(0);
            if (Client.verifyNickname(nickname)) {
                // Check to make sure not a duplicate nickname.
                if (!serverHandler.nicknameTaken(nickname)) {
                    // Nickname is not taken, switch clients nickname.
                    clientHandler.getClient().setNickname(nickname);
                    System.out.println("(temp) client updated -> " + clientHandler.getClient());
                } else {
                    // TODO: Reply with 433 (Nick in use)
                }
            } else {
                // TODO: Reply with nick has invalid characters.
            }
        } else {
            // TODO: Reply with not enough params (we should have this reply saved prematurely somewhere in serverHandler IMO)
        }

        return null;
    }
}
