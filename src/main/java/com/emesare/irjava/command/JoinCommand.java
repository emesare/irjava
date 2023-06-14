package com.emesare.irjava.command;

import com.emesare.irjava.*;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Command used by clients to indicate that they want to join the given channel(s).
 *
 * <a href="https://modern.ircdocs.horse/#join-message">Reference</a>
 */
public class JoinCommand implements Command {
    @Override
    public Message handleMessage(ServerHandler serverHandler, ClientHandler clientHandler, Message clientMessage) {
        ArrayList<String> params = clientMessage.getParameters();
        String clientNickname = clientHandler.getClient().getNickname();
        String[] channelsNames = params.get(0).split(",");
        String[] keys = null;

        if (params.size() > 1) {
            keys = params.get(1).split(",");
        }

        // TODO: Check if client is over their channel limit. (CHANLIMIT)

        for (int i = 0; i < channelsNames.length; i++) {
            String channelName = channelsNames[i];
            // TODO: Add all channel prefixes, and choose appropriately.
            if (!channelName.startsWith("#")) {
                channelName = "#" + channelName; // If the channel they provide is missing #, add it.
            }

            String finalChannelName = channelName;
            Optional<ChannelHandler> wrappedChanHandler = serverHandler.getChannelHandlers().stream().filter(ch -> ch.getChannel().getName().equals(finalChannelName)).findFirst();
            if (wrappedChanHandler.isPresent()) {
                ChannelHandler channelHandler = wrappedChanHandler.get();

                // If key is present for channel validate the clients key against it.
                String channelKey = channelHandler.getChannel().getKey();
                if (!channelKey.isEmpty()) {
                    boolean validated = false;
                    if (keys != null) {
                        String key = keys[i];
                        if (key != null) {
                            if (key.equals(channelKey)) {
                                validated = true;
                            }
                        }
                    }

                    if (!validated) {
                        // TODO: ERR_BADCHANNELKEY (475)
                    }
                }

                System.out.println("client -> " + clientNickname + " joined -> " + channelName);

                // Register the client handler with the channel.
                channelHandler.addClientHandler(clientHandler);

                // Alert everyone on the channel that the user has joined.
                String clientHostmask = clientHandler.getClient().getHostmask();
                channelHandler.messageAll(new Message.Builder().command("JOIN").source(clientHostmask).parameter(channelName).build());

                // Send the newly joined client the channel's topic.
                String channelTopic = channelHandler.getChannel().getTopic();
                if (!channelTopic.isBlank()) {
                    // <client> <channel> :<topic>
                    clientHandler.writeMessage(new Message.Builder(Message.Builder.ServerReplyCode.RPL_TOPIC, "myServer", clientNickname).parameter(channelName).parameter(channelTopic).build());
                }

                // TODO: Send reply RPL_TOPICWHOTIME (optional, however why not!)

                // Send all currently joined users of the channel to the newly joined client.
                for (String clientHostMask : channelHandler.getClientHostMasks()) {
                    // <client> <symbol> <channel> :[prefix]<nick>{ [prefix]<nick>}
                    // TODO: Switch the hardcoded `=` channel symbol for the actual clients status.
                    clientHandler.writeMessage(new Message.Builder(Message.Builder.ServerReplyCode.RPL_NAMREPLY, "myServer", clientNickname).parameter("=").parameter(channelName).parameter(clientHostMask).build());
                }
                // Send end of names to let client know there are no more RPL_NAMREPLY as a result of executing the JOIN command.
                clientHandler.writeMessage(new Message.Builder(Message.Builder.ServerReplyCode.RPL_ENDOFNAMES, "myServer", clientNickname).parameter("=").parameter(channelName).parameter("End of /NAMES list").build());
            } else {
                // TODO: Reply with no such channel (403) (Or wait, don't we want to create it instead?)
                // Create a channel, add the user who wants it then register it with the server handler.
                ChannelHandler channelHandler = new ChannelHandler(new Channel(channelName));
                channelHandler.addClientHandler(clientHandler);
                serverHandler.addChannelHandler(channelHandler);
                System.out.println("client -> " + clientNickname + " created -> " + channelName);
            }
        }

        return null;
    }
}
