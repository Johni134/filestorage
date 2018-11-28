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
import ru.brainmove.AbstractMessage;
import ru.brainmove.Network;
import ru.brainmove.entity.Token;
import ru.brainmove.entity.User;
import ru.brainmove.file.FileListMessage;
import ru.brainmove.file.FileListRequest;
import ru.brainmove.file.FileMessage;
import ru.brainmove.file.FileRequest;
import ru.brainmove.util.FxUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private Token accessToken;

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
        refreshLocalFilesList();
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        downloadFileFromServer(getFocusedItem(filesListServer));
    }

    private void downloadFileFromServer(final String focusedItem) {
        if (focusedItem != null) {
            final FileRequest fileRequest = new FileRequest(focusedItem);
            fileRequest.setAccessToken(accessToken.getAccessToken());
            fileRequest.setId(accessToken.getId());
            Network.sendMsg(fileRequest);
        } else {
            FxUtils.showAlertDialog("Ошибка!", "Ошибка скачивания файла", "Необходимо выбрать строку с файлом из списка!", Alert.AlertType.ERROR);
        }
    }

    void sendFileListRequest() {
        final FileListRequest fileListRequest = new FileListRequest();
        fileListRequest.setAccessToken(accessToken.getAccessToken());
        fileListRequest.setId(accessToken.getId());
        Network.sendMsg(fileListRequest);
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
        final String focusedItem = getFocusedItem(filesListServer);
        final int newIndexOfFocusedItem = serverFileList.indexOf(focusedItem);
        filesListServer.getItems().clear();
        filesListServer.getItems().addAll(serverFileList);
        if (newIndexOfFocusedItem != -1) {
            filesListServer.getSelectionModel().select(newIndexOfFocusedItem);
            filesListServer.getFocusModel().focus(newIndexOfFocusedItem);
            filesListServer.scrollTo(newIndexOfFocusedItem);
        }
    }

    private void uploadFileToServer(final String focusedItem) {
        if (focusedItem != null) {
            try {
                final FileMessage fileMessage = new FileMessage(Paths.get(CLIENT_STORAGE + focusedItem));
                fileMessage.setAccessToken(accessToken.getAccessToken());
                fileMessage.setId(accessToken.getId());
                Network.sendMsg(fileMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            FxUtils.showAlertDialog("Ошибка!", "Ошибка загрузки файла", "Необходимо выбрать строку с файлом из списка!", Alert.AlertType.ERROR);
        }
    }

    private void deleteFile(final String focusedItem) {
        if (focusedItem != null) {
            try {
                final Path filePath = Paths.get(CLIENT_STORAGE + focusedItem);
                if (Files.exists(filePath)) Files.delete(filePath);
                refreshLocalFilesList();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            FxUtils.showAlertDialog("Ошибка!", "Ошибка удаления файла", "Необходимо выбрать строку с файлом из списка!", Alert.AlertType.ERROR);
        }
    }

    public void pressOnUploadBtn(ActionEvent actionEvent) {
        uploadFileToServer(getFocusedItem(filesList));
    }

    private String getFocusedItem(final ListView<String> items) {
        return (items.getFocusModel() == null ? null : items.getFocusModel().getFocusedItem());
    }

    public void clientContextMenuUpload(ActionEvent actionEvent) {
        uploadFileToServer(getFocusedItem(filesList));
    }

    public void clientContextMenuDelete(ActionEvent actionEvent) {
        deleteFile(getFocusedItem(filesList));
    }

    public void serverContextMenuDownload(ActionEvent actionEvent) {
        downloadFileFromServer(getFocusedItem(filesListServer));
    }

    public void clientContextMenuRefresh(ActionEvent actionEvent) {
        refreshLocalFilesList();
    }

    public void serverContextMenuRefresh(ActionEvent actionEvent) {
        sendFileListRequest();
    }
}
