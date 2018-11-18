package ru.brainmove.util;

import javafx.scene.control.Alert;

public class FxUtils {

    public static void showAlertDialog(final String title, final String headerText, final String contentText, Alert.AlertType alertType) {
        final Alert alert = new Alert(alertType);

        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }

}
