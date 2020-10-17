package ru.VaolEr.chat.authentication;

public interface AuthenticationService {
    void start();

    String getUsernameByLoginAndPassword(String login, String password);

    void stop();
}
