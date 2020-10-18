package ru.VaolEr.networkclientserver.commands;

import java.io.Serializable;

public class AuthenticationErrorCommandData implements Serializable {

    private final String errorMessage;

    public AuthenticationErrorCommandData(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
