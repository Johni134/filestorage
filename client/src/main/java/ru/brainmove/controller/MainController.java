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
import ru.brainmove.file.*;
import ru.brainmove.util.FxUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

import static ru.brainmove.file.FileUtils.MAX_BYTE_SIZE;

@Setter
@Getter
public class MainController implements Initializable {
    private static final String CLIENT_STORAGE = "client_storage/";
    private static final String TEMP_FOLDER = "temp/";

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
                        final String path = CLIENT_STORAGE + (fm.getFileCounts() != 1 ? TEMP_FOLDER : "");
                        FileUtils.createFileAndDirectories(path, fm);
                        if (fm.getFileCounts() > 1) {
                            // файлы все сложились в темп, преобразуем их
                            if (FileUtils.createFileFromTemp(path, CLIENT_STORAGE, fm))
                                refreshLocalFilesList();
                        } else
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
        Files.list(Paths.get(CLIENT_STORAGE))
                .filter(p -> !TEMP_FOLDER.replaceAll("/", "").equals(p.getFileName().toString()))
                .filter(p -> !Files.isDirectory(p))
                .map(p -> p.getFileName().toString())
                .forEach(o -> filesList.getItems().add(o));
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
                long fileCounts = 1;
                Path filePath = Paths.get(CLIENT_STORAGE + focusedItem);
                File file = filePath.toFile();
                if (file.length() > MAX_BYTE_SIZE) {
                    long remainFileSize = file.length();
                    long fileCountBySize = remainFileSize / MAX_BYTE_SIZE + 1;
                    byte[] bytes = new byte[MAX_BYTE_SIZE];
                    Path tempPath = Paths.get(CLIENT_STORAGE + TEMP_FOLDER);
                    Files.createDirectories(tempPath);
                    RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                    while (remainFileSize > 0) {
                        if (remainFileSize > MAX_BYTE_SIZE) {
                            randomAccessFile.read(bytes, 0, MAX_BYTE_SIZE);
                            remainFileSize -= MAX_BYTE_SIZE;
                        } else {
                            bytes = new byte[(int) remainFileSize];
                            randomAccessFile.read(bytes, 0, (int) remainFileSize);
                            remainFileSize = 0;
                        }
                        String filePartName = String.format("%s.%03d", focusedItem, fileCounts++);
                        uploadPartOfFileToServer(filePartName, bytes, fileCountBySize, focusedItem);
                    }
                    randomAccessFile.close();
                } else
                    uploadFileToServer(Paths.get(CLIENT_STORAGE + focusedItem), fileCounts);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            FxUtils.showAlertDialog("Ошибка!", "Ошибка загрузки файла", "Необходимо выбрать строку с файлом из списка!", Alert.AlertType.ERROR);
        }
    }

    private void uploadFileToServer(Path filePath, long fileCounts) throws IOException {
        uploadFileToServer(filePath, fileCounts, filePath.getFileName().toString());
    }

    private void uploadFileToServer(Path filePath, long fileCounts, String realFileName) throws IOException {
        final FileMessage fileMessage = new FileMessage(filePath, fileCounts);
        fileMessage.setAccessToken(accessToken.getAccessToken());
        fileMessage.setId(accessToken.getId());
        fileMessage.setRealFilename(realFileName);
        Network.sendMsg(fileMessage);
    }

    private void uploadPartOfFileToServer(String partFileName,
                                          byte[] partFile,
                                          long fileCounts,
                                          String realFileName) {
        final FileMessage fileMessage = new FileMessage(partFileName, partFile, fileCounts, realFileName);
        fileMessage.setAccessToken(accessToken.getAccessToken());
        fileMessage.setId(accessToken.getId());
        Network.sendMsg(fileMessage);
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

    public void serverContextMenuDelete(ActionEvent actionEvent) {
        deleteFileFromServer(getFocusedItem(filesListServer));
    }

    private void deleteFileFromServer(String focusedItem) {
        if (focusedItem != null) {
            final FileRequest fileRequest = new FileRequest(focusedItem, true);
            fileRequest.setAccessToken(accessToken.getAccessToken());
            fileRequest.setId(accessToken.getId());
            Network.sendMsg(fileRequest);
        } else {
            FxUtils.showAlertDialog("Ошибка!", "Ошибка удаления файла", "Необходимо выбрать строку с файлом из списка!", Alert.AlertType.ERROR);
        }
    }
}
