package com.ryan.property.ui;

import java.util.Optional;

import com.ryan.property.dao.OwnerDao;
import com.ryan.property.model.Owner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import com.ryan.property.ui.UiStyler;

public class OwnerListView extends BorderPane {

    private final OwnerDao ownerDao = new OwnerDao();
    private final ObservableList<Owner> data = FXCollections.observableArrayList();

    private final TableView<Owner> table = new TableView<>(data);
    private final TextField keywordField = new TextField();
    private String currentKeyword = "";

    public OwnerListView() {
        setPadding(new Insets(16));
        getStyleClass().add("view-root");

        Text title = new Text("业主信息（Owners）");
        title.getStyleClass().add("view-title");

        Button btnAdd = new Button("新增");
        Button btnEdit = new Button("修改");
        Button btnDel = new Button("删除");
        Button btnSearch = new Button("搜索");
        Button btnClear = new Button("清空");
        Button btnRefresh = new Button("刷新");

        btnAdd.setOnAction(e -> onAdd());
        btnEdit.setOnAction(e -> onEdit());
        btnDel.setOnAction(e -> onDelete());
        btnSearch.setOnAction(e -> onSearch());
        btnClear.setOnAction(e -> onClearSearch());
        btnRefresh.setOnAction(e -> reload());

        btnAdd.getStyleClass().add("btn-primary");
        btnEdit.getStyleClass().add("btn-secondary");
        btnDel.getStyleClass().add("btn-danger");
        btnSearch.getStyleClass().add("btn-primary");
        btnClear.getStyleClass().add("btn-ghost");
        btnRefresh.getStyleClass().add("btn-secondary");

        keywordField.setPromptText("按姓名/手机号/楼栋/单元搜索");
        keywordField.setPrefWidth(240);
        keywordField.setOnAction(e -> onSearch());
        UiStyler.applyInputStyles(keywordField);

        ToolBar toolBar = new ToolBar(
                title,
                new Separator(),
                new Label("关键字:"),
                keywordField,
                btnSearch,
                btnClear,
                new Separator(),
                btnAdd, btnEdit, btnDel,
                new Separator(),
                btnRefresh
        );
        toolBar.getStyleClass().add("app-toolbar");

        setTop(toolBar);

        buildTable();
        table.getStyleClass().add("app-table");
        setCenter(table);
        BorderPane.setMargin(table, new Insets(12, 0, 0, 0));

        table.setRowFactory(tv -> {
            TableRow<Owner> row = new TableRow<>();
            row.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2 && !row.isEmpty()) {
                    onEdit();
                }
            });
            return row;
        });

        reload();
    }

    private void buildTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Owner, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().getId()));

        TableColumn<Owner, String> colName = new TableColumn<>("姓名");
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));

        TableColumn<Owner, String> colPhone = new TableColumn<>("电话");
        colPhone.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPhone()));

        TableColumn<Owner, String> colBuilding = new TableColumn<>("楼栋");
        colBuilding.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBuilding()));

        TableColumn<Owner, String> colUnit = new TableColumn<>("单元");
        colUnit.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUnit()));

        table.getColumns().setAll(colId, colName, colPhone, colBuilding, colUnit);
    }

    private void reload() {
        if (currentKeyword == null || currentKeyword.isBlank()) {
            data.setAll(ownerDao.findAll());
        } else {
            data.setAll(ownerDao.findByKeyword(currentKeyword));
        }
        System.out.println("[UI] Owner list loaded: " + data.size());
    }

    private void onSearch() {
        currentKeyword = keywordField.getText().trim();
        reload();
    }

    private void onClearSearch() {
        keywordField.clear();
        currentKeyword = "";
        reload();
    }

    // ---------------- CRUD actions ----------------

    private void onAdd() {
        Optional<Owner> opt = showOwnerDialog("新增业主", null);
        if (opt.isEmpty()) return;

        Owner toInsert = opt.get();
        long newId = ownerDao.insert(toInsert);
        if (newId > 0) {
            alertInfo("成功", "已新增业主，ID = " + newId);
            reload();
        } else {
            alertError("失败", "新增失败，请查看控制台错误信息。");
        }
    }

    private void onEdit() {
        Owner selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertWarn("提示", "请先在表格中选中一条业主记录。");
            return;
        }

        Optional<Owner> opt = showOwnerDialog("修改业主（ID=" + selected.getId() + "）", selected);
        if (opt.isEmpty()) return;

        Owner edited = opt.get();
        edited.setId(selected.getId());

        boolean ok = ownerDao.update(edited);
        if (ok) {
            alertInfo("成功", "已更新业主信息。");
            reload();
        } else {
            alertError("失败", "更新失败，请查看控制台错误信息。");
        }
    }

    private void onDelete() {
        Owner selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertWarn("提示", "请先在表格中选中一条业主记录。");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认删除");
        confirm.setHeaderText("确定要删除该业主吗？");
        confirm.setContentText("ID=" + selected.getId() + "，姓名=" + selected.getName());

        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isEmpty() || r.get() != ButtonType.OK) return;

        boolean ok = ownerDao.deleteById(selected.getId());
        if (ok) {
            alertInfo("成功", "已删除。");
            reload();
        } else {
            alertError("失败", "删除失败，请查看控制台错误信息。");
        }
    }

    // ---------------- Dialog (Add/Edit) ----------------

    private Optional<Owner> showOwnerDialog(String title, Owner origin) {
        Dialog<Owner> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        UiStyler.applyDialogStyles(dialog);

        ButtonType okType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField tfName = new TextField();
        TextField tfPhone = new TextField();
        TextField tfBuilding = new TextField();
        TextField tfUnit = new TextField();
        UiStyler.applyInputStyles(tfName, tfPhone, tfBuilding, tfUnit);

        tfName.setPromptText("例如：张三");
        tfPhone.setPromptText("例如：13800000000");
        tfBuilding.setPromptText("例如：1号楼");
        tfUnit.setPromptText("例如：1单元-101");

        if (origin != null) {
            tfName.setText(origin.getName());
            tfPhone.setText(origin.getPhone());
            tfBuilding.setText(origin.getBuilding());
            tfUnit.setText(origin.getUnit());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(14));
        grid.getStyleClass().add("form-grid");

        int r = 0;
        grid.add(new Label("姓名*"), 0, r);    grid.add(tfName, 1, r++);
        grid.add(new Label("电话*"), 0, r);    grid.add(tfPhone, 1, r++);
        grid.add(new Label("楼栋*"), 0, r);    grid.add(tfBuilding, 1, r++);
        grid.add(new Label("单元*"), 0, r);    grid.add(tfUnit, 1, r++);

        dialog.getDialogPane().setContent(grid);

        Node okBtn = dialog.getDialogPane().lookupButton(okType);
        okBtn.setDisable(true);

        Runnable validator = () -> okBtn.setDisable(
                tfName.getText().trim().isEmpty()
                        || tfPhone.getText().trim().isEmpty()
                        || tfBuilding.getText().trim().isEmpty()
                        || tfUnit.getText().trim().isEmpty()
        );

        tfName.textProperty().addListener((a,b,c) -> validator.run());
        tfPhone.textProperty().addListener((a,b,c) -> validator.run());
        tfBuilding.textProperty().addListener((a,b,c) -> validator.run());
        tfUnit.textProperty().addListener((a,b,c) -> validator.run());

        validator.run();

        dialog.setResultConverter(btn -> {
            if (btn != okType) return null;

            Owner o = new Owner();
            o.setName(tfName.getText().trim());
            o.setPhone(tfPhone.getText().trim());
            o.setBuilding(tfBuilding.getText().trim());
            o.setUnit(tfUnit.getText().trim());
            return o;
        });

        return dialog.showAndWait();
    }

    // ---------------- Alerts ----------------

    private void alertInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        UiStyler.applyDialogStyles(a.getDialogPane());
        a.showAndWait();
    }

    private void alertWarn(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        UiStyler.applyDialogStyles(a.getDialogPane());
        a.showAndWait();
    }

    private void alertError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        UiStyler.applyDialogStyles(a.getDialogPane());
        a.showAndWait();
    }
}
