package com.ryan.property.ui;

import com.ryan.property.dao.ParkingUsageDao;
import com.ryan.property.model.ParkingUsageFilter;
import com.ryan.property.model.ParkingUsageRecord;
import com.ryan.property.model.ParkingUsageStats;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ParkingUsageView extends BorderPane {

    private final ParkingUsageDao parkingUsageDao = new ParkingUsageDao();
    private final ObservableList<ParkingUsageRecord> data = FXCollections.observableArrayList();
    private final ObservableList<ParkingUsageStats> statsData = FXCollections.observableArrayList();

    private final TableView<ParkingUsageRecord> table = new TableView<>(data);
    private final TableView<ParkingUsageStats> statsTable = new TableView<>(statsData);
    private final TextField keywordField = new TextField();
    private final ComboBox<String> statusCombo = new ComboBox<>();
    private final DatePicker startDatePicker = new DatePicker();
    private final DatePicker endDatePicker = new DatePicker();
    private final Label statusLabel = new Label("就绪");
    private final HBox statusBar = new HBox(statusLabel);

    public ParkingUsageView() {
        setPadding(new Insets(16));
        getStyleClass().add("view-root");

        Label title = new Label("停车位使用情况");
        title.getStyleClass().add("page-title");

        Button btnRefresh = new Button("刷新");
        btnRefresh.getStyleClass().add("btn-secondary");
        btnRefresh.setOnAction(e -> reload());

        HBox actions = new HBox(8, btnRefresh);
        actions.getStyleClass().add("top-bar-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(12, title, spacer, actions);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        setTop(topBar);

        buildTable();
        buildStatsTable();

        table.getStyleClass().add("app-table");
        statsTable.getStyleClass().add("app-table");

        table.setPlaceholder(buildEmptyState("暂无停车使用记录", "刷新数据", this::reload));
        statsTable.setPlaceholder(buildEmptyState("暂无统计数据", "刷新数据", this::reload));

        VBox card = new VBox(12, buildFilterBar(), table, buildStatsSection());
        card.getStyleClass().add("card");
        VBox.setVgrow(table, Priority.ALWAYS);

        setCenter(card);
        BorderPane.setMargin(card, new Insets(12, 0, 0, 0));

        statusBar.getStyleClass().addAll("status-bar", "status-info");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        setBottom(statusBar);
        BorderPane.setMargin(statusBar, new Insets(12, 0, 0, 0));

        reload();
    }

    private FlowPane buildFilterBar() {
        keywordField.setPromptText("车位位置/业主姓名/电话");
        statusCombo.getItems().setAll("", "进行中", "已结束");
        statusCombo.setValue("");
        startDatePicker.setPromptText("开始日期");
        endDatePicker.setPromptText("结束日期");

        UiStyler.applyInputStyles(keywordField, statusCombo, startDatePicker, endDatePicker);

        Button btnFilter = new Button("查询");
        Button btnReset = new Button("重置");

        btnFilter.getStyleClass().add("btn-primary");
        btnReset.getStyleClass().add("btn-ghost");

        btnFilter.setOnAction(e -> reload());
        btnReset.setOnAction(e -> {
            keywordField.clear();
            statusCombo.setValue("");
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
            reload();
        });

        Label keywordLabel = new Label("关键字");
        keywordLabel.getStyleClass().add("filter-label");
        Label statusLabel = new Label("状态");
        statusLabel.getStyleClass().add("filter-label");
        Label startLabel = new Label("开始");
        startLabel.getStyleClass().add("filter-label");
        Label endLabel = new Label("结束");
        endLabel.getStyleClass().add("filter-label");

        FlowPane filterBar = new FlowPane(10, 8,
                keywordLabel, keywordField,
                statusLabel, statusCombo,
                startLabel, startDatePicker,
                endLabel, endDatePicker,
                btnFilter, btnReset
        );
        filterBar.getStyleClass().add("filter-bar");
        filterBar.setAlignment(Pos.CENTER_LEFT);
        return filterBar;
    }

    private VBox buildStatsSection() {
        Label subtitle = new Label("年度使用统计");
        subtitle.getStyleClass().add("page-subtitle");

        VBox box = new VBox(8, subtitle, statsTable);
        VBox.setVgrow(statsTable, Priority.NEVER);
        statsTable.setPrefHeight(180);
        return box;
    }

    private void buildTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ParkingUsageRecord, Number> colId = new TableColumn<>("记录ID");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().getId()));

        TableColumn<ParkingUsageRecord, Number> colSlotId = new TableColumn<>("车位ID");
        colSlotId.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().getSlotId()));

        TableColumn<ParkingUsageRecord, String> colSlotLocation = new TableColumn<>("车位位置");
        colSlotLocation.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                safe(c.getValue().getSlotLocation())
        ));

        TableColumn<ParkingUsageRecord, Number> colOwnerId = new TableColumn<>("业主ID");
        colOwnerId.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().getOwnerId()));

        TableColumn<ParkingUsageRecord, String> colOwnerName = new TableColumn<>("业主姓名");
        colOwnerName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                safe(c.getValue().getOwnerName())
        ));

        TableColumn<ParkingUsageRecord, String> colOwnerPhone = new TableColumn<>("联系电话");
        colOwnerPhone.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                safe(c.getValue().getOwnerPhone())
        ));

        TableColumn<ParkingUsageRecord, String> colStart = new TableColumn<>("开始时间");
        colStart.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStartTime() == null ? "" : c.getValue().getStartTime().toString()
        ));

        TableColumn<ParkingUsageRecord, String> colEnd = new TableColumn<>("结束时间");
        colEnd.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getEndTime() == null ? "" : c.getValue().getEndTime().toString()
        ));

        TableColumn<ParkingUsageRecord, String> colFee = new TableColumn<>("费用(元)");
        colFee.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getFee() == null ? "" : c.getValue().getFee().toPlainString()
        ));

        TableColumn<ParkingUsageRecord, String> colStatus = new TableColumn<>("状态");
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().isActive() ? "进行中" : "已结束"
        ));

        table.getColumns().setAll(
                colId, colSlotId, colSlotLocation, colOwnerId, colOwnerName,
                colOwnerPhone, colStart, colEnd, colFee, colStatus
        );
    }

    private void buildStatsTable() {
        statsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ParkingUsageStats, Number> colYear = new TableColumn<>("年份");
        colYear.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getUsageYear()));

        TableColumn<ParkingUsageStats, Number> colCount = new TableColumn<>("使用次数");
        colCount.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getUsageCount()));

        TableColumn<ParkingUsageStats, String> colFee = new TableColumn<>("总费用(元)");
        colFee.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getTotalFee() == null ? "" : c.getValue().getTotalFee().toPlainString()
        ));

        statsTable.getColumns().setAll(colYear, colCount, colFee);
    }

    private void reload() {
        ParkingUsageFilter filter = new ParkingUsageFilter(
                keywordField.getText(),
                resolveStatus(statusCombo.getValue()),
                startDatePicker.getValue(),
                endDatePicker.getValue());

        List<ParkingUsageRecord> records = parkingUsageDao.findByFilter(filter);
        data.setAll(records);
        statsData.setAll(parkingUsageDao.fetchYearlyStats());

        showStatus("status-info", "已加载 " + records.size() + " 条停车使用记录");
    }

    private String resolveStatus(String selection) {
        if (selection == null || selection.isBlank()) {
            return "";
        }
        if ("进行中".equals(selection)) {
            return "ACTIVE";
        }
        if ("已结束".equals(selection)) {
            return "ENDED";
        }
        return selection;
    }

    private void showStatus(String styleClass, String message) {
        statusLabel.setText(message);
        statusBar.getStyleClass().removeAll("status-info", "status-warning", "status-error", "status-success");
        statusBar.getStyleClass().add(styleClass);
    }

    private VBox buildEmptyState(String title, String actionLabel, Runnable action) {
        Label icon = new Label("🅿️");
        icon.getStyleClass().add("empty-state-icon");
        Label headline = new Label(title);
        headline.getStyleClass().add("empty-state-title");
        Label subtext = new Label("请检查数据库或刷新数据");
        subtext.getStyleClass().add("empty-state-text");
        Button actionBtn = new Button(actionLabel);
        actionBtn.getStyleClass().add("btn-primary");
        actionBtn.setOnAction(e -> action.run());

        VBox box = new VBox(8, icon, headline, subtext, actionBtn);
        box.getStyleClass().add("empty-state");
        return box;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
