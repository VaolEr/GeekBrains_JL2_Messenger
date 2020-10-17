package ru.VaolEr.chat;

import ru.VaolEr.chat.authentication.AuthenticationService;
import ru.VaolEr.chat.authentication.BaseAuthenticationService;
import ru.VaolEr.chat.handler.ClientHandler;
import ru.VaolEr.chat.util.DateUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {


    private final ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();
    private final AuthenticationService authService;

    public ChatServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.authService = new BaseAuthenticationService();
    }
    public void start() throws IOException {
        System.out.println(DateUtil.getCurrentLocalTime() + " --- Server started ---");

        authService.start();
        try {
            while (true){
                waitAndProcessNewClient();
            }
        } catch (IOException e) {
            System.err.println(DateUtil.getCurrentLocalTime() + " --- Failed to accept new connection. ---");
            e.printStackTrace();
        } finally {
            authService.stop();
            serverSocket.close();
        }

    }

    private void waitAndProcessNewClient() throws IOException {
        System.out.println(DateUtil.getCurrentLocalTime() + " --- Waiting for new connection ---");
        Socket clientSocket = serverSocket.accept();
        System.out.println(DateUtil.getCurrentLocalTime() + " --- Client connected! ---");
        processClientConnection(clientSocket);
    }

    private void processClientConnection(Socket clientSocket) throws IOException {
        ClientHandler clientHandler = new ClientHandler(this, clientSocket);
        //clients.add(clientHandler);
        clientHandler.handle();
    }

    public AuthenticationService getAuthService(){
        return authService;
    }

    public void broadcastMessage(String message, ClientHandler sender) throws IOException {
        String user = "";
        String privateMessage = "";
        System.out.println("/******/ " + message);
        if(message.startsWith("/privat")){
            System.out.println("PRIVAT");
            String[] parts = message.split("\\s+",5);
            user = parts[3];
            privateMessage = "PRIVAT" + " " + parts[1] + " " + parts[4];
            for (ClientHandler client : clients) {
                if(client.getUserName().equals(user)){
                    client.sendMessage(privateMessage);
                }
            }
        } else {
            for (ClientHandler client : clients) {
                if (client == sender) {
                    continue;
                }
                client.sendMessage(message);
            }
        }
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    public boolean isUserNameAlreadyBusy(String userName) {
        for (ClientHandler client : clients) {
            if(client.getUserName().equals(userName)) {
                return true;
            }
        }
        return false;
    }
}
