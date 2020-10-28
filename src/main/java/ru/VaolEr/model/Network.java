package ru.VaolEr.model;

import javafx.application.Platform;
import ru.VaolEr.MainApp;
import ru.VaolEr.controller.Controller;
import ru.VaolEr.networkclientserver.Command;
import ru.VaolEr.networkclientserver.commands.*;
import ru.VaolEr.repository.util.DateUtil;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Network {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8189;

    private final String host;
    private final int port;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Socket socket;
    private String username;

    private String messagesHistoryFilePath;
    private BufferedWriter bufferedFileWriter;
    private boolean isMessageHistoryFileExist = false;

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
            try {
                bufferedFileWriter.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
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
                    messagesHistoryFilePath = "history_" + this.username +".txt";
                    File file = new File(messagesHistoryFilePath);
                    isMessageHistoryFileExist = file.exists();
                    bufferedFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(messagesHistoryFilePath, true), StandardCharsets.UTF_8));
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

    public void changeNicknameMessage(String sender, String newNickname)  throws IOException{
        Command command  = Command.changeNicknameCommand(sender, newNickname);
        sendCommand(command);
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
                                    viewController.showError("ServerError", message);
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

    public void appendMessageToFile(String message){
        String text = message + "\n";
        try {
            bufferedFileWriter.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void readLast100MessageFromHistory(Controller viewController){
        ArrayList<String> allMessages = new ArrayList<>();
        ArrayList<String> last100Messages = new ArrayList<>();
        try {
            if (isMessageHistoryFileExist){
                BufferedReader reader = new BufferedReader(new FileReader(messagesHistoryFilePath,StandardCharsets.UTF_8));
                String line = reader.readLine();
                allMessages.add(line);
                while (line != null) {
                    allMessages.add(line);
                    line = reader.readLine();
                }
                reader.close();

                int countOfMessages = allMessages.size();
                System.out.println("count of messages " +countOfMessages);
                if(countOfMessages <= 100){
                    for (int i = 0; i < countOfMessages; i++) {
                        String message = allMessages.get(i);
                        last100Messages.add(message);
                    }
                }else {
                    for (int i = 0; i < 100; i++) {
                        String message = allMessages.get(countOfMessages - 100 + i);
                        last100Messages.add(message);
                    }
                }
            }
            else{
                last100Messages.add("");
            }
            viewController.loadLast100Messages(last100Messages);
        }catch (IOException e){
            last100Messages.add("IO file error");
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            bufferedFileWriter.close();
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
