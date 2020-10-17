package ru.VaolEr;

import ru.VaolEr.chat.ChatServer;
import ru.VaolEr.chat.util.DateUtil;

import java.io.IOException;

public class ServerApp {

    private static final int DEFAULT_PORT = 8189;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if(args.length != 0){
            port = Integer.parseInt(args[0]);
        }
        try{
            new ChatServer(port).start();
        }
        catch (IOException e){
            System.err.println(DateUtil.getCurrentLocalTime() + " !! Failed to create ChatServer. !!");
            e.printStackTrace();
            System.exit(1);
        }
    }

}
