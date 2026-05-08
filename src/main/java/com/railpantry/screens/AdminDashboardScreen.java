package com.railpantry.screens;

import com.railpantry.SessionStore;
import com.railpantry.SessionStore.StaffEntry;
import com.railpantry.SessionStore.StationEntry;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardScreen extends BorderPane {

    private TableView<MasterItem> inventoryTable;
    private ObservableList<MasterItem> masterData;
    private List<Button> sidebarButtons = new ArrayList<>();

    private VBox inventoryView;
    private VBox analyticsView;
    private VBox routeView;
    private VBox userRolesView;
    private VBox historyView;

    // Hardcoded admin password (plain text — no hashing per requirement)
    private static final String ADMIN_PASSWORD = "admin123";
    private boolean authenticated = false;

    public AdminDashboardScreen() {
        getStyleClass().add("root");

        // ── Password Gate ─────────────────────────────────────────────────────
        if (!authenticated) {
            PasswordField pwField = new PasswordField();
            pwField.setPromptText("Enter admin password");
            pwField.setMaxWidth(280);
            pwField.setStyle("-fx-padding: 10px; -fx-background-radius: 6px;" +
                    " -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");

            Label errorLbl = new Label();
            errorLbl.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: bold;");
            errorLbl.setVisible(false);

            Button loginBtn = new Button("Unlock Admin Console");
            loginBtn.getStyleClass().add("btn-primary");

            VBox gate = new VBox(16);
            gate.setAlignment(Pos.CENTER);
            gate.setPadding(new Insets(60));

            Label icon  = new Label("🔒");
            icon.setStyle("-fx-font-size: 64px;");
            Label title = new Label("Admin Control Centre");
            title.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: #0f172a;");
            Label hint  = new Label("This area is password protected.");
            hint.setStyle("-fx-text-fill: #64748b;");

            loginBtn.setOnAction(e -> {
                if (ADMIN_PASSWORD.equals(pwField.getText())) {
                    authenticated = true;
                    buildAdminUI();
                } else {
                    errorLbl.setText("⚠ Incorrect password. Try again.");
                    errorLbl.setVisible(true);
                    pwField.clear();
                }
            });
            pwField.setOnAction(e -> loginBtn.fire());

            gate.getChildren().addAll(icon, title, hint, pwField, errorLbl, loginBtn);
            setCenter(gate);
            return;
        }

        buildAdminUI();
    }

    // ============================================================
    // Build full Admin UI after authentication
    // ============================================================
    private void buildAdminUI() {
        // ── Left Sidebar ─────────────────────────────────────────────────────
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(30, 20, 30, 20));
        sidebar.setStyle("-fx-background-color: #1e293b;");
        sidebar.setPrefWidth(220);

        Label brand    = new Label("RAILPANTRY");
        brand.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20px; -fx-font-weight: 900;");
        Label subBrand = new Label("Admin Control");
        subBrand.setStyle("-fx-text-fill: #10b981; -fx-font-size: 12px; -fx-font-weight: bold;");
        VBox logoBox = new VBox(brand, subBrand);
        logoBox.setPadding(new Insets(0, 0, 30, 0));

        sidebar.getChildren().add(logoBox);

        Button btnInventory = createSidebarButton("📦 Master Inventory");
        Button btnRoute     = createSidebarButton("🗺 Route & Journey");
        Button btnUsers     = createSidebarButton("👤 User Roles");
        Button btnHistory   = createSidebarButton("📜 Journey History");
        Button btnAnalytics = createSidebarButton("📊 System Analytics");

        sidebarButtons.addAll(List.of(btnInventory, btnRoute, btnUsers, btnHistory, btnAnalytics));
        sidebar.getChildren().addAll(btnInventory, btnRoute, btnUsers, btnHistory, btnAnalytics);

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Button logoutBtn = new Button("Secure Logout");
        logoutBtn.getStyleClass().add("btn-danger");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Log out of Admin Console?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) com.railpantry.Main.navigateTo(new LoginScreen());
            });
        });
        sidebar.getChildren().add(logoutBtn);
        setLeft(sidebar);

        // ── Build Views ───────────────────────────────────────────────────────
        initInventoryView();
        initAnalyticsView();
        initRouteView();
        initUserRolesView();
        initHistoryView();

        btnInventory.setOnAction(e -> navigateTo(btnInventory, inventoryView));
        btnRoute.setOnAction(e     -> navigateTo(btnRoute,     routeView));
        btnUsers.setOnAction(e     -> navigateTo(btnUsers,     userRolesView));
        btnHistory.setOnAction(e   -> navigateTo(btnHistory,   historyView));
        btnAnalytics.setOnAction(e -> navigateTo(btnAnalytics, analyticsView));

        navigateTo(btnInventory, inventoryView);
    }

    private void navigateTo(Button activeBtn, Node view) {
        for (Button btn : sidebarButtons) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: 600;");
            btn.setOnMouseEntered(e -> { if (btn.getStyle().contains("transparent"))
                btn.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 6px;"); });
            btn.setOnMouseExited(e -> { if (!btn.getStyle().contains("#3b82f6"))
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: 600;"); });
        }
        activeBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px;");
        activeBtn.setOnMouseEntered(null); activeBtn.setOnMouseExited(null);
        setCenter(view);
    }

    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(12, 10, 12, 15));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: 600;");
        return btn;
    }

    // ==============================================================================
    // VIEW 1: Master Inventory — full CRUD
    // ==============================================================================
    private void initInventoryView() {
        inventoryView = new VBox(20);
        inventoryView.setPadding(new Insets(30, 40, 30, 40));

        Label headerTitle = new Label("Master Inventory Database");
        headerTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
        Label subTitle = new Label("Add, remove, or price menu items visible across all journeys.");
        subTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        VBox headerBox = new VBox(5, headerTitle, subTitle);

        HBox toolBar = new HBox(15);
        toolBar.setAlignment(Pos.CENTER_LEFT);
        toolBar.setPadding(new Insets(10, 0, 10, 0));

        TextField searchField = new TextField();
        searchField.setPromptText("Search by ID or Name...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-padding: 8px; -fx-background-radius: 6px;" +
                " -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-background-color: #ffffff;");

        Region toolSpacer = new Region(); HBox.setHgrow(toolSpacer, Priority.ALWAYS);

        Button addBtn    = new Button("+ Add New Item");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showAddItemDialog());

        Button deleteBtn = new Button("Remove Selected");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setOnAction(e -> deleteSelectedItem());

        Button csvBtn = new Button("⬇ Export CSV Audit");
        csvBtn.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; -fx-font-weight: bold;" +
                " -fx-padding: 10px 16px; -fx-background-radius: 6px; -fx-cursor: hand;");
        csvBtn.setOnAction(e -> exportCSV());

        toolBar.getChildren().addAll(searchField, toolSpacer, csvBtn, addBtn, deleteBtn);

        inventoryTable = new TableView<>();
        inventoryTable.getStyleClass().add("table-view");
        VBox.setVgrow(inventoryTable, Priority.ALWAYS);

        TableColumn<MasterItem, String> idCol = new TableColumn<>("Item ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id")); idCol.setPrefWidth(100);

        TableColumn<MasterItem, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name")); nameCol.setPrefWidth(260);

        TableColumn<MasterItem, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category")); catCol.setPrefWidth(130);

        TableColumn<MasterItem, Integer> priceCol = new TableColumn<>("Base Price (₹)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price")); priceCol.setPrefWidth(120);

        TableColumn<MasterItem, Integer> threshCol = new TableColumn<>("Low Stock Warn");
        threshCol.setCellValueFactory(new PropertyValueFactory<>("threshold")); threshCol.setPrefWidth(130);

        TableColumn<MasterItem, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(120);
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                String bg = item.equals("ACTIVE") ? "#d1fae5" : "#fecdd3";
                String fg = item.equals("ACTIVE") ? "#047857" : "#be123c";
                Label pill = new Label(item);
                pill.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + fg +
                        "; -fx-font-weight: bold; -fx-padding: 2px 10px; -fx-background-radius: 20px;");
                setGraphic(pill); setText(null);
            }
        });

        inventoryTable.getColumns().addAll(idCol, nameCol, catCol, priceCol, threshCol, statusCol);

        // ── Build masterData from SessionStore.inventory (no hardcoding) ─────
        masterData = FXCollections.observableArrayList();
        for (SessionStore.InventoryItem inv : SessionStore.get().getInventory()) {
            masterData.add(new MasterItem(
                    inv.id, inv.name, inv.category,
                    inv.pricePerUnit, inv.threshold, "ACTIVE"));
        }

        // Keep in sync: if inventory is updated elsewhere, rebuild the master list
        SessionStore.get().getInventory().addListener(
                (javafx.collections.ListChangeListener<SessionStore.InventoryItem>) c -> {
                    masterData.clear();
                    for (SessionStore.InventoryItem inv : SessionStore.get().getInventory()) {
                        masterData.add(new MasterItem(
                                inv.id, inv.name, inv.category,
                                inv.pricePerUnit, inv.threshold, "ACTIVE"));
                    }
                });

        // Search filter
        javafx.collections.transformation.FilteredList<MasterItem> filtered =
                new javafx.collections.transformation.FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, o, n) ->
                filtered.setPredicate(item -> n == null || n.isEmpty()
                        || item.getName().toLowerCase().contains(n.toLowerCase())
                        || item.getId().toLowerCase().contains(n.toLowerCase())));

        inventoryTable.setItems(filtered);
        inventoryView.getChildren().addAll(headerBox, toolBar, inventoryTable);
    }

    // ==============================================================================
    // VIEW 2: System Analytics — all data from SessionStore, no hardcoding
    // ==============================================================================
    private void initAnalyticsView() {
        analyticsView = new VBox(20);
        analyticsView.setPadding(new Insets(30, 40, 30, 40));

        Label headerTitle = new Label("System Analytics & Health");
        headerTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
        Label subTitle = new Label("Live consumption trends derived from real passenger orders and waste logs.");
        subTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        VBox headerBox = new VBox(5, headerTitle, subTitle);

        // ── Empty-state banner shown when no orders exist yet ─────────────────
        Label emptyBanner = new Label(
            "📭  No orders recorded yet. Add orders via Passenger Tracker to populate these charts.");
        emptyBanner.setStyle(
            "-fx-background-color: #eff6ff; -fx-text-fill: #1d4ed8; -fx-font-weight: bold;" +
            "-fx-padding: 14px 20px; -fx-background-radius: 8px; -fx-font-size: 13px;");
        emptyBanner.setWrapText(true);
        emptyBanner.setMaxWidth(Double.MAX_VALUE);
        emptyBanner.setVisible(SessionStore.get().getOrders().isEmpty());
        emptyBanner.setManaged(SessionStore.get().getOrders().isEmpty());

        // ── Pie Chart — Category Revenue ─────────────────────────────────────
        PieChart pieChart = new PieChart();
        pieChart.setAnimated(true);
        pieChart.setTitle("Revenue by Meal Category (Live)");
        pieChart.setPrefHeight(400);
        pieChart.setPrefWidth(420);
        pieChart.setStyle("-fx-background-color: white; -fx-background-radius: 12px;" +
                " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        // ── Bar Chart — Orders per Item ───────────────────────────────────────
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        xAxis.setLabel("Item"); yAxis.setLabel("Orders");
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setAnimated(true);
        barChart.setTitle("Orders per Menu Item (Live)");
        barChart.setPrefHeight(400);
        HBox.setHgrow(barChart, Priority.ALWAYS);
        barChart.setStyle("-fx-background-color: white; -fx-background-radius: 12px;" +
                " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
        barSeries.setName("Orders");
        barChart.getData().add(barSeries);

        // ── Helper: populate both charts from live data ───────────────────────
        Runnable refreshCharts = () -> {
            // PieChart
            pieChart.getData().clear();
            java.util.Map<String, Double> catRev = SessionStore.get().getMealCategoryRevenue();
            if (catRev.isEmpty()) {
                pieChart.getData().add(new PieChart.Data("No Orders Yet", 1));
            } else {
                catRev.forEach((cat, rev) ->
                        pieChart.getData().add(new PieChart.Data(cat + " (₹" + rev.intValue() + ")", rev)));
            }

            // BarChart
            barSeries.getData().clear();
            java.util.Map<String, Integer> perItem = SessionStore.get().getOrdersPerItem();
            // Show top 8 items by order count, trimming name to first word
            perItem.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .limit(8)
                    .forEach(e -> barSeries.getData().add(
                            new XYChart.Data<>(e.getKey().split(" ")[0], e.getValue())));

            // Toggle empty-state banner
            boolean empty = SessionStore.get().getOrders().isEmpty();
            emptyBanner.setVisible(empty);
            emptyBanner.setManaged(empty);

            // Reattach tooltips after data refresh
            Platform.runLater(() -> {
                for (PieChart.Data data : pieChart.getData()) {
                    Node node = data.getNode();
                    if (node == null) continue;
                    Tooltip t = new Tooltip(data.getName());
                    Tooltip.install(node, t);
                    node.setOnMouseEntered(e -> { node.setTranslateY(-5); node.setStyle("-fx-cursor: hand; -fx-opacity: 0.8;"); });
                    node.setOnMouseExited(e  -> { node.setTranslateY(0);  node.setStyle(""); });
                }
                for (XYChart.Data<String, Number> data : barSeries.getData()) {
                    Node node = data.getNode();
                    if (node == null) continue;
                    Tooltip.install(node, new Tooltip(data.getXValue() + ": " + data.getYValue() + " orders"));
                    node.setOnMouseEntered(e -> { node.setStyle("-fx-cursor: hand; -fx-opacity: 0.8;"); node.setTranslateY(-4); });
                    node.setOnMouseExited(e  -> { node.setStyle(""); node.setTranslateY(0); });
                }
            });
        };

        // Initial load
        refreshCharts.run();

        // ── Auto-refresh when orders list changes ─────────────────────────────
        SessionStore.get().getOrders().addListener(
                (javafx.collections.ListChangeListener<SessionStore.OrderEntry>) c ->
                        Platform.runLater(refreshCharts));

        // Also refresh when waste log changes (affects category labels indirectly)
        SessionStore.get().getWasteLog().addListener(
                (javafx.collections.ListChangeListener<SessionStore.WasteEntry>) c ->
                        Platform.runLater(refreshCharts));

        HBox chartsBox = new HBox(20, pieChart, barChart);
        chartsBox.setPadding(new Insets(10, 0, 10, 0));
        VBox.setVgrow(chartsBox, Priority.ALWAYS);

        analyticsView.getChildren().addAll(headerBox, emptyBanner, chartsBox);
    }

    // ==============================================================================
    // VIEW 3: Route & Journey — real CRUD
    // ==============================================================================
    private void initRouteView() {
        routeView = new VBox(20);
        routeView.setPadding(new Insets(30, 40, 30, 40));

        Label title    = new Label("Route & Journey Configurations");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
        Label subtitle = new Label("Manage active train routes, halt stations, and restock flags.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");

        HBox toolBar = new HBox(12);
        toolBar.setAlignment(Pos.CENTER_LEFT);
        Button addStationBtn = new Button("+ Add Station");
        addStationBtn.getStyleClass().add("btn-primary");
        Button removeStationBtn = new Button("Remove Selected");
        removeStationBtn.getStyleClass().add("btn-danger");
        toolBar.getChildren().addAll(addStationBtn, removeStationBtn);

        TableView<StationEntry> stTable = new TableView<>();
        stTable.getStyleClass().add("table-view");
        VBox.setVgrow(stTable, Priority.ALWAYS);
        stTable.setItems(SessionStore.get().getStations());

        TableColumn<StationEntry, String> nameCol = new TableColumn<>("Station Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));
        nameCol.setPrefWidth(200);

        TableColumn<StationEntry, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().code));
        codeCol.setPrefWidth(80);

        TableColumn<StationEntry, String> etaCol = new TableColumn<>("ETA");
        etaCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().eta));
        etaCol.setPrefWidth(90);

        TableColumn<StationEntry, String> restockCol = new TableColumn<>("Restocking");
        restockCol.setPrefWidth(130);
        restockCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().supportsRestocking ? "YES" : "NO"));
        restockCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label pill = new Label(item);
                pill.setStyle(item.equals("YES")
                        ? "-fx-background-color: #d1fae5; -fx-text-fill: #047857; -fx-font-weight: bold; -fx-padding: 2px 10px; -fx-background-radius: 20px;"
                        : "-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8; -fx-padding: 2px 10px; -fx-background-radius: 20px;");
                setGraphic(pill); setText(null);
            }
        });

        TableColumn<StationEntry, String> passedCol = new TableColumn<>("Status");
        passedCol.setPrefWidth(110);
        passedCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().passed ? "PASSED" : "UPCOMING"));

        stTable.getColumns().addAll(nameCol, codeCol, etaCol, restockCol, passedCol);

        addStationBtn.setOnAction(e -> showAddStationDialog());
        removeStationBtn.setOnAction(e -> {
            StationEntry sel = stTable.getSelectionModel().getSelectedItem();
            if (sel == null) { new Alert(Alert.AlertType.WARNING, "Select a station first.").show(); return; }
            SessionStore.get().getStations().remove(sel);
        });

        routeView.getChildren().addAll(new VBox(5, title, subtitle), toolBar, stTable);
    }

    private void showAddStationDialog() {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Add New Station");
        dlg.setHeaderText("Add a halt to the current route");
        ButtonType saveType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField nameF = new TextField(); nameF.setPromptText("Station Name");
        TextField codeF = new TextField(); codeF.setPromptText("Code");
        TextField etaF  = new TextField(); etaF.setPromptText("HH:mm");
        CheckBox restockChk = new CheckBox("Supports Restocking");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(12); grid.setPadding(new Insets(20, 100, 10, 10));
        grid.add(new Label("Name:"),   0, 0); grid.add(nameF, 1, 0);
        grid.add(new Label("Code:"),   0, 1); grid.add(codeF, 1, 1);
        grid.add(new Label("ETA:"),    0, 2); grid.add(etaF,  1, 2);
        grid.add(restockChk,           1, 3);
        dlg.getDialogPane().setContent(grid);

        dlg.setResultConverter(btn -> {
            if (btn == saveType && !nameF.getText().isBlank()) {
                SessionStore.get().getStations().add(new StationEntry(
                        nameF.getText(), codeF.getText(), etaF.getText(),
                        restockChk.isSelected(), false));
            }
            return null;
        });
        dlg.show();
    }

    // ==============================================================================
    // VIEW 4: User Roles — real management
    // ==============================================================================
    private void initUserRolesView() {
        userRolesView = new VBox(20);
        userRolesView.setPadding(new Insets(30, 40, 30, 40));

        Label title    = new Label("System User Roles & Access");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
        Label subtitle = new Label("Manage active pantry manager and staff credentials.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");

        HBox toolBar = new HBox(12);
        toolBar.setAlignment(Pos.CENTER_LEFT);
        Button addUserBtn    = new Button("+ Add Staff");
        addUserBtn.getStyleClass().add("btn-primary");
        Button toggleBtn     = new Button("Toggle Active/Inactive");
        toggleBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #334155; -fx-font-weight: bold;" +
                " -fx-padding: 10px 16px; -fx-background-radius: 6px; -fx-cursor: hand;");
        Button resetPwBtn    = new Button("Reset Password");
        resetPwBtn.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-font-weight: bold;" +
                " -fx-padding: 10px 16px; -fx-background-radius: 6px; -fx-cursor: hand;");
        toolBar.getChildren().addAll(addUserBtn, toggleBtn, resetPwBtn);

        TableView<StaffEntry> staffTable = new TableView<>();
        staffTable.getStyleClass().add("table-view");
        VBox.setVgrow(staffTable, Priority.ALWAYS);
        staffTable.setItems(SessionStore.get().getStaff());

        TableColumn<StaffEntry, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));
        nameCol.setPrefWidth(200);

        TableColumn<StaffEntry, String> idCol = new TableColumn<>("Employee ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().empId));
        idCol.setPrefWidth(120);

        TableColumn<StaffEntry, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().role));
        roleCol.setPrefWidth(160);

        TableColumn<StaffEntry, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(130);
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                String bg = item.equals("ACTIVE") ? "#d1fae5" : "#fecdd3";
                String fg = item.equals("ACTIVE") ? "#047857" : "#be123c";
                Label pill = new Label(item);
                pill.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + fg +
                        "; -fx-font-weight: bold; -fx-padding: 2px 10px; -fx-background-radius: 20px;");
                setGraphic(pill); setText(null);
            }
        });

        staffTable.getColumns().addAll(nameCol, idCol, roleCol, statusCol);

        addUserBtn.setOnAction(e -> showAddUserDialog());
        toggleBtn.setOnAction(e -> {
            StaffEntry sel = staffTable.getSelectionModel().getSelectedItem();
            if (sel == null) { new Alert(Alert.AlertType.WARNING, "Select a user first.").show(); return; }
            sel.status = sel.status.equals("ACTIVE") ? "INACTIVE" : "ACTIVE";
            staffTable.refresh();
        });
        resetPwBtn.setOnAction(e -> {
            StaffEntry sel = staffTable.getSelectionModel().getSelectedItem();
            if (sel == null) { new Alert(Alert.AlertType.WARNING, "Select a user first.").show(); return; }
            new Alert(Alert.AlertType.INFORMATION,
                    "Password for " + sel.name + " has been reset to: railpantry@123\n(Change on next login.)",
                    ButtonType.OK).show();
        });

        userRolesView.getChildren().addAll(new VBox(5, title, subtitle), toolBar, staffTable);
    }

    private void showAddUserDialog() {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Add Staff Member");
        dlg.setHeaderText("Register a new user");
        ButtonType saveType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField nameF  = new TextField(); nameF.setPromptText("Full Name");
        TextField idF    = new TextField(); idF.setPromptText("e.g. XX-001");
        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList(
                "Pantry Manager", "Pantry Staff", "Administrator"));
        roleBox.getSelectionModel().select("Pantry Staff");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(12); grid.setPadding(new Insets(20, 100, 10, 10));
        grid.add(new Label("Name:"),       0, 0); grid.add(nameF,   1, 0);
        grid.add(new Label("Employee ID:"),0, 1); grid.add(idF,     1, 1);
        grid.add(new Label("Role:"),       0, 2); grid.add(roleBox, 1, 2);
        dlg.getDialogPane().setContent(grid);

        dlg.setResultConverter(btn -> {
            if (btn == saveType && !nameF.getText().isBlank()) {
                SessionStore.get().getStaff().add(new StaffEntry(
                        nameF.getText(), idF.getText(), roleBox.getValue(), "ACTIVE"));
            }
            return null;
        });
        dlg.show();
    }

    // ==============================================================================
    // CSV Export
    // ==============================================================================
    private void exportCSV() {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Export Master Audit Trail");
        chooser.setInitialFileName("MasterAudit_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMMyyyy")) + ".csv");
        chooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        java.io.File file = chooser.showSaveDialog(getScene().getWindow());
        if (file == null) return;

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("ItemID,ItemName,Category,BasePrice,LowStockThreshold,Status");
            for (MasterItem item : masterData) {
                pw.printf("%s,%s,%s,%d,%d,%s%n",
                        item.getId(), item.getName(), item.getCategory(),
                        item.getPrice(), item.getThreshold(), item.getStatus());
            }
            Alert ok = new Alert(Alert.AlertType.INFORMATION,
                    "CSV exported to:\n" + file.getAbsolutePath(), ButtonType.OK);
            ok.setHeaderText("Export Successful ✔");
            ok.show();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).show();
        }
    }

    // ==============================================================================
    // Add/Remove Inventory Dialogs
    // ==============================================================================
    private void showAddItemDialog() {
        Dialog<MasterItem> dialog = new Dialog<>();
        dialog.setTitle("Add New Menu Item");
        dialog.setHeaderText("Create a new item in the global dictionary.");

        ButtonType saveType = new ButtonType("Save to Database", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(15); grid.setPadding(new Insets(20, 150, 10, 10));

        TextField name      = new TextField(); name.setPromptText("e.g. Tomato Soup");
        ComboBox<String> category = new ComboBox<>(FXCollections.observableArrayList("Hot Meal", "Beverage", "Snack"));
        category.getSelectionModel().selectFirst();
        TextField price     = new TextField(); price.setPromptText("₹");
        TextField threshold = new TextField(); threshold.setPromptText("Qty");

        grid.add(new Label("Item Name:"),     0, 0); grid.add(name,      1, 0);
        grid.add(new Label("Category:"),      0, 1); grid.add(category,  1, 1);
        grid.add(new Label("Base Price:"),    0, 2); grid.add(price,     1, 2);
        grid.add(new Label("Low Stock Warn:"),0, 3); grid.add(threshold, 1, 3);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                try {
                    String itemId = "ITM-" + (500 + (int)(Math.random() * 500));
                    int priceVal     = Integer.parseInt(price.getText().trim());
                    int thresholdVal = Integer.parseInt(threshold.getText().trim());

                    // Write through to SessionStore.inventory so all screens see the new item
                    SessionStore.get().getInventory().add(new SessionStore.InventoryItem(
                            itemId, name.getText(), category.getValue(),
                            0, "units",
                            java.time.LocalDateTime.now().plusDays(30),
                            thresholdVal, priceVal));

                    return new MasterItem(itemId, name.getText(), category.getValue(),
                            priceVal, thresholdVal, "ACTIVE");
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Invalid number for Price/Threshold.").show();
                }
            }
            return null;
        });
        // masterData is auto-synced via the SessionStore listener — no manual add needed
        dialog.showAndWait();
    }

    private void deleteSelectedItem() {
        MasterItem sel = inventoryTable.getSelectionModel().getSelectedItem();
        if (sel == null) { new Alert(Alert.AlertType.WARNING, "Select an item first.").show(); return; }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Remove [" + sel.getName() + "] from the master database?",
                ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Remove Item");
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                // Remove from SessionStore.inventory (masterData auto-syncs via listener)
                SessionStore.get().getInventory().removeIf(i -> i.id.equals(sel.getId()));
            }
        });
    }

    // ==============================================================================
    // VIEW 5: Journey History — persistent logs from SessionStore
    // ==============================================================================
    private void initHistoryView() {
        historyView = new VBox(20);
        historyView.setPadding(new Insets(30, 40, 30, 40));

        Label headerTitle = new Label("Global Journey History");
        headerTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
        Label subTitle = new Label("Review past performance, sales audits, and waste reconciliation across all recorded trips.");
        subTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        VBox headerBox = new VBox(5, headerTitle, subTitle);

        TableView<SessionStore.JourneyHistoryEntry> historyTable = new TableView<>();
        historyTable.getStyleClass().add("table-view");
        VBox.setVgrow(historyTable, Priority.ALWAYS);
        historyTable.setItems(SessionStore.get().getJourneyHistory());

        TableColumn<SessionStore.JourneyHistoryEntry, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().date.format(DateTimeFormatter.ofPattern("dd MMM HH:mm"))));
        dateCol.setPrefWidth(120);

        TableColumn<SessionStore.JourneyHistoryEntry, String> trainCol = new TableColumn<>("Train");
        trainCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().trainName));
        trainCol.setPrefWidth(180);

        TableColumn<SessionStore.JourneyHistoryEntry, String> mgrCol = new TableColumn<>("Manager");
        mgrCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().managerName));
        mgrCol.setPrefWidth(150);

        TableColumn<SessionStore.JourneyHistoryEntry, String> revCol = new TableColumn<>("Revenue (₹)");
        revCol.setCellValueFactory(d -> new SimpleStringProperty("₹ " + d.getValue().totalRevenue));
        revCol.setPrefWidth(110);

        TableColumn<SessionStore.JourneyHistoryEntry, String> wasteCol = new TableColumn<>("Waste %");
        wasteCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.1f%%", d.getValue().wastePct)));
        wasteCol.setPrefWidth(90);

        TableColumn<SessionStore.JourneyHistoryEntry, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(120);
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().wastePct > 10 ? "WARNING" : "SUCCESS"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                String bg = item.equals("SUCCESS") ? "#d1fae5" : "#fee2e2";
                String fg = item.equals("SUCCESS") ? "#047857" : "#991b1b";
                Label pill = new Label(item);
                pill.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + fg +
                        "; -fx-font-weight: bold; -fx-padding: 2px 10px; -fx-background-radius: 20px;");
                setGraphic(pill); setText(null);
            }
        });

        historyTable.getColumns().addAll(dateCol, trainCol, mgrCol, revCol, wasteCol, statusCol);

        // Placeholder for empty table
        Label placeholder = new Label("📜 No completed journeys in history yet.\nComplete an End-of-Journey report to archive data.");
        placeholder.setStyle("-fx-text-fill: #94a3b8; -fx-text-alignment: center; -fx-font-size: 14px;");
        historyTable.setPlaceholder(placeholder);

        historyView.getChildren().addAll(headerBox, historyTable);
    }

    // ==============================================================================
    // Data Model
    // ==============================================================================
    public static class MasterItem {
        private String id, name, category, status;
        private int price, threshold;

        public MasterItem(String id, String name, String category, int price, int threshold, String status) {
            this.id = id; this.name = name; this.category = category;
            this.price = price; this.threshold = threshold; this.status = status;
        }
        public String getId()        { return id; }
        public String getName()      { return name; }
        public String getCategory()  { return category; }
        public int    getPrice()     { return price; }
        public int    getThreshold() { return threshold; }
        public String getStatus()    { return status; }
    }
}
