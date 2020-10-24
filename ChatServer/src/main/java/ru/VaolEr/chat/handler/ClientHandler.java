package ru.VaolEr.chat.handler;

import com.jcabi.aspects.Timeable;
import ru.VaolEr.chat.ChatServer;
import ru.VaolEr.chat.util.DateUtil;
import ru.VaolEr.networkclientserver.Command;
import ru.VaolEr.networkclientserver.CommandType;
import ru.VaolEr.networkclientserver.commands.AuthenticationCommandData;
import ru.VaolEr.networkclientserver.commands.ChangeNicknameCommandData;
import ru.VaolEr.networkclientserver.commands.PrivateMessageCommandData;
import ru.VaolEr.networkclientserver.commands.PublicMessageCommandData;

import java.io.*;
import java.net.Socket;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ClientHandler {

    private final ChatServer myServer;
    private final Socket clientSocket;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    private String userName = null;
    private boolean authSuccessful = false;

    public ClientHandler(ChatServer server, Socket socket) {
        this.myServer = server;
        this.clientSocket = socket;
    }

    public void handle() throws IOException {
        in = new ObjectInputStream(clientSocket.getInputStream());
        out = new ObjectOutputStream(clientSocket.getOutputStream());

         new Thread(() -> {
            try {
                authentication();
                authSuccessful = true;
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

    public boolean isAuthSuccessful() {
        return authSuccessful;
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
                    myServer.broadcastMessage(this, Command.messageInfoCommand(messageBody, messageSender));
                }
                case PRIVATE_MESSAGE -> {
                    PrivateMessageCommandData data = (PrivateMessageCommandData) command.getCommandData();
                    String recipient = data.getReceiver();
                    String messageBody = data.getMessage();
                    myServer.sendPrivateMessage(recipient, Command.messageInfoCommand(messageBody, userName));
                }
                case CHANGE_NICKNAME -> {
                    ChangeNicknameCommandData data = (ChangeNicknameCommandData) command.getCommandData();
                    String user = data.getSender();
                    String newNickname = data.getNickname();
                    myServer.updateNickname(user, newNickname);
                    myServer.sendPrivateMessage(user, Command.messageInfoCommand("Nickname will be changed after reconnect", "Server"));
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
        boolean isAuthSuccess = false;
        while (true) {
            Command command = readCommand();            //blocking method
            System.out.println("In authentication...");
            if(command == null){
                continue;
            }
            if(command.getCommandType() == CommandType.AUTH){
                isAuthSuccess =  processAuthenticationCommand(command);
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

    public void closeConnection() throws IOException {
        if(authSuccessful){
            myServer.unsubscribe(this);
            String message = DateUtil.getCurrentLocalTime() + " " + userName + " left the chat!";
            myServer.broadcastMessage(this, Command.messageInfoCommand(message, null));
        }
        else{
            sendMessage(Command.errorCommand("Authentication session timed out!"));
        }
        clientSocket.close();
    }


    public void sendMessage(Command command) throws IOException {
        out.writeObject(command);
    }
}
