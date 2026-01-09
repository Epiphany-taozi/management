package com.ryan.property.ui;

import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.Scene;

public final class UiStyler {

    public static final String APP_STYLESHEET = "/styles/app.css";
    public static final String THEME_STYLESHEET = "/styles/theme.css";
    public static final String COMPONENTS_STYLESHEET = "/styles/components.css";
    public static final String PAGES_STYLESHEET = "/styles/pages.css";
    private static boolean darkMode = false;

    private UiStyler() {
    }

    public static void applyDialogStyles(Dialog<?> dialog) {
        applyDialogStyles(dialog.getDialogPane());
    }

    public static void applyDialogStyles(DialogPane pane) {
        applyStylesheets(pane);
        if (!pane.getStyleClass().contains("app-dialog")) {
            pane.getStyleClass().add("app-dialog");
        }
        syncThemeClass(pane);
    }

    public static void applySceneStyles(Scene scene) {
        applyStylesheets(scene);
    }

    public static void setDarkMode(boolean enabled) {
        darkMode = enabled;
    }

    public static void applyInputStyles(Control... controls) {
        for (Control control : controls) {
            if (!control.getStyleClass().contains("app-input")) {
                control.getStyleClass().add("app-input");
            }
        }
    }

    private static void applyStylesheets(DialogPane pane) {
        String stylesheet = UiStyler.class.getResource(APP_STYLESHEET).toExternalForm();
        if (!pane.getStylesheets().contains(stylesheet)) {
            pane.getStylesheets().add(stylesheet);
        }
    }

    private static void applyStylesheets(Scene scene) {
        String stylesheet = UiStyler.class.getResource(APP_STYLESHEET).toExternalForm();
        if (!scene.getStylesheets().contains(stylesheet)) {
            scene.getStylesheets().add(stylesheet);
        }
    }

    private static void syncThemeClass(DialogPane pane) {
        if (darkMode) {
            if (!pane.getStyleClass().contains("theme-dark")) {
                pane.getStyleClass().add("theme-dark");
            }
        } else {
            pane.getStyleClass().remove("theme-dark");
        }
    }
}
