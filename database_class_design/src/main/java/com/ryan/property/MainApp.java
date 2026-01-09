package com.ryan.property;

import com.ryan.property.ui.ComplaintListView;
import com.ryan.property.ui.OwnerListView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        System.out.println("[APP] start() entered");

        TabPane tabPane = new TabPane();

        Tab ownerTab = new Tab("业主管理", new OwnerListView());
        ownerTab.setClosable(false);

        Tab complaintTab = new Tab("投诉管理", new ComplaintListView());
        complaintTab.setClosable(false);

        tabPane.getTabs().setAll(ownerTab, complaintTab);

        Scene scene = new Scene(tabPane, 1200, 620);
        stage.setTitle("Property Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
