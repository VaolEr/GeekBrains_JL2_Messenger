package ru.VaolEr.networkclientserver.commands;

import java.io.Serializable;

public class MessageInfoCommandData implements Serializable {

    private final String sender;
    private final String message;

    public MessageInfoCommandData(String message, String sender) {
        this.sender = sender;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }
}
