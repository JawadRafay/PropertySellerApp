package com.hypenet.realestaterehman.model;

import java.util.List;

public class ChatBotCompletion {

    private String model;
    private float temperature;
    private List<Chat> messages;

    public ChatBotCompletion() {
        this.model = "gpt-3.5-turbo-0301";
        this.temperature = 0.9f;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public List<Chat> getMessages() {
        return messages;
    }

    public void setMessages(List<Chat> messages) {
        this.messages = messages;
    }
}
