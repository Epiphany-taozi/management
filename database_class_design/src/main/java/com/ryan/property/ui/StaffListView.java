package com.ryan.property.ui;

import java.util.Optional;

import com.ryan.property.dao.StaffDao;
import com.ryan.property.model.Staff;

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
import javafx.scene.text.Text;

public class StaffListView extends BorderPane {

    private final StaffDao staffDao = new StaffDao();
    private final ObservableList<Staff> data = FXCollections.observableArrayList();

    private final TableView<Staff> table = new TableView<>(data);
    private final TextField keywordField = new TextField();
    private String currentKeyword = "";

    public StaffListView() {
        setPadding(new Insets(12));

        Text title = new Text("工作人员信息（Staff）");

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

        keywordField.setPromptText("按姓名/手机号/角色搜索");
        keywordField.setPrefWidth(240);
        keywordField.setOnAction(e -> onSearch());

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

        setTop(toolBar);

        buildTable();
        setCenter(table);

        table.setRowFactory(tv -> {
            TableRow<Staff> row = new TableRow<>();
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

        TableColumn<Staff, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().getId()));

        TableColumn<Staff, String> colName = new TableColumn<>("姓名");
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));

        TableColumn<Staff, String> colRole = new TableColumn<>("角色");
        colRole.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getRole()));

        TableColumn<Staff, String> colPhone = new TableColumn<>("电话");
        colPhone.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPhone()));

        table.getColumns().setAll(colId, colName, colRole, colPhone);
    }

    private void reload() {
        if (currentKeyword == null || currentKeyword.isBlank()) {
            data.setAll(staffDao.findAll());
        } else {
            data.setAll(staffDao.findByKeyword(currentKeyword));
        }
        System.out.println("[UI] Staff list loaded: " + data.size());
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

    private void onAdd() {
        Optional<Staff> opt = showStaffDialog("新增工作人员", null);
        if (opt.isEmpty()) return;

        Staff toInsert = opt.get();
        long newId = staffDao.insert(toInsert);
        if (newId > 0) {
            alertInfo("成功", "已新增工作人员，ID = " + newId);
            reload();
        } else {
            alertError("失败", "新增失败，请查看控制台错误信息。");
        }
    }

    private void onEdit() {
        Staff selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertWarn("提示", "请先在表格中选中一条工作人员记录。");
            return;
        }

        Optional<Staff> opt = showStaffDialog("修改工作人员（ID=" + selected.getId() + "）", selected);
        if (opt.isEmpty()) return;

        Staff edited = opt.get();
        edited.setId(selected.getId());

        boolean ok = staffDao.update(edited);
        if (ok) {
            alertInfo("成功", "已更新工作人员信息。");
            reload();
        } else {
            alertError("失败", "更新失败，请查看控制台错误信息。");
        }
    }

    private void onDelete() {
        Staff selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertWarn("提示", "请先在表格中选中一条工作人员记录。");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认删除");
        confirm.setHeaderText("确定要删除该工作人员吗？");
        confirm.setContentText("ID=" + selected.getId() + "，姓名=" + selected.getName());

        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isEmpty() || r.get() != ButtonType.OK) return;

        boolean ok = staffDao.deleteById(selected.getId());
        if (ok) {
            alertInfo("成功", "已删除。");
            reload();
        } else {
            alertError("失败", "删除失败，请查看控制台错误信息。");
        }
    }

    private Optional<Staff> showStaffDialog(String title, Staff origin) {
        Dialog<Staff> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType okType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField tfName = new TextField();
        TextField tfRole = new TextField();
        TextField tfPhone = new TextField();

        tfName.setPromptText("例如：李四");
        tfRole.setPromptText("例如：保安/保洁");
        tfPhone.setPromptText("例如：13800000000");

        if (origin != null) {
            tfName.setText(origin.getName());
            tfRole.setText(origin.getRole());
            tfPhone.setText(origin.getPhone());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(14));

        int r = 0;
        grid.add(new Label("姓名*"), 0, r);    grid.add(tfName, 1, r++);
        grid.add(new Label("角色*"), 0, r);    grid.add(tfRole, 1, r++);
        grid.add(new Label("电话*"), 0, r);    grid.add(tfPhone, 1, r++);

        dialog.getDialogPane().setContent(grid);

        Node okBtn = dialog.getDialogPane().lookupButton(okType);
        okBtn.setDisable(true);

        Runnable validator = () -> okBtn.setDisable(
                tfName.getText().trim().isEmpty()
                        || tfRole.getText().trim().isEmpty()
                        || tfPhone.getText().trim().isEmpty()
        );

        tfName.textProperty().addListener((a,b,c) -> validator.run());
        tfRole.textProperty().addListener((a,b,c) -> validator.run());
        tfPhone.textProperty().addListener((a,b,c) -> validator.run());

        validator.run();

        dialog.setResultConverter(btn -> {
            if (btn != okType) return null;

            Staff s = new Staff();
            s.setName(tfName.getText().trim());
            s.setRole(tfRole.getText().trim());
            s.setPhone(tfPhone.getText().trim());
            return s;
        });

        return dialog.showAndWait();
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
}
