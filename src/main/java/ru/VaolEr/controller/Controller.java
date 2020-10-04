package ru.VaolEr.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.sun.tools.javac.Main;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import ru.VaolEr.MainApp;
import ru.VaolEr.model.Message;
import ru.VaolEr.model.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Controller {

    @FXML
    JFXButton buttonSearchUser;
    @FXML
    JFXTextField textFieldSearchUser;

    @FXML
    TableView<User> usersTable;
    @FXML
    TableColumn<User, String> usersImagesColumn;
    @FXML
    TableColumn<User, String> usersColumn;
    @FXML
    Label labelUserNickname;

    @FXML
    JFXButton buttonSendMessage;
    @FXML
    JFXTextField textFieldNewMessage;
    @FXML
    TextArea textAreaMessenger;
    private MainApp mainApp;


    @FXML
    public void initialize(){
        usersTable.setRowFactory(param -> {
            TableRow<User> row = new TableRow<>();
            return row;
        });

        usersColumn.setCellValueFactory(cellData -> cellData.getValue().propertyNickname());
        usersImagesColumn.setCellValueFactory(cellData -> cellData.getValue().propertyImage());

        usersTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showUserDetails(newValue));

    }

    private User currentUser;

    private void showUserDetails(User user){
        currentUser = user;
        if(currentUser!= null){
            labelUserNickname.setText(currentUser.getNickname());
            for(int i = 0; i < currentUser.getIncomingMessages().size(); i++) {
                if(i > 0){
                    textAreaMessenger.setText(textAreaMessenger.getText() + "\n" + currentUser.getIncomingMessages().get(i).getMessageText());
                }
                else{
                    textAreaMessenger.setText(String.valueOf(currentUser.getIncomingMessages().get(i).getMessageText()));
                }
            }
        }
    }

    @FXML
    private void buttonSendMessageClick(ActionEvent event) {
        if(currentUser!= null) {
            textAreaMessenger.setText(textAreaMessenger.getText() + "\n" + textFieldNewMessage.getText());
            currentUser.addIncomingMessage(new Message(textFieldNewMessage.getText()));
            textFieldNewMessage.setText("");
        }
    }

    public void  setMainApp(MainApp mainApp){
        this.mainApp = mainApp;
        usersTable.setItems(mainApp.getUserData());
    }
}
