package ru.VaolEr.networkclientserver;

public enum CommandType {
    AUTH,
    AUTH_OK,
    AUTH_ERROR,
    UPDATE_USERS_LIST,
    SERVER_MESSAGE,
    PUBLIC_MESSAGE,
    PRIVATE_MESSAGE,
    ERROR,
    END
}
