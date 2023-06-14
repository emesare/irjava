package com.emesare.irjava.command;

import com.emesare.irjava.ChannelHandler;
import com.emesare.irjava.ClientHandler;
import com.emesare.irjava.Message;
import com.emesare.irjava.ServerHandler;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Command used to remove a client from channels.
 *
 * <a href="https://modern.ircdocs.horse/#part-message">Reference</a>
 */
public class PartCommand implements Command {

    @Override
    public Message handleMessage(ServerHandler serverHandler, ClientHandler clientHandler, Message clientMessage) {
        ArrayList<String> params = clientMessage.getParameters();
        String[] channelsNames = params.get(0).split(",");
        // TODO: Check if client is over their channel limit. (CHANLIMIT)
        // TODO: Check for minimum number of params.

        for (String channelName : channelsNames) {
            Optional<ChannelHandler> wrappedChanHandler = serverHandler.getChannelHandlers().stream().filter(ch -> ch.getChannel().getName().equals(channelName)).findFirst();
            if (wrappedChanHandler.isPresent()) {
                ChannelHandler channelHandler = wrappedChanHandler.get();

                // Check if client is in the channel.
                if (channelHandler.clientHandlerPresent(clientHandler)) {
                    // Remove the client from the channel.
                    channelHandler.removeClientHandler(clientHandler);

                    // Alert everyone on the channel that the user has left.
                    String clientHostmask = clientHandler.getClient().getHostmask();
                    // :hostmask PART <channel>{,<channel>} [<reason>]
                    Message.Builder messageBuilder = new Message.Builder().command("PART").source(clientHostmask).parameter(channelName);
                    // Add the clients parting reason to the broadcast message.
                    if (params.size() > 1) {
                        messageBuilder.parameter(params.get(1));
                    }
                    channelHandler.messageAll(new Message.Builder().command("PART").source(clientHostmask).parameter(channelName).build());

                } else {
                    // TODO: ERR_NOTONCHANNEL (442)
                }
            } else {
                // TODO: Reply with no such channel (403)
            }
        }

        return null;
    }
}
