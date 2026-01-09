package com.ryan.property.ui;

import com.ryan.property.fees.FeeFilter;
import com.ryan.property.fees.FeeQueryService;
import com.ryan.property.fees.FeeRecord;
import com.ryan.property.fees.FeeSummary;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class FeeDataView extends BorderPane {

    private final FeeQueryService feeQueryService = new FeeQueryService();
    private final ObservableList<FeeRecord> data = FXCollections.observableArrayList();
    private final ObservableList<FeeSummary> summaryData = FXCollections.observableArrayList();

    private final TableView<FeeRecord> table = new TableView<>(data);
    private final TableView<FeeSummary> summaryTable = new TableView<>(summaryData);

    private final TextField ownerKeywordField = new TextField();
    private final TextField feeTypeField = new TextField();
    private final ComboBox<String> statusCombo = new ComboBox<>();
    private final DatePicker startDatePicker = new DatePicker();
    private final DatePicker endDatePicker = new DatePicker();
    private final Label statusLabel = new Label("就绪");
    private final HBox statusBar = new HBox(statusLabel);

    public FeeDataView() {
        setPadding(new Insets(16));
        getStyleClass().add("view-root");

        Label title = new Label("收费数据");
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
        buildSummaryTable();
        table.getStyleClass().add("app-table");
        summaryTable.getStyleClass().add("app-table");

        table.setPlaceholder(buildEmptyState("暂无收费记录", "刷新数据", this::reload));
        summaryTable.setPlaceholder(buildEmptyState("暂无统计数据", "刷新数据", this::reload));

        VBox card = new VBox(12, buildFilterBar(), table, buildSummarySection());
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
        ownerKeywordField.setPromptText("业主姓名/电话");
        feeTypeField.setPromptText("收费类型");
        statusCombo.getItems().setAll("", "已缴", "未缴");
        statusCombo.setValue("");
        startDatePicker.setPromptText("开始日期");
        endDatePicker.setPromptText("结束日期");

        UiStyler.applyInputStyles(ownerKeywordField, feeTypeField, statusCombo, startDatePicker, endDatePicker);

        Button btnFilter = new Button("查询");
        Button btnReset = new Button("重置");
        btnFilter.getStyleClass().add("btn-primary");
        btnReset.getStyleClass().add("btn-ghost");

        btnFilter.setOnAction(e -> reload());
        btnReset.setOnAction(e -> {
            ownerKeywordField.clear();
            feeTypeField.clear();
            statusCombo.setValue("");
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
            reload();
        });

        Label ownerLabel = new Label("业主");
        ownerLabel.getStyleClass().add("filter-label");
        Label feeTypeLabel = new Label("类型");
        feeTypeLabel.getStyleClass().add("filter-label");
        Label statusLabel = new Label("状态");
        statusLabel.getStyleClass().add("filter-label");
        Label startLabel = new Label("开始");
        startLabel.getStyleClass().add("filter-label");
        Label endLabel = new Label("结束");
        endLabel.getStyleClass().add("filter-label");

        FlowPane filterBar = new FlowPane(10, 8,
                ownerLabel, ownerKeywordField,
                feeTypeLabel, feeTypeField,
                statusLabel, statusCombo,
                startLabel, startDatePicker,
                endLabel, endDatePicker,
                btnFilter, btnReset
        );
        filterBar.getStyleClass().add("filter-bar");
        filterBar.setAlignment(Pos.CENTER_LEFT);
        return filterBar;
    }

    private VBox buildSummarySection() {
        Label subtitle = new Label("年度收费统计");
        subtitle.getStyleClass().add("page-subtitle");

        VBox box = new VBox(8, subtitle, summaryTable);
        summaryTable.setPrefHeight(180);
        VBox.setVgrow(summaryTable, Priority.NEVER);
        return box;
    }

    private void buildTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<FeeRecord, Number> colId = new TableColumn<>("记录ID");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().id()));

        TableColumn<FeeRecord, Number> colOwnerId = new TableColumn<>("业主ID");
        colOwnerId.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().ownerId()));

        TableColumn<FeeRecord, String> colOwnerName = new TableColumn<>("业主姓名");
        colOwnerName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                safe(c.getValue().ownerName())
        ));

        TableColumn<FeeRecord, String> colFeeType = new TableColumn<>("收费类型");
        colFeeType.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                safe(c.getValue().feeType())
        ));

        TableColumn<FeeRecord, String> colAmount = new TableColumn<>("金额(元)");
        colAmount.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().amount() == null ? "" : c.getValue().amount().toPlainString()
        ));

        TableColumn<FeeRecord, String> colStatus = new TableColumn<>("状态");
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().statusLabel()
        ));

        TableColumn<FeeRecord, String> colPaidDate = new TableColumn<>("缴费日期");
        colPaidDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().paidDate() == null ? "" : c.getValue().paidDate().toString()
        ));

        table.getColumns().setAll(
                colId, colOwnerId, colOwnerName, colFeeType, colAmount, colStatus, colPaidDate
        );
    }

    private void buildSummaryTable() {
        summaryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<FeeSummary, Number> colYear = new TableColumn<>("年份");
        colYear.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().paidYear()));

        TableColumn<FeeSummary, String> colType = new TableColumn<>("收费类型");
        colType.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                safe(c.getValue().feeType())
        ));

        TableColumn<FeeSummary, String> colTotalAmount = new TableColumn<>("总金额(元)");
        colTotalAmount.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().totalAmount() == null ? "" : c.getValue().totalAmount().toPlainString()
        ));

        TableColumn<FeeSummary, Number> colTotalCount = new TableColumn<>("收费笔数");
        colTotalCount.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().totalCount()));

        summaryTable.getColumns().setAll(colYear, colType, colTotalAmount, colTotalCount);
    }

    private void reload() {
        FeeFilter filter = new FeeFilter(
                ownerKeywordField.getText(),
                feeTypeField.getText(),
                resolveStatus(statusCombo.getValue()),
                startDatePicker.getValue(),
                endDatePicker.getValue());

        List<FeeRecord> records = feeQueryService.query(filter);
        data.setAll(records);
        summaryData.setAll(feeQueryService.querySummary());

        showStatus("status-info", "已加载 " + records.size() + " 条收费记录");
    }

    private String resolveStatus(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if ("已缴".equals(value)) {
            return "PAID";
        }
        if ("未缴".equals(value)) {
            return "UNPAID";
        }
        return value;
    }

    private void showStatus(String styleClass, String message) {
        statusLabel.setText(message);
        statusBar.getStyleClass().removeAll("status-info", "status-warning", "status-error", "status-success");
        statusBar.getStyleClass().add(styleClass);
    }

    private VBox buildEmptyState(String title, String actionLabel, Runnable action) {
        Label icon = new Label("💳");
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
