package com.railpantry.screens;

import com.railpantry.SessionStore;
import com.railpantry.SessionStore.InventoryItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryScreen extends BorderPane {

    private TableView<InventoryItem> table;
    private FilteredList<InventoryItem> filteredData;
    private Label expiryBanner;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm | dd MMM");

    public InventoryScreen() {
        getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        // ── TOP ──────────────────────────────────────────────────────────────
        VBox topBox = new VBox(14);

        // Expiry Warning Banner (hidden until needed)
        expiryBanner = new Label();
        expiryBanner.setMaxWidth(Double.MAX_VALUE);
        expiryBanner.setPadding(new Insets(12, 20, 12, 20));
        expiryBanner.getStyleClass().add("banner-warning");
        expiryBanner.setVisible(false);
        expiryBanner.setManaged(false);

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(5);
        Label headerTitle = new Label("Active Journey Inventory");
        headerTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
        Label subTitle = new Label("Live onboard stock for " + SessionStore.get().getTrainName() + ".");
        subTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        titleBox.getChildren().addAll(headerTitle, subTitle);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        header.getChildren().addAll(titleBox, headerSpacer);

        // Toolbar
        HBox toolBar = new HBox(15);
        toolBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search item or category...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-padding: 10px; -fx-background-radius: 6px;" +
                " -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-background-color: #ffffff;");

        Button refreshBtn = new Button("↺ Refresh");
        refreshBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-font-weight: bold;" +
                " -fx-padding: 10px 15px; -fx-background-radius: 6px; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> refreshTable());

        Button addBtn = new Button("+ Load New Item");
        addBtn.getStyleClass().add("btn-primary");

        toolBar.getChildren().addAll(searchField, refreshBtn, addBtn);
        topBox.getChildren().addAll(expiryBanner, header, toolBar);
        setTop(topBox);

        // ── CENTER: Table ─────────────────────────────────────────────────────
        table = new TableView<>();
        table.getStyleClass().add("table-view");
        BorderPane.setMargin(table, new Insets(20, 20, 0, 0));

        // Name column
        TableColumn<InventoryItem, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().name));
        nameCol.setPrefWidth(240);

        // Category column
        TableColumn<InventoryItem, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().category));
        catCol.setPrefWidth(110);

        // Qty column
        TableColumn<InventoryItem, String> qtyCol = new TableColumn<>("Current Qty");
        qtyCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().qty + " " + d.getValue().unit));
        qtyCol.setPrefWidth(110);

        // Expiry column
        TableColumn<InventoryItem, String> expCol = new TableColumn<>("Expiry Time");
        expCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().expiry.format(FMT)));
        expCol.setPrefWidth(150);

        // Status Pill column — custom cell
        TableColumn<InventoryItem, String> statusCol = new TableColumn<>("Health Status");
        statusCol.setPrefWidth(150);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                InventoryItem inv = (InventoryItem) getTableRow().getItem();
                setGraphic(buildStatusPill(inv));
            }
        });
        // Provide a dummy value so cell factory triggers
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().name));

        table.getColumns().addAll(nameCol, catCol, qtyCol, expCol, statusCol);

        // Bind to SessionStore inventory
        filteredData = new FilteredList<>(SessionStore.get().getInventory(), p -> true);

        searchField.textProperty().addListener((obs, oldV, newV) ->
                filteredData.setPredicate(item -> {
                    if (newV == null || newV.isEmpty()) return true;
                    String lower = newV.toLowerCase();
                    return item.name.toLowerCase().contains(lower) || item.category.toLowerCase().contains(lower);
                }));

        SortedList<InventoryItem> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // Row factory — alternate & highlight rows
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-expired", "row-expiring-soon", "row-low-stock");
                if (item == null || empty) {
                    setStyle("");
                } else {
                    LocalDateTime now = LocalDateTime.now();
                    long hrs = ChronoUnit.HOURS.between(now, item.expiry);
                    if (item.expiry.isBefore(now)) {
                        getStyleClass().add("row-expired");
                    } else if (hrs >= 0 && hrs < 4) {
                        getStyleClass().add("row-expiring-soon");
                    } else if (item.qty <= item.threshold) {
                        getStyleClass().add("row-low-stock");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        setCenter(table);

        // Add listener to auto-refresh when inventory changes (e.g. from Admin)
        SessionStore.get().getInventory().addListener((javafx.collections.ListChangeListener<InventoryItem>) c -> {
            javafx.application.Platform.runLater(this::refreshTable);
        });

        // ── RIGHT: Quick-Action Sidebar ───────────────────────────────────────
        VBox rightBox = new VBox(15);
        rightBox.setPadding(new Insets(20, 0, 0, 0));
        rightBox.setPrefWidth(230);

        Label actionsTitle = new Label("Quick Actions");
        actionsTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #475569; -fx-font-size: 14px;");

        Button consumeBtn = createActionBtn("📋 Log Consumption", "-fx-background-color: #3b82f6; -fx-text-fill: white;");
        Button wasteBtn   = createActionBtn("🗑 Log Waste",        "-fx-background-color: #ef4444; -fx-text-fill: white;");
        Button editBtn    = createActionBtn("✏ Edit Stock Target", "-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-border-color: #e2e8f0; -fx-border-width: 1px;");

        consumeBtn.setOnAction(e -> handleLogConsumption());
        wasteBtn.setOnAction(e  -> handleLogWaste());
        editBtn.setOnAction(e   -> handleEditTarget());

        // Legend
        VBox legend = new VBox(8);
        legend.setPadding(new Insets(20, 0, 0, 0));
        label(legend, "🟥  Expired",     "#be123c");
        label(legend, "🟧  Expiring <4h","#c2410c");
        label(legend, "🟨  Low Stock",   "#a16207");
        label(legend, "🟩  In Stock",    "#047857");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        rightBox.getChildren().addAll(actionsTitle, consumeBtn, wasteBtn, editBtn, spacer, legend);
        setRight(rightBox);

        // Initial expiry check
        refreshTable();
    }

    // ── Status Pill Node ─────────────────────────────────────────────────────
    private HBox buildStatusPill(InventoryItem item) {
        LocalDateTime now = LocalDateTime.now();
        long hrs = ChronoUnit.HOURS.between(now, item.expiry);

        String text;
        String bg;
        if (item.expiry.isBefore(now)) {
            text = "Expired";  bg = "#fecdd3";
        } else if (hrs >= 0 && hrs < 4) {
            text = "Expiring"; bg = "#ffedd5";
        } else if (item.qty <= item.threshold) {
            text = "Low";      bg = "#fef9c3";
        } else {
            text = "In Stock"; bg = "#d1fae5";
        }

        String textColor = switch (text) {
            case "Expired"  -> "#be123c";
            case "Expiring" -> "#c2410c";
            case "Low"      -> "#a16207";
            default         -> "#047857";
        };

        Label pill = new Label(text);
        pill.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + textColor + ";" +
                " -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 3px 12px;" +
                " -fx-background-radius: 20px;");

        HBox box = new HBox(pill);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    // ── Action Handlers ──────────────────────────────────────────────────────
    private void handleLogConsumption() {
        InventoryItem sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Select an item first."); return; }

        Dialog<Integer> dlg = new Dialog<>();
        dlg.setTitle("Log Consumption");
        dlg.setHeaderText("Consuming: " + sel.name + " (Current: " + sel.qty + " " + sel.unit + ")");

        ButtonType saveType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        Spinner<Integer> spinner = new Spinner<>(1, Math.max(1, sel.qty), 1);
        spinner.setEditable(true);
        spinner.setPrefWidth(150);
        dlg.getDialogPane().setContent(new VBox(10, new Label("Quantity consumed:"), spinner));
        dlg.setResultConverter(btn -> btn == saveType ? spinner.getValue() : null);
        dlg.showAndWait().ifPresent(qty -> {
            SessionStore.get().deductInventory(sel.name, qty);
            table.refresh();
            refreshTable();
            info("Logged consumption of " + qty + " × " + sel.name);
        });
    }

    private void handleLogWaste() {
        InventoryItem sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Select an item first."); return; }

        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Log Waste");
        dlg.setHeaderText("Waste entry for: " + sel.name);

        ButtonType saveType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        Spinner<Integer> qtySpinner = new Spinner<>(1, Math.max(1, sel.qty), 1);
        qtySpinner.setEditable(true);
        ComboBox<String> reasonBox = new ComboBox<>(FXCollections.observableArrayList(
                "Expired", "Spoiled", "Overcooked", "Passenger Return", "Dropped / Spilled"));
        reasonBox.getSelectionModel().selectFirst();
        reasonBox.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(12);
        grid.add(new Label("Quantity:"), 0, 0); grid.add(qtySpinner, 1, 0);
        grid.add(new Label("Reason:"),   0, 1); grid.add(reasonBox,  1, 1);
        dlg.getDialogPane().setContent(grid);

        dlg.setResultConverter(btn -> {
            if (btn == saveType) {
                int cost = qtySpinner.getValue() * sel.pricePerUnit;
                SessionStore.get().addWasteEntry(
                        new SessionStore.WasteEntry(sel.name, qtySpinner.getValue(),
                                reasonBox.getValue(), "", cost));
                table.refresh();
                refreshTable();
                info("Waste logged: " + qtySpinner.getValue() + " × " + sel.name +
                        " [" + reasonBox.getValue() + "] Cost: ₹" + cost);
            }
            return null;
        });
        dlg.show();
    }

    private void handleEditTarget() {
        InventoryItem sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Select an item first."); return; }
        TextInputDialog d = new TextInputDialog(String.valueOf(sel.threshold));
        d.setTitle("Edit Stock Threshold");
        d.setHeaderText("Set new low-stock warning threshold for: " + sel.name);
        d.setContentText("New threshold:");
        d.showAndWait().ifPresent(val -> {
            try { sel.threshold = Integer.parseInt(val.trim()); table.refresh(); }
            catch (NumberFormatException ex) { warn("Invalid number."); }
        });
    }

    // ── Expiry Alert Banner ───────────────────────────────────────────────────
    private void refreshTable() {
        table.refresh();
        List<InventoryItem> expiring = SessionStore.get().getInventory().stream()
                .filter(i -> {
                    long hrs = ChronoUnit.HOURS.between(LocalDateTime.now(), i.expiry);
                    return hrs >= 0 && hrs < 4;
                }).collect(Collectors.toList());

        List<InventoryItem> expired = SessionStore.get().getInventory().stream()
                .filter(i -> i.expiry.isBefore(LocalDateTime.now())).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        if (!expired.isEmpty()) {
            sb.append("⛔ EXPIRED (").append(expired.size()).append("): ");
            sb.append(expired.stream().map(i -> i.name).collect(Collectors.joining(", ")));
            sb.append("   ");
        }
        if (!expiring.isEmpty()) {
            sb.append("⚠ Expiring soon (<4 h) (").append(expiring.size()).append("): ");
            sb.append(expiring.stream().map(i -> i.name).collect(Collectors.joining(", ")));
        }

        boolean show = !expired.isEmpty() || !expiring.isEmpty();
        expiryBanner.setVisible(show);
        expiryBanner.setManaged(show);
        if (show) expiryBanner.setText(sb.toString());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private Button createActionBtn(String text, String style) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(style + " -fx-font-weight: bold; -fx-padding: 12px; -fx-background-radius: 6px; -fx-cursor: hand;");
        return btn;
    }

    private void label(VBox box, String text, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:" + color + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        box.getChildren().add(l);
    }

    private void warn(String msg) { new Alert(Alert.AlertType.WARNING, msg).show(); }
    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText("Done");
        a.show();
    }
}
