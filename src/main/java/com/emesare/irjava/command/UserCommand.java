package com.emesare.irjava.command;

import com.emesare.irjava.ClientHandler;
import com.emesare.irjava.Message;
import com.emesare.irjava.ServerHandler;

import java.util.ArrayList;

/**
 * Command used by clients to set their username and realname.
 *
 * <a href="https://modern.ircdocs.horse/#user-message">Reference</a>
 */
public class UserCommand implements Command {
    // TODO: Research implementing IDENT protocol for realname. (RFC: https://datatracker.ietf.org/doc/html/rfc1413)

    @Override
    public Message handleMessage(ServerHandler serverHandler, ClientHandler clientHandler, Message clientMessage) {
        ArrayList<String> params = clientMessage.getParameters();
        Message replyMessage = null;

        if (clientHandler.getClient().getUsername().isEmpty()) {
            // TODO: We need a more streamlined way of dealing with a commands minimum number of parameters, so we dont have so much extra code.
            if (params.size() > 2) {
                String username = params.get(0);
                String realname = params.get(3);
                clientHandler.getClient().setUsername("~" + username); // We prepend ~ for user defined usernames to differentiate them from the IDENT ones.
                clientHandler.getClient().setRealName(realname);
                System.out.println("(temp) client updated -> " + clientHandler.getClient());
            } else {
                // TODO: Reply with not enough params (461)
                System.out.println("not enough params");
            }
        } else {
            // TODO: Reply with already registered (462)
        }

        return replyMessage;
    }
}
