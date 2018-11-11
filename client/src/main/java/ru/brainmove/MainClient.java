package ru.brainmove;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainClient extends Application {

    private static Stage loginStage;

    static Stage getLoginStage() {
        return loginStage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Filestorage Client");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        loginStage = primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
