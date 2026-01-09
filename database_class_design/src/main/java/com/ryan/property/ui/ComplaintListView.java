package com.ryan.property.ui;

import java.time.LocalDate;
import java.util.Optional;

import com.ryan.property.dao.ComplaintDao;
import com.ryan.property.model.Complaint;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ComplaintListView extends BorderPane {

    private final ComplaintDao complaintDao = new ComplaintDao();

    private final ObservableList<Complaint> data = FXCollections.observableArrayList();
    private final TableView<Complaint> table = new TableView<>(data);

    private final TextField ownerIdField = new TextField();
    private final ComboBox<String> statusCombo = new ComboBox<>();
    private final DatePicker startDatePicker = new DatePicker();
    private final DatePicker endDatePicker = new DatePicker();

    public ComplaintListView() {
        setPadding(new Insets(12));

        Text title = new Text("投诉管理（Complaints）");

        Button btnAdd = new Button("登记投诉");
        Button btnReply = new Button("回复");
        Button btnRefresh = new Button("刷新");

        btnAdd.setOnAction(e -> onAdd());
        btnReply.setOnAction(e -> onReply());
        btnRefresh.setOnAction(e -> reload());

        ToolBar toolBar = new ToolBar(
                title,
                new Separator(),
                btnAdd,
                btnReply,
                new Separator(),
                btnRefresh
        );

        setTop(new VBox(toolBar, buildFilterBar()));

        buildTable();
        setCenter(table);

        table.setRowFactory(tv -> {
            TableRow<Complaint> row = new TableRow<>();
            row.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2 && !row.isEmpty()) {
                    onReply();
                }
            });
            return row;
        });

        reload();
    }

    private HBox buildFilterBar() {
        ownerIdField.setPromptText("业主ID（可空）");

        // 状态值建议与你数据库/业务状态保持一致
        // 你也可以按自己实际情况加/减，例如：CLOSED / REJECTED 等
        statusCombo.getItems().setAll("", "NEW", "IN_PROGRESS", "RESOLVED");
        statusCombo.setValue("");

        startDatePicker.setPromptText("开始日期");
        endDatePicker.setPromptText("结束日期");

        Button btnFilter = new Button("查询");
        Button btnReset = new Button("重置");

        btnFilter.setOnAction(e -> reload());
        btnReset.setOnAction(e -> {
            ownerIdField.clear();
            statusCombo.setValue("");
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
            reload();
        });

        HBox filterBar = new HBox(10,
                new Label("业主ID"), ownerIdField,
                new Label("状态"), statusCombo,
                new Label("开始"), startDatePicker,
                new Label("结束"), endDatePicker,
                btnFilter, btnReset
        );
        filterBar.setPadding(new Insets(10, 0, 0, 0));
        return filterBar;
    }

    private void buildTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Complaint, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().getId()));

        TableColumn<Complaint, Number> colOwnerId = new TableColumn<>("业主ID");
        colOwnerId.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().getOwnerId()));

        TableColumn<Complaint, String> colTitle = new TableColumn<>("标题");
        colTitle.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                safeStr(c.getValue().getTitle())
        ));

        TableColumn<Complaint, String> colContent = new TableColumn<>("内容");
        colContent.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                safeStr(c.getValue().getContent())
        ));

        TableColumn<Complaint, String> colStatus = new TableColumn<>("状态");
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                safeStr(c.getValue().getStatus())
        ));

        TableColumn<Complaint, String> colCreatedAt = new TableColumn<>("创建时间");
        colCreatedAt.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getCreatedAt() == null ? "" : c.getValue().getCreatedAt().toString()
        ));

        TableColumn<Complaint, String> colUpdatedAt = new TableColumn<>("更新时间");
        colUpdatedAt.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getUpdatedAt() == null ? "" : c.getValue().getUpdatedAt().toString()
        ));

        TableColumn<Complaint, String> colLatestReply = new TableColumn<>("最新回复摘要");
        colLatestReply.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                summarizeReply(c.getValue().getLatestReplyContent(), c.getValue().getLatestReplyTime())
        ));

        table.getColumns().setAll(
                colId, colOwnerId, colTitle, colContent, colStatus, colCreatedAt, colUpdatedAt, colLatestReply
        );
    }

    private void reload() {
        Long ownerId = parseOwnerId();
        String status = statusCombo.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        data.setAll(complaintDao.findByFilters(ownerId, status, startDate, endDate));
        System.out.println("[UI] Complaint list loaded: " + data.size());
    }

    private Long parseOwnerId() {
        String raw = ownerIdField.getText().trim();
        if (raw.isEmpty()) return null;
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            alertWarn("提示", "业主ID请输入数字。");
            return null;
        }
    }

    private void onAdd() {
        Optional<Complaint> opt = showComplaintDialog("登记投诉");
        if (opt.isEmpty()) return;

        Complaint toInsert = opt.get();
        long newId = complaintDao.insert(toInsert);
        if (newId > 0) {
            alertInfo("成功", "已登记投诉，ID = " + newId);
            reload();
        } else {
            alertError("失败", "登记失败，请查看控制台错误信息。");
        }
    }

    private void onReply() {
        Complaint selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertWarn("提示", "请先在表格中选中一条投诉记录。");
            return;
        }

        Optional<ReplyForm> opt = showReplyDialog("回复投诉（ID=" + selected.getId() + "）");
        if (opt.isEmpty()) return;

        ReplyForm form = opt.get();
        boolean ok = complaintDao.addReply(selected.getId(), form.staffId, form.replyContent, form.newStatus);
        if (ok) {
            alertInfo("成功", "已回复投诉并更新状态。");
            reload();
        } else {
            alertError("失败", "回复失败，请查看控制台错误信息。");
        }
    }

    private Optional<Complaint> showComplaintDialog(String title) {
        Dialog<Complaint> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType okType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField tfOwnerId = new TextField();
        TextField tfTitle = new TextField();
        TextArea taContent = new TextArea();
        taContent.setPrefRowCount(4);

        tfOwnerId.setPromptText("例如：1001");
        tfTitle.setPromptText("例如：噪音扰民");
        taContent.setPromptText("请输入投诉内容");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(14));

        int r = 0;
        grid.add(new Label("业主ID*"), 0, r);    grid.add(tfOwnerId, 1, r++);
        grid.add(new Label("标题*"), 0, r);      grid.add(tfTitle, 1, r++);
        grid.add(new Label("内容*"), 0, r);      grid.add(taContent, 1, r++);

        dialog.getDialogPane().setContent(grid);

        Node okBtn = dialog.getDialogPane().lookupButton(okType);
        okBtn.setDisable(true);

        Runnable validator = () -> okBtn.setDisable(
                tfOwnerId.getText().trim().isEmpty()
                        || tfTitle.getText().trim().isEmpty()
                        || taContent.getText().trim().isEmpty()
        );

        tfOwnerId.textProperty().addListener((a, b, c) -> validator.run());
        tfTitle.textProperty().addListener((a, b, c) -> validator.run());
        taContent.textProperty().addListener((a, b, c) -> validator.run());

        validator.run();

        dialog.setResultConverter(btn -> {
            if (btn != okType) return null;

            long ownerId;
            try {
                ownerId = Long.parseLong(tfOwnerId.getText().trim());
            } catch (NumberFormatException ex) {
                alertWarn("提示", "业主ID请输入数字。");
                return null;
            }

            Complaint c = new Complaint();
            c.setOwnerId(ownerId);
            c.setTitle(tfTitle.getText().trim());
            c.setContent(taContent.getText().trim());
            c.setStatus("NEW");
            return c;
        });

        return dialog.showAndWait();
    }

    private Optional<ReplyForm> showReplyDialog(String title) {
        Dialog<ReplyForm> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType okType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField tfStaffId = new TextField();
        TextArea taReply = new TextArea();
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().setAll("IN_PROGRESS", "RESOLVED");
        statusBox.setValue("IN_PROGRESS");
        taReply.setPrefRowCount(4);

        tfStaffId.setPromptText("例如：2001");
        taReply.setPromptText("请输入回复内容");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(14));

        int r = 0;
        grid.add(new Label("员工ID*"), 0, r);    grid.add(tfStaffId, 1, r++);
        grid.add(new Label("状态*"), 0, r);      grid.add(statusBox, 1, r++);
        grid.add(new Label("回复内容*"), 0, r);  grid.add(taReply, 1, r++);

        dialog.getDialogPane().setContent(grid);

        Node okBtn = dialog.getDialogPane().lookupButton(okType);
        okBtn.setDisable(true);

        Runnable validator = () -> okBtn.setDisable(
                tfStaffId.getText().trim().isEmpty()
                        || taReply.getText().trim().isEmpty()
                        || statusBox.getValue() == null
        );

        tfStaffId.textProperty().addListener((a, b, c) -> validator.run());
        taReply.textProperty().addListener((a, b, c) -> validator.run());
        statusBox.valueProperty().addListener((a, b, c) -> validator.run());

        validator.run();

        dialog.setResultConverter(btn -> {
            if (btn != okType) return null;

            long staffId;
            try {
                staffId = Long.parseLong(tfStaffId.getText().trim());
            } catch (NumberFormatException ex) {
                alertWarn("提示", "员工ID请输入数字。");
                return null;
            }

            ReplyForm form = new ReplyForm();
            form.staffId = staffId;
            form.replyContent = taReply.getText().trim();
            form.newStatus = statusBox.getValue();
            return form;
        });

        return dialog.showAndWait();
    }

    private String summarizeReply(String content, java.sql.Timestamp time) {
        if (content == null || content.isBlank()) return "";
        String trimmed = content.trim().replace("\n", " ");
        if (trimmed.length() > 30) trimmed = trimmed.substring(0, 30) + "...";
        if (time == null) return trimmed;
        return trimmed + " (" + time.toString() + ")";
    }

    private String safeStr(String s) {
        return s == null ? "" : s;
    }

    private void alertInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void alertWarn(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void alertError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private static class ReplyForm {
        private long staffId;
        private String replyContent;
        private String newStatus;
    }
}
