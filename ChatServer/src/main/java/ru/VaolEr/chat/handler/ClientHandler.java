package ru.VaolEr.chat.handler;

import ru.VaolEr.chat.ChatServer;
import ru.VaolEr.chat.util.DateUtil;
import ru.VaolEr.networkclientserver.Command;
import ru.VaolEr.networkclientserver.CommandType;
import ru.VaolEr.networkclientserver.commands.AuthenticationCommandData;
import ru.VaolEr.networkclientserver.commands.PrivateMessageCommandData;
import ru.VaolEr.networkclientserver.commands.PublicMessageCommandData;

import java.io.*;
import java.net.Socket;

import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {

    private final ChatServer myServer;
    private final Socket clientSocket;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    private String userName = null;

    public ClientHandler(ChatServer server, Socket socket) {
        this.myServer = server;
        this.clientSocket = socket;
    }

    public void handle() throws IOException {
        in = new ObjectInputStream(clientSocket.getInputStream());
        out = new ObjectOutputStream(clientSocket.getOutputStream());

        new Thread(() -> {
            try {
                Timer authenticationTimer = new Timer();
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
            Command command = readCommand();
            if(command == null){
                continue;
            }
            switch (command.getCommandType()){
                case END -> {
                    return;
                }
                case PUBLIC_MESSAGE -> {
                    PublicMessageCommandData data = (PublicMessageCommandData) command.getCommandData();
                    String messageSender = data.getSender();
                    String messageBody = data.getMessage();
                    myServer.broadcastMessage(this, Command.messageInfoCommand(messageSender, messageBody));
                }
                case PRIVATE_MESSAGE -> {
                    PrivateMessageCommandData data = (PrivateMessageCommandData) command.getCommandData();
                    String recipient = data.getReceiver();
                    String messageBody = data.getMessage();
                    myServer.sendPrivateMessage(recipient, Command.messageInfoCommand(userName, messageBody));
                }
                default -> {
                    System.err.println("Unknown type of command " + command.getCommandType());
                }
            }
        }
    }

    private Command readCommand() throws IOException{
        try {
            return (Command) in.readObject();
        } catch (ClassNotFoundException e) {
            String errorMessage = "Unknown type of object from client!";
            System.err.println(errorMessage);
            sendMessage(Command.errorCommand(errorMessage));
            e.printStackTrace();
            return null;
        }
    }

    private void authentication() throws IOException {

        while (true) {
            Command command = readCommand();
            System.out.println("In authentication...");
            if(command == null){
                continue;
            }
            if(command.getCommandType() == CommandType.AUTH){
                boolean isAuthSuccess =  processAuthenticationCommand(command);
                if(isAuthSuccess) {
                    break;
                }
            } else{
                sendMessage(Command.authenticationErrorCommand("/auth command is required"));
                System.out.println(this.userName + "Authentication command is required");
            }
        }
    }

    private boolean processAuthenticationCommand(Command command) throws IOException {
        AuthenticationCommandData data = (AuthenticationCommandData) command.getCommandData();
        String login = data.getLogin();
        String password = data.getPassword();
        this.userName = myServer.getAuthService().getUsernameByLoginAndPassword(login,password);
        System.out.println(this.userName + " authentication...");
        if(myServer.isUserNameAlreadyBusy(this.userName)){
            sendMessage(Command.authenticationErrorCommand("Login and password are already used."));
            System.out.println(this.userName + " Login and password are already used.");
            return false;
        }
        else if(this.userName != null && !myServer.isUserNameAlreadyBusy(this.userName)){
            sendMessage(Command.authenticationOkCommand(this.userName));
            System.out.println(this.userName + " authentication successful!");
            String message = this.userName + " joined to chat!";
            myServer.broadcastMessage(this, Command.messageInfoCommand(message, null));
            myServer.subscribe(this);
            return true;
        } else {
            sendMessage(Command.authenticationErrorCommand("Invalid login or password."));
            System.out.println(this.userName + " Invalid login or password.");
            return false;
        }
    }

    private void closeConnection() throws IOException {
        myServer.unsubscribe(this);
        String message = DateUtil.getCurrentLocalTime() + " " + userName + " left the chat!";
        myServer.broadcastMessage(this, Command.messageInfoCommand(message, null));
        clientSocket.close();
    }


    public void sendMessage(Command command) throws IOException {
        out.writeObject(command);
    }
}
