package ru.VaolEr.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.sun.tools.javac.Main;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import ru.VaolEr.MainApp;
import ru.VaolEr.model.Message;
import ru.VaolEr.model.Network;
import ru.VaolEr.model.User;
import ru.VaolEr.repository.util.DateUtil;
import ru.VaolEr.util.FileReadWriteUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Controller {

    @FXML
    JFXButton buttonSearchUser;
    @FXML
    JFXTextField textFieldSearchUser;

//    @FXML
//    TableView<User> usersTable;
//    @FXML
//    TableColumn<User, String> usersImagesColumn;
//    @FXML
//    TableColumn<User, String> usersColumn;

    @FXML
    TableView<String> usersTable;
    @FXML
    TableColumn<String, String> usersImagesColumn;
    @FXML
    TableColumn<String, String> usersColumn;


    @FXML
    ListView<String> usersList;

    @FXML
    Label labelUserNickname;

    @FXML
    JFXButton buttonSendMessage;
    @FXML
    JFXTextField textFieldNewMessage;
    @FXML
    TextArea textAreaMessenger;
    private MainApp mainApp;
    private Network network;

    private String selectedRecipient;

    @FXML
    public void initialize(){
//        usersTable.setRowFactory(param -> {
//            TableRow<String> row = new TableRow<>();
//            return row;
//        });

 //       usersTable.setItems(FXCollections.observableArrayList(NetworkChatClient.USERS_TEST_DATA));

//        usersColumn.setCellValueFactory(cellData -> cellData.getValue().propertyNickname());
//        usersImagesColumn.setCellValueFactory(cellData -> cellData.getValue().propertyImage());
//
//        usersTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showUserDetails(newValue));
        usersList.setCellFactory(lv -> {
            MultipleSelectionModel<String> selectionModel = usersList.getSelectionModel();
            ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                usersList.requestFocus();
                if (! cell.isEmpty()) {
                    int index = cell.getIndex();
                    if (selectionModel.getSelectedIndices().contains(index)) {
                        selectionModel.clearSelection(index);
                        selectedRecipient = null;
                    } else {
                        selectionModel.select(index);
                        selectedRecipient = cell.getItem();
                    }
                    event.consume();
                }
            });
            return cell ;
        });

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
//        if(currentUser!= null) {
//            textAreaMessenger.setText(textAreaMessenger.getText() + "\n" + textFieldNewMessage.getText());
//            currentUser.addIncomingMessage(new Message(textFieldNewMessage.getText()));
//            textFieldNewMessage.setText("");
//        }
        sendMessage();
    }

    @FXML
    public void onEnter(ActionEvent event){
        sendMessage();
    }

    public void  setMainApp(MainApp mainApp){
        this.mainApp = mainApp;
        //usersTable.setItems(mainApp.getUserData());
    }

    public void appendMessage(String message) {
        textAreaMessenger.appendText(message);
        textAreaMessenger.appendText(System.lineSeparator());
//        String username = this.labelUserNickname.getText();
//        String filePath = "history_" + username + ".txt";
//        FileReadWriteUtil.addMessageToLocalHistoryFile(filePath, username, message);
        network.appendMessageToFile(message);
    }

    public void setUserNickName(String nickname){
        labelUserNickname.setText(nickname);
    }

    private void sendMessage() {
        String message = textFieldNewMessage.getText();
        appendMessage(DateUtil.getCurrentLocalTime() + " <- Me: " +  message);
        textFieldNewMessage.clear();

        try {
            if(selectedRecipient != null){
                network.sendPrivateMessage(message, selectedRecipient);
            }
            else{
                if(message.startsWith("/cN")){
                    String[] parts = message.split("\\s+",2);
                    String newUsername = parts[1];
                    if(!newUsername.equals("")) {
                        network.changeNicknameMessage(this.labelUserNickname.getText(), newUsername);
                    } else{
                        appendMessage(DateUtil.getCurrentLocalTime() + " ERROR: new NickName not valid.");
                    }
                }else {
                    network.sendMessage(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            String errorMessage = DateUtil.getCurrentLocalTime() + " Failed to send message!";
            MainApp.showNetworkError(e.getMessage(), errorMessage);
        }
        catch (ArrayIndexOutOfBoundsException e){
            appendMessage(DateUtil.getCurrentLocalTime() + " ERROR: new NickName not valid.");
        }
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void showError(String title, String message) {
        MainApp.showNetworkError(message, title);
    }

    public void updateUsers(List<String> users) {
        usersList.setItems(FXCollections.observableArrayList(users));
    }

    public void loadLast100Messages(ArrayList<String> last100messages){
        //ArrayList<String> last100messages = network.readLast100MessageFromHistory();
        for (String last100message : last100messages) {
            textAreaMessenger.appendText(last100message);
            textAreaMessenger.appendText(System.lineSeparator());
        }
    }
}
