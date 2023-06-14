package com.emesare.irjava.command;

import com.emesare.irjava.ChannelHandler;
import com.emesare.irjava.ClientHandler;
import com.emesare.irjava.Message;
import com.emesare.irjava.ServerHandler;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Command used to send messages.
 *
 * <a href="https://modern.ircdocs.horse/#privmsg-message">Reference</a>
 */
public class MessageCommand implements Command {
    @Override
    public Message handleMessage(ServerHandler serverHandler, ClientHandler clientHandler, Message clientMessage) {
        ArrayList<String> params = clientMessage.getParameters();
        // TODO: Add support for multiple targets and client-to-client messaging.
        String channelName = params.get(0);
        String messageContent = params.get(1);

        if (messageContent.isBlank()) {
            // TODO: Reply with ERR_NOTEXTTOSEND (412)
        }

        // TODO: We need to make getting the channel handler less code.
        Optional<ChannelHandler> wrappedChanHandler = serverHandler.getChannelHandlers().stream().filter(ch -> ch.getChannel().getName().equals(channelName)).findFirst();
        if (wrappedChanHandler.isPresent()) {
            ChannelHandler channelHandler = wrappedChanHandler.get();
            // Check if client is in the channel.
            if (channelHandler.clientHandlerPresent(clientHandler)) {
                // Message every client on the channel except the source of the message.
                String clientHostmask = clientHandler.getClient().getHostmask();
                // Parameters: <target>{,<target>} <text to be sent>
                channelHandler.messageAllExcept(new Message.Builder().command("PRIVMSG").source(clientHostmask).parameter(channelName).parameter(messageContent).build(), clientHandler);
            } else {
                // TODO: Reply with ERR_CANNOTSENDTOCHAN (404)
            }
        }

        return null;
    }
}
