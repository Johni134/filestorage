package ru.brainmove;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

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
                while (true) {
                    final AbstractMessage am = Network.readObject();
                    if (am instanceof AuthMessage) {
                        final AuthMessage authMessage = (AuthMessage) am;
                        Platform.runLater(() -> {
                            try {
                                checkLogin(authMessage);
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

    private void checkLogin(AuthMessage fm) throws IOException {
        switch (fm.getAuthType()) {
            case REGISTRY:
                if (fm.isSuccess())
                    JOptionPane.showMessageDialog(null, "Регистрация прошла успешно!", "Успех!", JOptionPane.INFORMATION_MESSAGE);
            case LOGIN:
                if (fm.isSuccess()) {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main.fxml"));
                    Parent root = fxmlLoader.load();
                    MainClient.getLoginStage().close();

                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.show();
                } else {
                    JOptionPane.showMessageDialog(null, fm.getErrorMsg(), "Произошла ошибка!", JOptionPane.ERROR_MESSAGE);
                }
                break;
        }
    }

    public void pressOnLoginBtn(ActionEvent actionEvent) {
        sendLoginAndPasswordByType(AuthType.LOGIN);
    }

    public void pressOnRegistryBtn(ActionEvent actionEvent) {
        sendLoginAndPasswordByType(AuthType.REGISTRY);
    }

    private void sendLoginAndPasswordByType(AuthType authType) {
        if (tfLogin.getLength() == 0 || pfPassword.getLength() == 0) {
            JOptionPane.showMessageDialog(null, "Необходимо ввести логин и пароль в поля логина и пароля!", "Введите логин и пароль!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Network.sendMsg(new AuthRequest(tfLogin.getText(), pfPassword.getText(), authType));
    }
}
