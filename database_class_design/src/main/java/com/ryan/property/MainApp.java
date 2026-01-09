package com.ryan.property;

import com.ryan.property.ui.ComplaintListView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        System.out.println("[APP] start() entered");

        // ✅ 主界面：投诉列表
        ComplaintListView root = new ComplaintListView();

        Scene scene = new Scene(root, 980, 520);
        stage.setTitle("Property Management System - Complaint Management");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
