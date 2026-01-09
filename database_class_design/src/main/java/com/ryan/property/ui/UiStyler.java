package com.ryan.property.ui;

import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public final class UiStyler {

    public static final String APP_STYLESHEET = "/styles/app.css";

    private UiStyler() {
    }

    public static void applyDialogStyles(Dialog<?> dialog) {
        applyDialogStyles(dialog.getDialogPane());
    }

    public static void applyDialogStyles(DialogPane pane) {
        String stylesheet = UiStyler.class.getResource(APP_STYLESHEET).toExternalForm();
        if (!pane.getStylesheets().contains(stylesheet)) {
            pane.getStylesheets().add(stylesheet);
        }
        if (!pane.getStyleClass().contains("app-dialog")) {
            pane.getStyleClass().add("app-dialog");
        }
    }

    public static void applyInputStyles(Control... controls) {
        for (Control control : controls) {
            if (!control.getStyleClass().contains("app-input")) {
                control.getStyleClass().add("app-input");
            }
        }
    }
}
