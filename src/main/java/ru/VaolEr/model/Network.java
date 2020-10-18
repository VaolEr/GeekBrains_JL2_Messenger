package ru.VaolEr.model;

import javafx.application.Platform;
import ru.VaolEr.MainApp;
import ru.VaolEr.controller.Controller;
import ru.VaolEr.networkclientserver.Command;
import ru.VaolEr.networkclientserver.commands.*;
import ru.VaolEr.repository.util.DateUtil;

import java.io.*;
import java.net.Socket;

public class Network {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8189;

    private final String host;
    private final int port;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Socket socket;
    private String username;

    public Network() {
        this(SERVER_ADDRESS, SERVER_PORT);
    }

    public Network(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            return true;
        } catch (IOException e) {
            System.err.println("Connection was not set!");
            e.printStackTrace();
            return false;
        }
    }

    public String sendAuthCommand(String login, String password) {
        try {
            Command authCommand = Command.authenticationCommand(login, password);
            outputStream.writeObject(authCommand);
            Command command = readCommand();
            if(command == null){
                return "Failed to read command from server.";
            }
            switch (command.getCommandType()){
                case AUTH_OK-> {
                    AuthenticationOkCommandData data = (AuthenticationOkCommandData) command.getCommandData();
                    this.username = data.getUsername();
                    return null;
                }
                case AUTH_ERROR -> {
                    AuthenticationErrorCommandData data = (AuthenticationErrorCommandData) command.getCommandData();
                    return  data.getErrorMessage();
                }
                default -> {
                    return "Unknown type of command from server: " + command.getCommandType();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public void sendMessage(String message) throws IOException {
        Command command = Command.publicMessageCommand(this.username, message);
        sendCommand(command);
    }

    public void sendPrivateMessage(String message, String recipient) throws IOException {
        Command command = Command.privateMessageCommand(recipient, message);
        sendCommand(command);
    }

    private void sendCommand(Command command) throws IOException {
        outputStream.writeObject(command);
    }

    public void waitMessages(Controller viewController) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Command command = readCommand();
                        if(command == null){
                            viewController.showError("ServerError","Invalid Command From Server!");
                            continue;
                        }
                        switch (command.getCommandType()){
                            case SERVER_MESSAGE -> {
                                MessageInfoCommandData data = (MessageInfoCommandData) command.getCommandData();
                                String sender = data.getSender();
                                String msgBody = data.getMessage();
                                String formattedMessage = sender != null ? String.format("%s -> %s: %s",DateUtil.getCurrentLocalTime(), sender, msgBody) :String.format("%s -> %s: %s",DateUtil.getCurrentLocalTime(), "Server", msgBody);
                                Platform.runLater(() -> {
                                    viewController.appendMessage(formattedMessage);
                                });
                            }
                            case ERROR -> {
                                ErrorCommandData data = (ErrorCommandData) command.getCommandData();
                                String message = data.getErrorMessage();
                                Platform.runLater(() -> {
                                    viewController.showError("ServerError",message);
                                });
                            }
                            case UPDATE_USERS_LIST -> {
                                UpdateUsersListCommandData data = (UpdateUsersListCommandData) command.getCommandData();
                                Platform.runLater(() -> {
                                    viewController.updateUsers(data.getUsers());
                                });
                            }
                            default -> {
                                Platform.runLater(() -> {
                                    viewController.showError("Unknown command from server.", command.getCommandType().toString());
                                });
                            }

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Connection broken!");
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public ObjectInputStream getInputStream() {
        return inputStream;
    }

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    public void changeNickNameInView(Controller viewController){
        Thread thread = new Thread(() ->{
            Platform.runLater(() -> {
                viewController.setUserNickName(this.username);
            });
        });
        thread.setDaemon(true);
        thread.start();


    }



    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setChatMode() {
        waitMessages(null);
    }

    public String getUsername() {
        return username;
    }

    private Command readCommand() throws IOException{
        try {
            return (Command) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            String errorMessage = "Unknown type of object from client!";
            System.err.println(errorMessage);
            e.printStackTrace();
            return null;
        }
    }
}
