package ru.VaolEr.model;

import javafx.application.Platform;
import ru.VaolEr.controller.Controller;
import ru.VaolEr.repository.util.DateUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {

    private static final String AUTH_CMD_PREFIX = "/auth";
    private static final String AUTHOK_CMD_PREFIX = "/authok";
    private static final String AUTHERR_CMD_PREFIX = "/autherr";

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8189;

    private final String host;
    private final int port;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
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
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            return true;
        } catch (IOException e) {
            System.err.println("Connection was not set!");
            e.printStackTrace();
            return false;
        }
    }

    public String sendAuthCommand(String login, String password) {
        try {
            getOutputStream().writeUTF(String.format("%s %s %s", AUTH_CMD_PREFIX, login, password));
            String response = getInputStream().readUTF();
            if (response.startsWith(AUTHOK_CMD_PREFIX)) {
                this.username = response.split("\\s+", 2)[1];
                return null;
            } else {
                return response.split("\\s+", 2)[1];
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public DataInputStream getInputStream() {
        return inputStream;
    }

    public DataOutputStream getOutputStream() {
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

    public void waitMessages(Controller viewController) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String message = inputStream.readUTF();
                        Platform.runLater(() -> {
                            viewController.appendMessage(DateUtil.getCurrentLocalTime() + " -> " + message);
                        });
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
}
