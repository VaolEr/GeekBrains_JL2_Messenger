package ru.VaolEr;

import javafx.application.Application;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.VaolEr.controller.Controller;
import ru.VaolEr.model.User;
import ru.VaolEr.repository.UsersRepository;
import ru.VaolEr.repository.impl.TestUsersRepository;

import java.io.File;
import java.net.URL;

public class MainApp extends Application {
    private static final String MAIN_APP_FXML = "src/main/java/ru/VaolEr/view/fxml/mainWindow.fxml";
    private final UsersRepository usersRepository;
    private final ObservableList<User> userData;

    public MainApp() {
        this.usersRepository = new TestUsersRepository();
        this.userData = FXCollections.observableArrayList(usersRepository.getAllData());
    }

    public ObservableList<User> getUserData() {
        return userData;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        //Parent root = FXMLLoader.load(getClass().getResource("../resources/mainWindow.fxml"));

        URL url = new File(MAIN_APP_FXML).toURL();
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
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
