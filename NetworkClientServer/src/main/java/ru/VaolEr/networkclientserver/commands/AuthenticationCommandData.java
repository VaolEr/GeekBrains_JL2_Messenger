package ru.VaolEr.networkclientserver.commands;

import java.io.Serializable;

public class AuthenticationCommandData implements Serializable {

    private final String login;
    private final String password;

    public AuthenticationCommandData(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
