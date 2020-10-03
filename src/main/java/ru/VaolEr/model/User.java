package ru.VaolEr.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;

public class User {

    private final StringProperty nickname;
    private final StringProperty image;

    private ArrayList<Message>  incomingMessages = new ArrayList<>();

    public User() {
        this(null,null);
    }

    public User(String nickname, String image) {
        this.nickname = new SimpleStringProperty(nickname);
        this.image  = new SimpleStringProperty(image);
    }

    public String getNickname(){
        return nickname.get();
    }

    public String getImage(){
        return image.get();
    }

    public StringProperty propertyNickname() {
        return nickname;
    }

    public StringProperty propertyImage() {
        return image;
    }

    public ArrayList<Message> getIncomingMessages() {
        return incomingMessages;
    }

    public void addIncomingMessage(Message message){
        incomingMessages.add(message);
    }
}
