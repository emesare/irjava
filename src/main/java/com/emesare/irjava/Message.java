package com.emesare.irjava;

import com.emesare.irjava.util.KeyValue;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * https://modern.ircdocs.horse/#message-format
 */
public class Message {
    /**
     * Optional metadata on a message, starting with ('@', 0x40).
     */
    private final ArrayList<KeyValue> tags;

    /**
     * Optional note of where the message came from, starting with (':', 0x3A).
     */
    private final String source;

    /**
     * The specific command this message represents.
     */
    private final String command; // TODO: Make this into some type, parse numerics into the enum too.

    /**
     * Optional data relevant to this specific command.
     */
    private final ArrayList<String> parameters;

    public Message(Builder builder) {
        this.tags = builder.tags;
        this.source = builder.source;
        this.command = builder.command;
        this.parameters = builder.parameters;
    }

    // TODO: Make a constructor for `fromString`

    public static Message fromString(String rawString) {
        ArrayList<KeyValue> tags = new ArrayList<>();
        String source = "";
        String command = "";
        ArrayList<String> params = new ArrayList<>();

        int cursor = 0;
        int nextSegment = rawString.indexOf(" ", cursor) + 1;

        // TODO: No spaces? so just a command? Do we have cases like this?
        if (nextSegment == 0) {
            return null;
        }

        // Parse tags.
        if (rawString.charAt(cursor) == 64) {
            for (String tag : rawString.substring(1, nextSegment).split(";")) {
                String[] sep = tag.split("=");
                ArrayList<String> values = new ArrayList<>();
                if (sep.length == 2) {
                    // Key has values, add them.
                    values = new ArrayList<>(Arrays.asList(sep[1].split(",")));
                }
                tags.add(new KeyValue(sep[0], values));
            }

            // Move cursor to next segment.
            cursor = nextSegment;
        }

        // Parse source.
        if (rawString.charAt(cursor) == 58) {
            nextSegment = rawString.indexOf(" ", cursor) + 1; // Update next segment to the segment after current.
            cursor++; // Move cursor after the colon.
            source = rawString.substring(cursor, nextSegment - 1);
            cursor = nextSegment; // Move cursor to next segment.
        }

        // Parse command.
        nextSegment = rawString.indexOf(" ", cursor) + 1;
        if (nextSegment == 0) {
            // No more segments, only the command left.
            command = rawString.substring(cursor);
        } else {
            command = rawString.substring(cursor, nextSegment - 1);
            cursor = nextSegment;

            // Parse parameters
            String rawParams = rawString.substring(cursor);
            int escapedParamIndex = rawParams.indexOf(" :");
            // TODO: Handle empty params?
            if (escapedParamIndex != -1) {
                // We have an escaped param at the end.
                params.addAll(Arrays.asList(rawParams.substring(0, escapedParamIndex).split(" ")));
                params.add(rawParams.substring(escapedParamIndex + 2)); // Add escaped param.
            } else {
                params.addAll(Arrays.asList(rawParams.split(" ")));
            }
        }

        // Build message and return to caller.
        return new Builder(command).parameters(params).tags(tags).source(source).build();
    }

    public ArrayList<KeyValue> getTags() {
        return this.tags;
    }

    public String getSource() {
        return this.source;
    }

    public String getCommand() {
        return this.command;
    }

    public ArrayList<String> getParameters() {
        return this.parameters;
    }

