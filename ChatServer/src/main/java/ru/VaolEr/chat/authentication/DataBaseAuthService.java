package ru.VaolEr.chat.authentication;

import ru.VaolEr.ServerApp;
import ru.VaolEr.chat.User;
import ru.VaolEr.chat.util.DateUtil;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class DataBaseAuthService implements AuthenticationService {

    private static final Logger logger = Logger.getLogger(DataBaseAuthService.class.getName());

    private Connection connection;
    private Statement statement;
    private PreparedStatement pStmtInsert;
    private PreparedStatement pStmtGetUsernameByLoginAndPassword;
    private PreparedStatement pStmtUpdateNickname;
    private PreparedStatement pStmtDelete;

    @Override
    public void start() {
        //System.out.println(DateUtil.getCurrentLocalTime() + " ~~~ Authentication has been started... ~~~");
        logger.info(" ~~~ Authentication has been started... ~~~");
        try {
            clientsDbConnect();
            //System.out.println(DateUtil.getCurrentLocalTime() + " ~~~ Connect to clients.db successful! ~~~");
            logger.info(" ~~~ Connect to clients.db successful! ~~~");
            prepareAllStatements();
            //exGetAllUsers();
        } catch (ClassNotFoundException | SQLException e) {
            //System.err.println(DateUtil.getCurrentLocalTime() + " ~~~ Failed connection to clients.db. ~~~");
            logger.severe(" ~~~ Failed connection to clients.db. ~~~");
            //e.printStackTrace();
            logger.severe(Arrays.toString(e.getStackTrace()));
        }
    }


    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {

        try {
            pStmtGetUsernameByLoginAndPassword.setString(1, login);
            pStmtGetUsernameByLoginAndPassword.setString(2, password);
            ResultSet rs = pStmtGetUsernameByLoginAndPassword.executeQuery();

            String username = rs.getString("username");
            //System.out.println(username);
            return username;

        } catch (Exception e){
            //e.printStackTrace();
            logger.severe(Arrays.toString(e.getStackTrace()));
        }
        return null; // bad solve to return null
    }

    @Override
    public void stop() {
        //System.out.println(DateUtil.getCurrentLocalTime() + " ~ Authentication has been finished. ~");
        logger.info(" ~ Authentication has been finished. ~");
        clientsDbDisconnect();
    }

    @Override
    public void changeNickname(String username, String newUsername) {
        try {
            //System.out.println(username + " -> " + newUsername);
            logger.info(username + " -> " + newUsername);
            pStmtUpdateNickname.setString(1, newUsername);
            pStmtUpdateNickname.setString(2, username);
            pStmtUpdateNickname.executeUpdate();
        }catch (Exception e){
            //e.printStackTrace();
            logger.severe(Arrays.toString(e.getStackTrace()));
        }
    }

    public void clientsDbConnect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:ChatServer/clients.db");
        statement = connection.createStatement();
    }
    public void clientsDbDisconnect() {
        try {
            statement.close();
        } catch (SQLException e) {
            //e.printStackTrace();
            logger.severe(Arrays.toString(e.getStackTrace()));
        }
        try {
            connection.close();
        } catch (SQLException e) {
            //e.printStackTrace();
            logger.severe(Arrays.toString(e.getStackTrace()));
        }
    }

    public void exInsert() throws SQLException {
        //statement.executeUpdate("");
    }

    public void exGetAllUsers() throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT login, password, username, status FROM clients;");
        while(rs.next()){
            //System.out.println(rs.getString("login") + " " + rs.getString("password") + " " + rs.getString("username") + " " + rs.getBoolean("status"));
            logger.info(rs.getString("login") + " " + rs.getString("password") + " " + rs.getString("username") + " " + rs.getBoolean("status"));
        }
    }

    public void prepareAllStatements() throws SQLException {
        pStmtInsert = connection.prepareStatement("INSERT INTO clients(login, password, username) VALUES(?,?,?);");
        pStmtGetUsernameByLoginAndPassword = connection.prepareStatement("SELECT username FROM clients WHERE login == ? AND password == ?");
        pStmtUpdateNickname = connection.prepareStatement("UPDATE clients SET username = ? WHERE username = ?");
    }

}
