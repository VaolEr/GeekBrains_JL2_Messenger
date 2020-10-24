package ru.VaolEr.networkclientserver.commands;

import java.io.Serializable;

public class ChangeNicknameCommandData implements Serializable {

    private final String sender;
    private final String nickname;

    public ChangeNicknameCommandData(String sender, String nickname) {
        this.sender = sender;
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public String getSender() {
        return sender;
    }
}
