package ru.brainmove;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.FocusModel;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static final String CLIENT_STORAGE = "client_storage/";
    @FXML
    TextField tfFileName;

    @FXML
    ListView<String> filesList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get(CLIENT_STORAGE + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
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
        filesList.setItems(FXCollections.observableArrayList());
        refreshLocalFilesList();
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0) {
            Network.sendMsg(new FileRequest(tfFileName.getText()));
            tfFileName.clear();
        }
    }

    private void refreshLocalFilesList() {
        if (Platform.isFxApplicationThread()) {
            try {
                refreshList();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    refreshList();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void refreshList() throws IOException {
        filesList.getItems().clear();
        Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesList.getItems().add(o));
    }

    public void pressOnUploadBtn(ActionEvent actionEvent) {
        final String focusedItem = (filesList.getFocusModel() == null ? null : filesList.getFocusModel().getFocusedItem());
        if (focusedItem != null) {
            try {
                Network.sendMsg(new FileMessage(Paths.get(CLIENT_STORAGE + focusedItem)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            JOptionPane.showMessageDialog(null, "Необходимо выбрать строку с файлом из списка!", "Ошибка загрузки файла", JOptionPane.ERROR_MESSAGE);
        }
    }
}
