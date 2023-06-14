package com.emesare.irjava;

public class Channel {
    private final String name;
    private String topic;

    private String key;

    // TODO: Maybe make this an array of golang like channels (server will update the channels)?

    // TODO: USER MODES? WHERE DO WE STORE USER SPECIFIC SHIT?

    public Channel(String name) {
        this.name = name;
        this.topic = "";
        this.key = "";
    }

    public Channel(String name, String topic, String key) {
        this.name = name;
        this.topic = topic;
        this.key = key;
    }

    public String getName() {
        return this.name;
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
