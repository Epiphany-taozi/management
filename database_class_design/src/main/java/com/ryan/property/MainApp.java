package com.ryan.property;

import com.ryan.property.ui.AssetListView;
import com.ryan.property.ui.ComplaintListView;
import com.ryan.property.ui.OwnerListView;
import com.ryan.property.ui.StaffListView;
import com.ryan.property.ui.UiStyler;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        System.out.println("[APP] start() entered");

        TabPane tabPane = new TabPane();

        Tab ownersTab = new Tab("业主");
        ownersTab.setClosable(false);
        ownersTab.setContent(new OwnerListView());

        Tab staffTab = new Tab("工作人员");
        staffTab.setClosable(false);
        staffTab.setContent(new StaffListView());

        Tab assetsTab = new Tab("公共财产");
        assetsTab.setClosable(false);
        assetsTab.setContent(new AssetListView());

        Tab complaintTab = new Tab("投诉管理");
        complaintTab.setClosable(false);
        complaintTab.setContent(new ComplaintListView());

        tabPane.getStyleClass().add("app-tabs");
        tabPane.getTabs().setAll(ownersTab, staffTab, assetsTab, complaintTab);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setPadding(new Insets(16));

        HBox appShell = new HBox(12);
        appShell.getStyleClass().add("app-shell");
        appShell.setAlignment(Pos.CENTER_LEFT);

        Label appTitle = new Label("Property Management System");
        appTitle.getStyleClass().add("app-shell-title");

        ToggleButton themeToggle = new ToggleButton("暗色模式");
        themeToggle.getStyleClass().add("btn-secondary");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        appShell.getChildren().addAll(appTitle, spacer, themeToggle);

        root.setTop(appShell);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1200, 680);
        UiStyler.applySceneStyles(scene);

        themeToggle.selectedProperty().addListener((obs, wasDark, isDark) -> {
            if (isDark) {
                if (!root.getStyleClass().contains("theme-dark")) {
                    root.getStyleClass().add("theme-dark");
                }
            } else {
                root.getStyleClass().remove("theme-dark");
            }
            UiStyler.setDarkMode(isDark);
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && newTab.getContent() != null) {
                FadeTransition fade = new FadeTransition(Duration.millis(160), newTab.getContent());
                fade.setFromValue(0.2);
                fade.setToValue(1.0);
                fade.playFromStart();
            }
        });

        stage.setTitle("Property Management System");
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch();
    }
}
