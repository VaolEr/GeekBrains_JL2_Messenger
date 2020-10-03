package ru.VaolEr.model;

import java.util.Date;
import java.util.Objects;

public class Message {

    private String messageText;
    private Date messageDate;

    public Message(String messageText, Date messageDate) {
        this.messageText = messageText;
        this.messageDate = messageDate;
    }

    public String getMessageText() {
        return messageText;
    }

    public Date getMessageDate() {
        return messageDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(messageText, message.messageText) &&
                Objects.equals(messageDate, message.messageDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageText, messageDate);
    }

}
