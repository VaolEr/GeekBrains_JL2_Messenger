package ru.VaolEr.networkclientserver.commands;

import java.io.Serializable;

public class AuthenticationOkCommandData implements Serializable {

    private final String username;

    public AuthenticationOkCommandData(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
