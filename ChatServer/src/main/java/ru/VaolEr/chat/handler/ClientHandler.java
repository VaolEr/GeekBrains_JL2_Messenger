package ru.VaolEr.chat.handler;

import ru.VaolEr.chat.ChatServer;
import ru.VaolEr.chat.util.DateUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import java.time.LocalDate;

public class ClientHandler {

    public static final String AUTH_CMD_PREFIX = "/auth";
    public static final String AUTHERROR_CMD_PREFIX = "/autherror";
    public static final String AUTHOK_CMD_PREFIX = "/authok";

    private final ChatServer myServer;
    private final Socket clientSocket;

    private DataInputStream in;
    private DataOutputStream out;

    private String userName = null;

    public ClientHandler(ChatServer server, Socket socket) {
        this.myServer = server;
        this.clientSocket = socket;
    }

    public void handle() throws IOException {
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());

        new Thread(() -> {
            try {
                authentication();
                readMessage();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    closeConnection();
                } catch (IOException e) {
                    System.err.println("Failed to close client socket.");
                }
            }
        }).start();

    }

    public String getUserName() {
        return userName;
    }

    private void readMessage() throws IOException {
        while (true){
            String message = in.readUTF();
            System.out.println(DateUtil.getCurrentLocalTime() + " message: " + message);
            if(message.startsWith("/end")){
                return; //maybe break here
            }
            if(message.startsWith("/privat")){
                myServer.broadcastMessage("/privat " + this.userName + ": " + message, this);
            }else{
                myServer.broadcastMessage(this.userName + ": " + message, this);
            }

        }
    }

    private void authentication() throws IOException {
        while (true) {
            System.out.println("In authentication...");
            String message = in.readUTF();
            String login = "";
            String password = "";
            if(message.startsWith(AUTH_CMD_PREFIX)){
                String[] parts = message.split("\\s+",3);
                login = parts[1];
                password = parts[2];
                this.userName = myServer.getAuthService().getUsernameByLoginAndPassword(login,password);
                System.out.println(this.userName + " authentication...");
                if(myServer.isUserNameAlreadyBusy(this.userName)){
                    out.writeUTF(AUTHERROR_CMD_PREFIX + " Login and password are already used.");
                    System.out.println(this.userName + " Login and password are already used.");
                }
                else if(this.userName != null && !myServer.isUserNameAlreadyBusy(this.userName)){
                    //out.writeUTF(AUTHOK_CMD_PREFIX + this.userName + " Login and password are correct!");
                    out.writeUTF(AUTHOK_CMD_PREFIX +" "+ this.userName);
                    System.out.println(this.userName + " authentication successful!");
                    myServer.broadcastMessage(this.userName + " joined to chat!", this);
                    myServer.subscribe(this);
                    break;
                } else {
                    out.writeUTF(AUTHERROR_CMD_PREFIX + " Invalid login or password.");
                    System.out.println(this.userName + " Invalid login or password.");
                }
            } else{
                out.writeUTF(AUTHERROR_CMD_PREFIX + " /auth command is required");
                System.out.println(this.userName + " /auth command is required");
            }
        }
    }

    private void closeConnection() throws IOException {
        myServer.unsubscribe(this);
        myServer.broadcastMessage(DateUtil.getCurrentLocalTime() + " " + userName + " left the chat!", this);
        clientSocket.close();
    }


    public void sendMessage(String message) throws IOException {
        out.writeUTF(message);
    }
}
