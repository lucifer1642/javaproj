package com.railpantry.screens;

import com.railpantry.SessionStore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ManagerDashboardScreen extends BorderPane {

    private List<Button> sidebarButtons = new ArrayList<>();

    private Node dashboardView;
    private Node inventoryView;
    private Node haltRestockView;
    private Node passengerTrackerView;
    private Node wasteLoggerView;
    private Node eojReportView;
    private Node historyView;

    public ManagerDashboardScreen() {
        getStyleClass().add("root");

        // ── Left Sidebar ──────────────────────────────────────────────────────
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(30, 20, 30, 20));
        sidebar.setStyle("-fx-background-color: #1e293b;");
        sidebar.setPrefWidth(250);

        Label brand    = new Label("🚄  RAILPANTRY");
        brand.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 18px; -fx-font-weight: 900; -fx-letter-spacing: 1px;");
        Label subBrand = new Label("Manager Terminal");
        subBrand.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-font-weight: bold;");

        // Live train info in sidebar header
        Label trainInfo = new Label(SessionStore.get().getTrainName());
        trainInfo.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        // Try to load IRCTC logo for sidebar
        VBox logoBox;
        InputStream logoStream = getClass().getResourceAsStream("/images/irctc.jpg");
        if (logoStream != null) {
            ImageView sidebarLogo = new ImageView(new Image(logoStream));
            sidebarLogo.setFitWidth(56);
            sidebarLogo.setFitHeight(56);
            sidebarLogo.setPreserveRatio(true);

            // IRCTC badge
            Label irctcBadge = new Label("🇮🇳  IRCTC Certified");
            irctcBadge.setStyle(
                "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #10b981;" +
                "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3px 10px;" +
                "-fx-background-radius: 12px;"
            );
            logoBox = new VBox(6, sidebarLogo, irctcBadge, brand, subBrand, trainInfo);
        } else {
            logoBox = new VBox(3, brand, subBrand, trainInfo);
        }
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPadding(new Insets(0, 0, 25, 0));
        sidebar.getChildren().add(logoBox);

        // Divider
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #334155;");
        sidebar.getChildren().add(sep);
        sidebar.getChildren().add(spacer(10));

        // Navigation Buttons
        Button btnDashboard  = createSidebarButton("🌐  Dashboard Overview");
        Button btnInventory  = createSidebarButton("📦  Active Inventory");
        Button btnHalt       = createSidebarButton("🚉  Halt Restocking");
        Button btnTracker    = createSidebarButton("🪑  Passenger Tracker");
        Button btnWaste      = createSidebarButton("🗑  Waste Logger");
        Button btnEOJ        = createSidebarButton("📊  End-of-Journey Report");
        Button btnHistory    = createSidebarButton("🕛  Past Journey History");

        sidebarButtons.addAll(List.of(btnDashboard, btnInventory, btnHalt, btnTracker, btnWaste, btnEOJ, btnHistory));
        sidebar.getChildren().addAll(btnDashboard, btnInventory, btnHalt, btnTracker, btnWaste, btnEOJ, btnHistory);

        Region grow = new Region(); VBox.setVgrow(grow, Priority.ALWAYS);
        sidebar.getChildren().add(grow);

        // Manager info block
        VBox mgrBox = new VBox(4);
        mgrBox.setPadding(new Insets(10, 5, 15, 5));
        mgrBox.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8px;");
        Label mgrName = new Label("👤 " + SessionStore.get().getManagerName());
        mgrName.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
        Label mgrId   = new Label("ID: " + SessionStore.get().getManagerId());
        mgrId.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        mgrBox.getChildren().addAll(mgrName, mgrId);
        sidebar.getChildren().add(mgrBox);

        Button logoutBtn = new Button("End Shift & Logout");
        logoutBtn.getStyleClass().add("btn-danger");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Log out of the Manager Terminal?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) com.railpantry.Main.navigateTo(new LoginScreen());
            });
        });
        sidebar.getChildren().add(logoutBtn);

        setLeft(sidebar);

        // ── Initialize Views ────────────────────────────────────────────────
        dashboardView       = new DashboardScreen();
        inventoryView       = new InventoryScreen();
        haltRestockView     = new HaltRestockingScreen();
        passengerTrackerView = new PassengerTrackerScreen();
        wasteLoggerView     = new WasteLoggerScreen();
        eojReportView       = new EOJReportScreen();
        initHistoryView();

        // Routing
        btnDashboard.setOnAction(e  -> navigateTo(btnDashboard,  dashboardView));
        btnInventory.setOnAction(e  -> navigateTo(btnInventory,  inventoryView));
        btnHalt.setOnAction(e       -> navigateTo(btnHalt,       haltRestockView));
        btnTracker.setOnAction(e    -> navigateTo(btnTracker,    passengerTrackerView));
        btnWaste.setOnAction(e      -> navigateTo(btnWaste,      wasteLoggerView));
        btnEOJ.setOnAction(e        -> navigateTo(btnEOJ,        eojReportView));
        btnHistory.setOnAction(e    -> navigateTo(btnHistory,    historyView));

        navigateTo(btnDashboard, dashboardView);
    }

    private void navigateTo(Button activeBtn, Node view) {
        for (Button btn : sidebarButtons) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: 600; -fx-font-size: 13px;");
            btn.setOnMouseEntered(e -> {
                if (btn.getStyle().contains("transparent"))
                    btn.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 8px; -fx-font-size: 13px;");
            });
            btn.setOnMouseExited(e -> {
                if (!btn.getStyle().contains("#3b82f6"))
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: 600; -fx-font-size: 13px;");
            });
        }
        activeBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;" +
                " -fx-background-radius: 8px; -fx-font-size: 13px; -fx-border-width: 0 0 0 3px;" +
                " -fx-border-color: #93c5fd; -fx-border-radius: 8px;");
        activeBtn.setOnMouseEntered(null);
        activeBtn.setOnMouseExited(null);
        setCenter(view);
    }

    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(12, 10, 12, 15));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: 600; -fx-font-size: 13px;");
        return btn;
    }

    private void initHistoryView() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30, 40, 30, 40));

        Label headerTitle = new Label("🕛 Past Journey History");
        headerTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");
        Label subTitle = new Label("Review your past trips, sales performance, and waste audits.");
        subTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        VBox headerBox = new VBox(5, headerTitle, subTitle);

        TableView<SessionStore.JourneyHistoryEntry> historyTable = new TableView<>();
        historyTable.getStyleClass().add("table-view");
        VBox.setVgrow(historyTable, Priority.ALWAYS);
        historyTable.setItems(SessionStore.get().getJourneyHistory());

        TableColumn<SessionStore.JourneyHistoryEntry, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().date.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"))));
        dateCol.setPrefWidth(130);

        TableColumn<SessionStore.JourneyHistoryEntry, String> trainCol = new TableColumn<>("Train / Route");
        trainCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().trainName + "\n" + d.getValue().route));
        trainCol.setPrefWidth(240);

        TableColumn<SessionStore.JourneyHistoryEntry, String> revCol = new TableColumn<>("Revenue");
        revCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty("₹ " + d.getValue().totalRevenue));
        revCol.setPrefWidth(120);

        TableColumn<SessionStore.JourneyHistoryEntry, String> wasteCol = new TableColumn<>("Waste %");
        wasteCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.format("%.1f%%", d.getValue().wastePct)));
        wasteCol.setPrefWidth(100);

        TableColumn<SessionStore.JourneyHistoryEntry, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(130);
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().wastePct > 10 ? "WARNING" : "SUCCESS"));
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

        historyTable.getColumns().addAll(dateCol, trainCol, revCol, wasteCol, statusCol);

        // Placeholder
        Label placeholder = new Label("📜 No journey records found.\nArchived data appears here after ending a journey.");
        placeholder.setStyle("-fx-text-fill: #94a3b8; -fx-text-alignment: center; -fx-font-size: 14px;");
        historyTable.setPlaceholder(placeholder);

        layout.getChildren().addAll(headerBox, historyTable);
        this.historyView = layout;
    }

    private Region spacer(double h) {
        Region r = new Region(); r.setPrefHeight(h); return r;
    }
}
