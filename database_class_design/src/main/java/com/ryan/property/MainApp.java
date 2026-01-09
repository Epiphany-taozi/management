package com.ryan.property;

import com.ryan.property.ui.AssetListView;
import com.ryan.property.ui.ComplaintListView;
import com.ryan.property.ui.OwnerListView;
import com.ryan.property.ui.StaffListView;

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

        tabPane.getTabs().setAll(ownersTab, staffTab, assetsTab, complaintTab);

        Scene scene = new Scene(tabPane, 1200, 620);
        stage.setTitle("Property Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
