package ru.brainmove.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import ru.brainmove.AbstractMessage;
import ru.brainmove.MainClient;
import ru.brainmove.Network;
import ru.brainmove.auth.AuthMessage;
import ru.brainmove.auth.AuthRequest;
import ru.brainmove.auth.AuthType;
import ru.brainmove.util.FxUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoginController implements Initializable {
    @FXML
    TextField tfLogin;
    @FXML
    PasswordField pfPassword;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                AtomicBoolean successLogin = new AtomicBoolean(false);
                while (!successLogin.get()) {
                    final AbstractMessage am = Network.readObject();
                    if (am instanceof AuthMessage) {
                        final AuthMessage authMessage = (AuthMessage) am;
                        Platform.runLater(() -> {
                            try {
                                successLogin.set(checkLogin(authMessage));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private boolean checkLogin(AuthMessage fm) throws IOException {
        switch (fm.getAuthType()) {
            case REGISTRY:
                if (fm.isSuccess())
                    FxUtils.showAlertDialog("Успех!", "Регистрация", "Регистрация прошла успешно!", Alert.AlertType.INFORMATION);
            case LOGIN:
                if (fm.isSuccess()) {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main.fxml"));
                    Parent root = fxmlLoader.load();

                    // setting current user and token
                    MainController mainController = fxmlLoader.getController();
                    mainController.setUser(fm.getUser());
                    mainController.setAccessToken(fm.getToken());
                    mainController.sendFileListRequest();
                    // closing register stage
                    MainClient.getLoginStage().close();

                    Stage stage = new Stage();
                    stage.setTitle("Пользователь: " + (fm.getUser() == null ? "Unknown" : fm.getUser().getLogin()));

                    stage.setScene(new Scene(root));
                    stage.show();

                    return true;
                } else {
                    FxUtils.showAlertDialog("Ошибка!", "Произошла ошибка!", fm.getErrorMsg(), Alert.AlertType.ERROR);
                }
        }
        return false;
    }

    public void pressOnLoginBtn(ActionEvent actionEvent) {
        sendLoginAndPasswordByType(AuthType.LOGIN);
    }

    public void pressOnRegistryBtn(ActionEvent actionEvent) {
        sendLoginAndPasswordByType(AuthType.REGISTRY);
    }

    private void sendLoginAndPasswordByType(AuthType authType) {
        if (tfLogin.getLength() == 0 || pfPassword.getLength() == 0) {
            FxUtils.showAlertDialog("Ошибка!", "Введите логин и пароль!", "Необходимо ввести логин и пароль в поля логина и пароля!", Alert.AlertType.ERROR);
            return;
        }
        Network.sendMsg(new AuthRequest(tfLogin.getText(), pfPassword.getText(), authType));
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            sendLoginAndPasswordByType(AuthType.LOGIN);
        }
    }
}
