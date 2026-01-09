package com.ryan.property;

import com.ryan.property.ui.OwnerListView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        System.out.println("[APP] start() entered");

        // ✅ 主界面：业主列表
        OwnerListView root = new OwnerListView();

        Scene scene = new Scene(root, 980, 520);
        stage.setTitle("Property Management System - Owner Management");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