    @Override
    // Note: Does not include the EOL (\r\n)
    public String toString() { // TODO: Move this to another helper method? IDK
        String rawTags = "";
        String rawSource = "";
        String rawParams = "";

        // Add the tags to the message.
        for (int i = 0; i < this.tags.size(); i++) {
            String tag = String.valueOf(this.tags.get(i));
            if (i != this.tags.size() - 1) {
                if (i == 0) {
                    tag = "@" + tag.concat(";"); // First tag should prepend the tag identifier.
                } else {
                    tag = tag.concat(";");
                }
            }

            rawTags = rawTags.concat(tag) + " ";
        }

        // Add the source to the message.
        if (!this.source.isEmpty()) {
            rawSource = ":" + this.source + " "; // Add the prepended identifier (colon).
        }

        // Add the parameters to message.
        for (int i = 0; i < this.parameters.size(); i++) {
            String parameter = this.parameters.get(i);

            if (i != this.parameters.size() - 1) {
                if (parameter.contains(" ")) {
                    // This is NOT allowed, only the last parameter can have a space.
                    // TODO: Throw an error or something.
                    return null;
                }
            } else {
                if (parameter.contains(" ") || parameter.startsWith(":")) {
                    // Last parameter can have spaces or start with a colon, so long as it is prepended with a colon.
                    parameter = ":" + parameter;
                }
            }

            // Add parameter to the message.
            rawParams = rawParams.concat(" " + parameter);
        }

        // Construct string in correct order and return it!
        return rawTags + rawSource + this.command + rawParams;
    }

    public static class Builder {
        private final ArrayList<KeyValue> tags;
        private final ArrayList<String> parameters;
        private String source;
        private String command;

        public Builder(String command) {
            this.command = command;
            this.source = "";
            this.tags = new ArrayList<>();
            this.parameters = new ArrayList<>();
        }

        /**
         * Distinct from a normal message, a numeric reply MUST contain a <source> and use a three-digit numeric as the command. A numeric reply SHOULD contain the target of the reply as the first parameter of the message.
         */
        public Builder(ServerReplyCode replyCode, String source, String target) {
            this.command = replyCode.toString();
            this.source = source;
            this.tags = new ArrayList<>();
            this.parameters = new ArrayList<>();
            if (!target.isEmpty()) {
                this.parameters.add(target);
            }
        }

        public Builder() {
            this.command = "";
            this.source = "";
            this.tags = new ArrayList<>();
            this.parameters = new ArrayList<>();
        }

