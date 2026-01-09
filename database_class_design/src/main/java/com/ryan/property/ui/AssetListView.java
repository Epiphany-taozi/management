package com.ryan.property.ui;

import java.util.Optional;

import com.ryan.property.dao.AssetDao;
import com.ryan.property.model.Asset;
import com.ryan.property.ui.UiStyler;

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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class AssetListView extends BorderPane {

    private final AssetDao assetDao = new AssetDao();
    private final ObservableList<Asset> data = FXCollections.observableArrayList();

    private final TableView<Asset> table = new TableView<>(data);
    private final TextField keywordField = new TextField();
    private String currentKeyword = "";

    public AssetListView() {
        setPadding(new Insets(16));
        getStyleClass().add("view-root");

        Text title = new Text("公共财产信息（Assets）");
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

        keywordField.setPromptText("按名称/位置/状态搜索");
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
            TableRow<Asset> row = new TableRow<>();
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

        TableColumn<Asset, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().getId()));

        TableColumn<Asset, String> colName = new TableColumn<>("名称");
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));

        TableColumn<Asset, String> colLocation = new TableColumn<>("位置");
        colLocation.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLocation()));

        TableColumn<Asset, String> colStatus = new TableColumn<>("状态");
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));

        table.getColumns().setAll(colId, colName, colLocation, colStatus);
    }

    private void reload() {
        if (currentKeyword == null || currentKeyword.isBlank()) {
            data.setAll(assetDao.findAll());
        } else {
            data.setAll(assetDao.findByKeyword(currentKeyword));
        }
        System.out.println("[UI] Asset list loaded: " + data.size());
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
        Optional<Asset> opt = showAssetDialog("新增公共财产", null);
        if (opt.isEmpty()) return;

        Asset toInsert = opt.get();
        long newId = assetDao.insert(toInsert);
        if (newId > 0) {
            alertInfo("成功", "已新增公共财产，ID = " + newId);
            reload();
        } else {
            alertError("失败", "新增失败，请查看控制台错误信息。");
        }
    }

    private void onEdit() {
        Asset selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertWarn("提示", "请先在表格中选中一条公共财产记录。");
            return;
        }

        Optional<Asset> opt = showAssetDialog("修改公共财产（ID=" + selected.getId() + "）", selected);
        if (opt.isEmpty()) return;

        Asset edited = opt.get();
        edited.setId(selected.getId());

        boolean ok = assetDao.update(edited);
        if (ok) {
            alertInfo("成功", "已更新公共财产信息。");
            reload();
        } else {
            alertError("失败", "更新失败，请查看控制台错误信息。");
        }
    }

    private void onDelete() {
        Asset selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertWarn("提示", "请先在表格中选中一条公共财产记录。");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认删除");
        confirm.setHeaderText("确定要删除该公共财产吗？");
        confirm.setContentText("ID=" + selected.getId() + "，名称=" + selected.getName());

        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isEmpty() || r.get() != ButtonType.OK) return;

        boolean ok = assetDao.deleteById(selected.getId());
        if (ok) {
            alertInfo("成功", "已删除。");
            reload();
        } else {
            alertError("失败", "删除失败，请查看控制台错误信息。");
        }
    }

    private Optional<Asset> showAssetDialog(String title, Asset origin) {
        Dialog<Asset> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        UiStyler.applyDialogStyles(dialog);

        ButtonType okType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField tfName = new TextField();
        TextField tfLocation = new TextField();
        ComboBox<String> statusBox = new ComboBox<>();
        UiStyler.applyInputStyles(tfName, tfLocation, statusBox);

        tfName.setPromptText("例如：电梯");
        tfLocation.setPromptText("例如：1号楼大厅");
        statusBox.getItems().setAll("正常", "维修中", "停用");
        statusBox.setPromptText("请选择状态");

        if (origin != null) {
            tfName.setText(origin.getName());
            tfLocation.setText(origin.getLocation());
            statusBox.setValue(origin.getStatus());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(14));
        grid.getStyleClass().add("form-grid");

        int r = 0;
        grid.add(new Label("名称*"), 0, r);    grid.add(tfName, 1, r++);
        grid.add(new Label("位置*"), 0, r);    grid.add(tfLocation, 1, r++);
        grid.add(new Label("状态*"), 0, r);    grid.add(statusBox, 1, r++);

        dialog.getDialogPane().setContent(grid);

        Node okBtn = dialog.getDialogPane().lookupButton(okType);
        okBtn.setDisable(true);

        Runnable validator = () -> okBtn.setDisable(
                tfName.getText().trim().isEmpty()
                        || tfLocation.getText().trim().isEmpty()
                        || statusBox.getValue() == null
        );

        tfName.textProperty().addListener((a,b,c) -> validator.run());
        tfLocation.textProperty().addListener((a,b,c) -> validator.run());
        statusBox.valueProperty().addListener((a,b,c) -> validator.run());

        validator.run();

        dialog.setResultConverter(btn -> {
            if (btn != okType) return null;

            Asset a = new Asset();
            a.setName(tfName.getText().trim());
            a.setLocation(tfLocation.getText().trim());
            a.setStatus(statusBox.getValue());
            return a;
        });

        return dialog.showAndWait();
    }

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
