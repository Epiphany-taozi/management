package com.ryan.property.ui;

import java.time.LocalDate;

import com.ryan.property.dao.ComplaintDao;
import com.ryan.property.model.ComplaintOverview;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class ComplaintListView extends BorderPane {

    private final ComplaintDao complaintDao = new ComplaintDao();
    private final ObservableList<ComplaintOverview> data = FXCollections.observableArrayList();

    private final TableView<ComplaintOverview> table = new TableView<>(data);

    private final TextField statusField = new TextField();
    private final DatePicker startDatePicker = new DatePicker();
    private final DatePicker endDatePicker = new DatePicker();

    public ComplaintListView() {
        setPadding(new Insets(12));

        Text title = new Text("投诉信息（Complaints）");

        statusField.setPromptText("状态，如: 已处理");
        startDatePicker.setPromptText("开始日期");
        endDatePicker.setPromptText("结束日期");

        Button btnSearch = new Button("查询");
        Button btnReset = new Button("重置");

        btnSearch.setOnAction(e -> reload());
        btnReset.setOnAction(e -> {
            statusField.clear();
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
            reload();
        });

        HBox filters = new HBox(10,
                new Label("状态"), statusField,
                new Label("开始"), startDatePicker,
                new Label("结束"), endDatePicker,
                btnSearch, btnReset
        );
        filters.setPadding(new Insets(6, 0, 6, 0));

        ToolBar toolBar = new ToolBar(
                title,
                new Separator()
        );

        BorderPane topPane = new BorderPane();
        topPane.setTop(toolBar);
        topPane.setBottom(filters);

        setTop(topPane);

        buildTable();
        setCenter(table);

        reload();
    }

    private void buildTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ComplaintOverview, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().getId()));

        TableColumn<ComplaintOverview, String> colStatus = new TableColumn<>("状态");
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStatus() == null ? "" : c.getValue().getStatus()
        ));

        TableColumn<ComplaintOverview, String> colCreatedAt = new TableColumn<>("投诉时间");
        colCreatedAt.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getCreatedAt() == null ? "" : c.getValue().getCreatedAt().toString()
        ));

        TableColumn<ComplaintOverview, String> colReplyAt = new TableColumn<>("最新回复时间");
        colReplyAt.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getLatestReplyAt() == null ? "" : c.getValue().getLatestReplyAt().toString()
        ));

        TableColumn<ComplaintOverview, String> colReplySummary = new TableColumn<>("回复摘要");
        colReplySummary.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getLatestReplySummary()
        ));

        table.getColumns().setAll(colId, colStatus, colCreatedAt, colReplyAt, colReplySummary);
    }

    private void reload() {
        String status = statusField.getText();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        data.setAll(complaintDao.findByFilters(status, start, end));
        System.out.println("[UI] Complaint list loaded: " + data.size());
    }
}
