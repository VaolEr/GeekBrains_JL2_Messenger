package ru.VaolEr;


import ru.VaolEr.chat.ChatServer;
import ru.VaolEr.chat.util.DateUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

public class ServerApp {

    private static final int DEFAULT_PORT = 8189;

    private static final Logger logger = Logger.getLogger(ServerApp.class.getName());

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if(args.length != 0){
            port = Integer.parseInt(args[0]);
        }
        try{
            new ChatServer(port).start();
        }
        catch (IOException e){
            //System.err.println(DateUtil.getCurrentLocalTime() + " !! Failed to create ChatServer. !!");
            logger.severe("Failed to create ChatServer!");
            logger.severe(Arrays.toString(e.getStackTrace()));
            //e.printStackTrace();
            System.exit(1);
        }
    }

}
