package com.emesare.irjava;

/**
 * Client connected to an instance of Server
 * https://modern.ircdocs.horse/#clients
 */
public class Client {
    /**
     * Example: 50.227.69.228 or my-hostname.com
     */
    private final String hostname;
    /**
     * Must not contain a: space, comma, *, ?, ! or an @
     * Must not start with a: $ or :
     * Must not start with any character listed as a channel type prefix (ex. #).
     */
    private String nickname;
    private String realName;

    /**
     * Provided in the host mask to others across the network (ex. username@hostname)
     */
    private String username;

    // TODO: Add a client TYPE (registered, unregistered, operator, server) : UPDATE this will not be stored here instead be in ServerHandler and ChannelhHandler

    public Client(String nickname, String hostname, String realName, String username) {
        this.nickname = nickname;
        this.hostname = hostname;
        this.realName = realName;
        this.username = username;
    }

    public Client() {
        this.nickname = "";
        this.hostname = "";
        this.realName = "";
        this.username = "";
    }

    // TODO: What other client identifiers do we need to verify?

    public static boolean verifyNickname(String nickname) {
        return !nickname.contains(" ") &&
                !nickname.contains("*") &&
                !nickname.contains(",") &&
                !nickname.contains("?") &&
                !nickname.contains("!") &&
                !nickname.contains("@") &&
                !nickname.startsWith("$") &&
                !nickname.startsWith(":");

        // TODO: Check to make sure it doesn't start with any character listed as a channel type prefix.
    }

    public String getHostmask() {
        // TODO: Is nickname always present?
        return this.nickname + "!" + this.username + "@" + this.hostname;
    }

    public String getNickname() {
        return this.nickname;
    }

    public boolean setNickname(String nickname) {
        if (verifyNickname(nickname)) {
            this.nickname = nickname;
            return true;
        } else {
            return false;
        }
    }

    public String getHostname() {
        return this.hostname;
    }

    public String getRealName() {
        return this.realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        // TODO: This is fine right? Ah whatever.
        return getHostmask();
    }
}
