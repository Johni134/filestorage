package ru.brainmove.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import lombok.Getter;
import lombok.Setter;
import ru.brainmove.*;
import ru.brainmove.entity.User;
import ru.brainmove.util.FxUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ResourceBundle;

@Setter
@Getter
public class MainController implements Initializable {
    private static final String CLIENT_STORAGE = "client_storage/";
    @FXML
    ListView<String> filesListServer;

    @FXML
    ListView<String> filesList;
    private User user;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileMessage) {
                        final FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get(CLIENT_STORAGE + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
                    }
                    if (am instanceof FileListMessage) {
                        final FileListMessage fileListMessage = (FileListMessage) am;
                        refreshServerFilesList(fileListMessage.getFileList());
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
        filesListServer.setItems(FXCollections.observableArrayList());
        sendFileListRequest();
        refreshLocalFilesList();
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        final String focusedItem = (filesListServer.getFocusModel() == null ? null : filesListServer.getFocusModel().getFocusedItem());
        if (focusedItem != null) {
            Network.sendMsg(new FileRequest(focusedItem));
        } else {
            FxUtils.showAlertDialog("Ошибка!", "Ошибка скачивания файла", "Необходимо выбрать строку с файлом из списка!", Alert.AlertType.ERROR);
        }
    }

    private void sendFileListRequest() {
        Network.sendMsg(new FileListRequest());
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
        Files.list(Paths.get(CLIENT_STORAGE)).map(p -> p.getFileName().toString()).forEach(o -> filesList.getItems().add(o));
    }

    private void refreshServerFilesList(List<String> serverFileList) {
        if (Platform.isFxApplicationThread()) {
            refreshServerList(serverFileList);
        } else {
            Platform.runLater(() -> refreshServerList(serverFileList));
        }
    }

    private void refreshServerList(List<String> serverFileList) {
        filesListServer.getItems().clear();
        filesListServer.getItems().addAll(serverFileList);
    }

    public void pressOnUploadBtn(ActionEvent actionEvent) {
        final String focusedItem = (filesList.getFocusModel() == null ? null : filesList.getFocusModel().getFocusedItem());
        if (focusedItem != null) {
            try {
                Network.sendMsg(new FileMessage(Paths.get(CLIENT_STORAGE + focusedItem)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            FxUtils.showAlertDialog("Ошибка!", "Ошибка загрузки файла", "Необходимо выбрать строку с файлом из списка!", Alert.AlertType.ERROR);
        }
    }


}