        public Builder command(String command) {
            this.command = command;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder tag(KeyValue tag) {
            this.tags.add(tag);
            return this;
        }

        public Builder tags(ArrayList<KeyValue> tags) {
            this.tags.addAll(tags);
            return this;
        }

        public Builder parameter(String parameter) {
            this.parameters.add(parameter);
            return this;
        }

        public Builder parameters(ArrayList<String> parameters) {
            this.parameters.addAll(parameters);
            return this;
        }

        public Message build() throws IllegalStateException {
            validate();
            return new Message(this);
        }

        private void validate() throws IllegalStateException {
            String errorMsg = ""; // TODO: More elegant error message format.
            for (int i = 0; i < this.parameters.size(); i++) {
                String parameter = this.parameters.get(i);
                if (parameter.contains(" ") && !parameter.startsWith(":")) {
                    // Parameter contains a space but is not prepended with a colon character, this is not allowed.
                    errorMsg = errorMsg.concat("Parameter " + i + " contains a space but is not prepended with a colon character. ");
                }
            }
            // TODO: Make sure there is a command.
        }

        public enum ServerReplyCode {
            RPL_WELCOME(1),
            RPL_YOURHOST(2),
            RPL_CREATED(3),
            RPL_MYINFO(4),
            RPL_ISUPPORT(5),
            RPL_BOUNCE(10),
            RPL_UMODEIS(221),
            RPL_LUSERCLIENT(251),
            RPL_LUSEROP(252),
            RPL_LUSERUNKNOWN(253),
            RPL_LUSERCHANNELS(254),
            RPL_LUSERME(255),
            RPL_ADMINME(256),
            RPL_ADMINLOC1(257),
            RPL_ADMINLOC2(258),
            RPL_ADMINEMAIL(259),
            RPL_TRYAGAIN(263),
            RPL_LOCALUSERS(265),
            RPL_GLOBALUSERS(266),
            RPL_WHOISCERTFP(276),
            RPL_NONE(300),
            RPL_AWAY(301),
            RPL_USERHOST(302),
            RPL_UNAWAY(305),
            RPL_NOWAWAY(306),
            RPL_WHOREPLY(352),
            RPL_ENDOFWHO(315),
            RPL_WHOISREGNICK(307),
            RPL_WHOISUSER(311),
            RPL_WHOISSERVER(312),
            RPL_WHOISOPERATOR(313),
            RPL_WHOWASUSER(314),
            RPL_WHOISIDLE(317),
            RPL_ENDOFWHOIS(318),
            RPL_WHOISCHANNELS(319),
            RPL_WHOISSPECIAL(320),
            RPL_LISTSTART(321),
            RPL_LIST(322),
            RPL_LISTEND(323),
            RPL_CHANNELMODEIS(324),
            RPL_CREATIONTIME(329),
            RPL_WHOISACCOUNT(330),
            RPL_NOTOPIC(331),
            RPL_TOPIC(332),
            RPL_TOPICWHOTIME(333),
            RPL_WHOISACTUALLY(338),
            RPL_INVITING(341),
            RPL_INVITELIST(346),
            RPL_ENDOFINVITELIST(347),
            RPL_EXCEPTLIST(348),
            RPL_ENDOFEXCEPTLIST(349),
            RPL_VERSION(351),
            RPL_NAMREPLY(353),
            RPL_ENDOFNAMES(366),
            RPL_BANLIST(367),
            RPL_ENDOFBANLIST(368),
            RPL_ENDOFWHOWAS(369),
            RPL_INFO(371),
            RPL_ENDOFINFO(374),
            RPL_MOTDSTART(375),
            RPL_MOTD(372),
            RPL_ENDOFMOTD(376),
            RPL_WHOISHOST(378),
            RPL_WHOISMODES(379),
            RPL_YOUREOPER(381),
            RPL_REHASHING(382),
            RPL_TIME(391),
            RPL_STARTTLS(670),
            RPL_WHOISSECURE(671),
            RPL_HELPSTART(704),
            RPL_HELPTXT(705),
            RPL_ENDOFHELP(706),
            RPL_LOGGEDIN(900),
            RPL_LOGGEDOUT(901),
            RPL_SASLSUCCESS(903),
            RPL_SASLMECHS(908),
            ERR_UNKNOWNERROR(400),
            ERR_NOSUCHNICK(401),
            ERR_NOSUCHSERVER(402),
            ERR_NOSUCHCHANNEL(403),
            ERR_CANNOTSENDTOCHAN(404),
            ERR_TOOMANYCHANNELS(405),
            ERR_WASNOSUCHNICK(406),
            ERR_NOORIGIN(409),
            ERR_INPUTTOOLONG(417),
            ERR_UNKNOWNCOMMAND(421),
            ERR_NOMOTD(422),
            ERR_ERRONEUSNICKNAME(432),
            ERR_NICKNAMEINUSE(433),
            ERR_USERNOTINCHANNEL(441),
            ERR_NOTONCHANNEL(442),
            ERR_USERONCHANNEL(443),
            ERR_NOTREGISTERED(451),
            ERR_NEEDMOREPARAMS(461),
            ERR_ALREADYREGISTERED(462),
            ERR_PASSWDMISMATCH(464),
            ERR_YOUREBANNEDCREEP(465),
            ERR_CHANNELISFULL(471),
            ERR_UNKNOWNMODE(472),
            ERR_INVITEONLYCHAN(473),
            ERR_BANNEDFROMCHAN(474),
            ERR_BADCHANNELKEY(475),
            ERR_BADCHANMASK(476),
            ERR_NOPRIVILEGES(481),
            ERR_CHANOPRIVSNEEDED(482),
            ERR_CANTKILLSERVER(483),
            ERR_NOOPERHOST(491),
            ERR_UMODEUNKNOWNFLAG(501),
            ERR_USERSDONTMATCH(502),
            ERR_HELPNOTFOUND(524),
            ERR_INVALIDKEY(525),
            ERR_STARTTLS(691),
            ERR_INVALIDMODEPARAM(696),
            ERR_NOPRIVS(723),
            ERR_NICKLOCKED(902),
            ERR_SASLFAIL(904),
            ERR_SASLTOOLONG(905),
            ERR_SASLABORTED(906),
            ERR_SASLALREADY(907);

            private final int code;

            ServerReplyCode(int code) {
                this.code = code;
            }

            @Override
            public String toString() {
                return String.valueOf(this.code);
            }
        }
    }
}
