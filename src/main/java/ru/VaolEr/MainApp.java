package ru.VaolEr;

import javafx.application.Application;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.VaolEr.controller.AuthenticationDialogController;
import ru.VaolEr.controller.Controller;
import ru.VaolEr.model.Network;
import ru.VaolEr.model.User;
import ru.VaolEr.repository.UsersRepository;
import ru.VaolEr.repository.impl.TestUsersRepository;

import java.io.File;
import java.net.URL;

public class MainApp extends Application {

    private Stage primaryStage;
    private Stage authDialogStage;
    private Network network;
    private Controller viewController;

    private static final String MAIN_APP_FXML = "src/main/java/ru/VaolEr/view/fxml/mainWindow.fxml";
    private static final String MAIN_APP_FXML_LIST = "src/main/java/ru/VaolEr/view/fxml/mainWindowList.fxml";
    private static final String AUTH_DIALOG_FXML = "src/main/java/ru/VaolEr/view/fxml/authenticationDialog.fxml";
    private final UsersRepository usersRepository;
    private final ObservableList<User> userData;

    public MainApp() {
        this.usersRepository = new TestUsersRepository();
        this.userData = FXCollections.observableArrayList(usersRepository.getAllData());
    }

    public static void showNetworkError(String errorDetails, String errorTitle) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Network Error");
        alert.setHeaderText(errorTitle);
        alert.setContentText(errorDetails);
        alert.showAndWait();
    }

    public void openChat() {
        authDialogStage.close();
        primaryStage.show();
        primaryStage.setTitle(network.getUsername());
        network.readLast100MessageFromHistory(viewController);
        network.waitMessages(viewController);
        network.changeNickNameInView(viewController);
    }

    public ObservableList<User> getUserData() {
        return userData;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;
        FXMLLoader authLoader = new FXMLLoader();

        URL auth_url = new File(AUTH_DIALOG_FXML).toURL();
        authLoader.setLocation(auth_url);
        Parent authDialogPanel = authLoader.load();
        authDialogStage = new Stage();

        authDialogStage.setTitle("Authentication...");
        authDialogStage.initModality(Modality.WINDOW_MODAL);
        authDialogStage.initOwner(primaryStage);
        Scene scene = new Scene(authDialogPanel);
        authDialogStage.setScene(scene);
        authDialogStage.show();

        network = new Network();
        if (!network.connect()) {
            showNetworkError("", "Failed to connect to server");
        }

        AuthenticationDialogController authController = authLoader.getController();
        authController.setNetwork(network);
        authController.setClientApp(this);

        //Parent root = FXMLLoader.load(getClass().getResource("../resources/mainWindow.fxml"));

        URL url = new File(MAIN_APP_FXML_LIST).toURL();
        //Parent root = FXMLLoader.load(url);

        FXMLLoader loader = new FXMLLoader();

        loader.setLocation(url);
        Parent root = loader.load();

        Controller controller = loader.getController();
        controller.setMainApp(this);

        primaryStage.setTitle("Chat");
        // primaryStage.getIcons().add(new Image("file:resources/icos/Vexels-Office-Bulb.ico"));
        primaryStage.setScene(new Scene(root, root.prefWidth(-1), root.prefHeight(-1)));
        primaryStage.setMinWidth(root.minWidth(-1));
        primaryStage.setMinHeight(root.minHeight(-1));
        primaryStage.setMaxHeight(root.maxHeight(-1));
        primaryStage.setMaxWidth(root.maxWidth(-1));

        //primaryStage.show();

        viewController = loader.getController();
        viewController.setNetwork(network);

        //        network.waitMessages(viewController);
        primaryStage.setOnCloseRequest(event -> {
            network.close();
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
