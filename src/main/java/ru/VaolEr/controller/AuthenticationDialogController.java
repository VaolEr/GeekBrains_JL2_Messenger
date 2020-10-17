package ru.VaolEr.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.VaolEr.MainApp;
import ru.VaolEr.model.Network;

public class AuthenticationDialogController {
    private @FXML TextField loginField;
    private @FXML PasswordField passwordField;
    private @FXML Button authButton;

    private Network network;
    private MainApp clientApp;

    @FXML
    public void executeAuth(ActionEvent actionEvent) {
        String login = loginField.getText();
        String password = passwordField.getText();
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            MainApp.showNetworkError("Username and password should be not empty!", "Auth error");
            return;
        }

        String authError = network.sendAuthCommand(login, password);
        if (authError == null) {
            clientApp.openChat();
        } else {
            MainApp.showNetworkError(authError, "Auth error");
        }


    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setClientApp(MainApp clientApp) {
        this.clientApp = clientApp;
    }
}
