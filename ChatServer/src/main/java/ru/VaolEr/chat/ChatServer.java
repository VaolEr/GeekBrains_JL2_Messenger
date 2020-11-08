package ru.VaolEr.chat;

import ru.VaolEr.ServerApp;
import ru.VaolEr.chat.authentication.AuthenticationService;
import ru.VaolEr.chat.authentication.BaseAuthenticationService;
import ru.VaolEr.chat.authentication.DataBaseAuthService;
import ru.VaolEr.chat.handler.ClientHandler;
import ru.VaolEr.chat.util.DateUtil;
import ru.VaolEr.networkclientserver.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Logger;

public class ChatServer {

    private final ServerSocket serverSocket;

    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());

    private final List<ClientHandler> clients = new ArrayList<>();

    private final AuthenticationService authService;

    private final int authTime = 120_000;

    public ChatServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        //this.authService = new BaseAuthenticationService();
        this.authService = new DataBaseAuthService();
    }

    public void start() throws IOException {
        //System.out.println(DateUtil.getCurrentLocalTime() + " --- Server started ---");
        logger.severe(" --- Server started ---");
        authService.start();
        try {
            while (true){
                waitAndProcessNewClient();
            }
        } catch (IOException e) {
            //System.err.println(DateUtil.getCurrentLocalTime() + " --- Failed to accept new connection. ---");
            logger.severe(" --- Failed to accept new connection. ---");
            logger.severe(Arrays.toString(e.getStackTrace()));
            //e.printStackTrace();
        } finally {
            authService.stop();
            serverSocket.close();
        }

    }

    private void waitAndProcessNewClient() throws IOException {
        //System.out.println(DateUtil.getCurrentLocalTime() + " --- Waiting for new connection ---");
        logger.info(" --- Waiting for new connection ---");
        Socket clientSocket = serverSocket.accept();
        //System.out.println(DateUtil.getCurrentLocalTime() + " --- Client connected! ---");
        logger.info(" --- Client connected! ---");
        processClientConnection(clientSocket);
    }

    private synchronized void processClientConnection(Socket clientSocket) throws IOException {
        ClientHandler clientHandler = new ClientHandler(this, clientSocket);
        clientHandler.handle();
        new java.util.Timer().schedule(
                new TimerTask() {
                    public void run() {
                        if(!clientHandler.isAuthSuccessful()){
                            //System.out.println(DateUtil.getCurrentLocalTime() + " --- Client authentication session timed out! ---");
                            logger.severe(" --- Client authentication session timed out! ---");
                            try {
                                clientHandler.closeConnection();
                            } catch (IOException e) {
                                //e.printStackTrace();
                                logger.severe(Arrays.toString(e.getStackTrace()));
                            }
                        }
                    }
                },
                authTime );
    }

    public AuthenticationService getAuthService(){
        return authService;
    }

    public synchronized void broadcastMessage(ClientHandler sender, Command command) throws IOException {
        for (ClientHandler client: clients){
            if(client == sender){
                continue;
            }
            client.sendMessage(command);
        }
    }

    public synchronized void sendPrivateMessage(String recipient, Command command) throws IOException {
        for (ClientHandler client : clients) {
            if (client.getUserName().equals(recipient)) {
                client.sendMessage(command);
            }
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) throws IOException {
        clients.add(clientHandler);
        List<String> userNames = getAllUserNames();
        broadcastMessage(null, Command.updateUsersListCommand(userNames));
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) throws IOException {
        clients.remove(clientHandler);
        List<String> userNames = getAllUserNames();
        broadcastMessage(null, Command.updateUsersListCommand(userNames));
    }

    private List<String> getAllUserNames() {
        List<String> usernames = new ArrayList<>();
        for(ClientHandler client: clients){
            usernames.add(client.getUserName());
        }
        return usernames;
    }

    public synchronized boolean isUserNameAlreadyBusy(String userName) {
        for (ClientHandler client : clients) {
            if(client.getUserName().equals(userName)) {
                return true;
            }
        }
        return false;
    }

    public void updateNickname(String user, String newNickname) {
        authService.changeNickname(user, newNickname);
    }
}
