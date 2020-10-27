package ru.VaolEr.chat.authentication;

public interface AuthenticationService {
    void start();

    String getUsernameByLoginAndPassword(String login, String password);

    void stop();

    void changeNickname(String username, String newUsername);
}
