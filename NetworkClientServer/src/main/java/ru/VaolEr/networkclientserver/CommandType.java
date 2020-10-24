package ru.VaolEr.networkclientserver;

public enum CommandType {
    AUTH,
    AUTH_OK,
    AUTH_ERROR,
    CHANGE_NICKNAME,
    ERROR,
    END,
    PUBLIC_MESSAGE,
    PRIVATE_MESSAGE,
    SERVER_MESSAGE,
    UPDATE_USERS_LIST
}
