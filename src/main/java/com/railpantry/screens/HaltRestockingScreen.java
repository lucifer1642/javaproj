package com.railpantry.screens;

import com.railpantry.SessionStore;
import com.railpantry.SessionStore.InventoryItem;
import com.railpantry.SessionStore.StationEntry;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class HaltRestockingScreen extends BorderPane {

    private TableView<SuggestionRow> suggTable;
    private ObservableList<SuggestionRow> suggData = FXCollections.observableArrayList();

    public HaltRestockingScreen() {
        getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        // ── TOP: Header ──────────────────────────────────────────────────────
        VBox topBox = new VBox(20);

        VBox titleBox = new VBox(5);
        Label headerTitle = new Label("Halt Restocking Assistant");
        headerTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
        Label subTitle = new Label("Review upcoming stations and confirm smart restock orders before each halt.");
        subTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        titleBox.getChildren().addAll(headerTitle, subTitle);

        // Next Restock Info Banner
        StationEntry nextRestock = SessionStore.get().getNextRestockStation();
        HBox infoBanner = new HBox(20);
        infoBanner.setPadding(new Insets(14, 20, 14, 20));
        infoBanner.setAlignment(Pos.CENTER_LEFT);
        infoBanner.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 10px;");

        if (nextRestock != null) {
            Label stLbl  = new Label("🚉 Next Restock Station: " + nextRestock.name + " (" + nextRestock.code + ")");
            stLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            Label etaLbl = new Label("ETA " + nextRestock.eta);
            etaLbl.setStyle("-fx-text-fill: #10b981; -fx-font-weight: 800; -fx-font-size: 14px;");
            Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
            Label pill = new Label("✔ Restocking Supported");
            pill.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold;" +
                    " -fx-padding: 4px 14px; -fx-background-radius: 20px;");
            infoBanner.getChildren().addAll(stLbl, etaLbl, s, pill);
        } else {
            Label lbl = new Label("⚠ No upcoming restocking stations on this route.");
            lbl.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
            infoBanner.getChildren().add(lbl);
        }

        topBox.getChildren().addAll(titleBox, infoBanner);
        setTop(topBox);

        // ── CENTER: Two-panel split ──────────────────────────────────────────
        HBox mainContent = new HBox(25);
        mainContent.setPadding(new Insets(25, 0, 0, 0));
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        // Left: Station List
        VBox stationPanel = buildStationPanel();
        stationPanel.setPrefWidth(340);

        // Right: Smart Suggestions
        VBox suggPanel = buildSuggestionPanel();
        HBox.setHgrow(suggPanel, Priority.ALWAYS);

        mainContent.getChildren().addAll(stationPanel, suggPanel);
        setCenter(mainContent);

        // Compute suggestions on load
        computeSuggestions();
    }

    // ============================================================
    // Station List Panel
    // ============================================================
    private VBox buildStationPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(25));
        panel.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 12px;");

        Label title = new Label("Route Stations");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: white;");

        VBox list = new VBox(8);
        for (StationEntry st : SessionStore.get().getStations()) {
            list.getChildren().add(buildStationCard(st));
        }

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        panel.getChildren().addAll(title, scroll);
        return panel;
    }

    private HBox buildStationCard(StationEntry st) {
        HBox card = new HBox(12);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER_LEFT);

        String bg = st.passed
                ? "rgba(255,255,255,0.04)"
                : "rgba(255,255,255,0.10)";
        card.setStyle("-fx-background-color:" + bg + "; -fx-background-radius: 8px;");

        VBox info = new VBox(4);
        HBox stationRow = new HBox(8);
        Label nameLbl = new Label(st.name + " (" + st.code + ")");
        nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill:" +
                (st.passed ? "#64748b" : "white") + "; -fx-font-size: 13px;");
        if (st.passed) {
            Label passedBadge = new Label("PASSED");
            passedBadge.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px; " +
                    "-fx-background-color: rgba(255,255,255,0.08); -fx-padding: 1px 6px; -fx-background-radius: 10px;");
            stationRow.getChildren().addAll(nameLbl, passedBadge);
        } else {
            stationRow.getChildren().add(nameLbl);
        }
        Label etaLbl = new Label("ETA " + st.eta);
        etaLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        info.getChildren().addAll(stationRow, etaLbl);

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Label restockPill;
        if (st.supportsRestocking) {
            restockPill = new Label("🔄 Restock");
            restockPill.setStyle("-fx-background-color: #065f46; -fx-text-fill: #6ee7b7;" +
                    " -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3px 10px; -fx-background-radius: 20px;");
        } else {
            restockPill = new Label("Transit");
            restockPill.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #64748b;" +
                    " -fx-font-size: 11px; -fx-padding: 3px 10px; -fx-background-radius: 20px;");
        }

        card.getChildren().addAll(info, sp, restockPill);
        return card;
    }

    // ============================================================
    // Smart Suggestion Table
    // ============================================================
    private VBox buildSuggestionPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(0, 0, 0, 5));

        Label title = new Label("Smart Restock Suggestions");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
        Label hint = new Label("Formula: Recommended = (Avg Demand × Remaining Halts) - Current Stock");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        suggTable = new TableView<>();
        suggTable.getStyleClass().add("table-view");
        VBox.setVgrow(suggTable, Priority.ALWAYS);

        TableColumn<SuggestionRow, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        itemCol.setPrefWidth(200);

        TableColumn<SuggestionRow, String> currentCol = new TableColumn<>("Current Qty");
        currentCol.setCellValueFactory(new PropertyValueFactory<>("currentQty"));
        currentCol.setPrefWidth(110);

        TableColumn<SuggestionRow, String> threshCol = new TableColumn<>("Threshold");
        threshCol.setCellValueFactory(new PropertyValueFactory<>("threshold"));
        threshCol.setPrefWidth(100);

        TableColumn<SuggestionRow, String> recCol = new TableColumn<>("Recommended Order");
        recCol.setCellValueFactory(new PropertyValueFactory<>("recommendedQty"));
        recCol.setPrefWidth(160);

        TableColumn<SuggestionRow, String> adjCol = new TableColumn<>("Adjusted Qty (Editable)");
        adjCol.setPrefWidth(200);
        adjCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().adjustedQty)));
        adjCol.setCellFactory(col -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>(0, 999, 1);
            { spinner.setEditable(true); }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setGraphic(null); return; }
                SuggestionRow row = (SuggestionRow) getTableRow().getItem();
                spinner.getValueFactory().setValue(row.adjustedQty);
                spinner.valueProperty().addListener((obs, o, n) -> row.adjustedQty = n);
                setGraphic(spinner);
            }
        });

        TableColumn<SuggestionRow, String> statusCol = new TableColumn<>("Urgency");
        statusCol.setPrefWidth(110);
        statusCol.setCellValueFactory(new PropertyValueFactory<>("urgency"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                String bg = item.equals("CRITICAL") ? "#fecdd3" : "#fef9c3";
                String fg = item.equals("CRITICAL") ? "#be123c" : "#a16207";
                Label pill = new Label(item);
                pill.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + fg +
                        "; -fx-font-weight: bold; -fx-padding: 3px 10px; -fx-background-radius: 20px;");
                setGraphic(pill); setText(null);
            }
        });

        suggTable.getColumns().addAll(itemCol, currentCol, threshCol, recCol, adjCol, statusCol);
        suggTable.setItems(suggData);

        // Action buttons
        HBox btnRow = new HBox(15);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(10, 0, 0, 0));

        Button refreshBtn = new Button("↺ Recalculate");
        refreshBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-font-weight: bold;" +
                " -fx-padding: 10px 18px; -fx-background-radius: 6px; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> computeSuggestions());

        Button confirmBtn = new Button("✔ Confirm Restock & Update Inventory");
        confirmBtn.getStyleClass().add("btn-primary");
        confirmBtn.setOnAction(e -> confirmRestock());

        Button slipBtn = new Button("🖨 Generate Vendor Slip");
        slipBtn.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; -fx-font-weight: bold;" +
                " -fx-padding: 10px 18px; -fx-background-radius: 6px; -fx-cursor: hand;");
        slipBtn.setOnAction(e -> generateVendorSlip());

        btnRow.getChildren().addAll(refreshBtn, slipBtn, confirmBtn);

        panel.getChildren().addAll(title, hint, suggTable, btnRow);
        return panel;
    }

    // ============================================================
    // Demand Formula & Suggestion Logic
    // ============================================================
    private void computeSuggestions() {
        suggData.clear();
        long remainingHalts = SessionStore.get().getStations().stream()
                .filter(s -> !s.passed).count();
        if (remainingHalts == 0) remainingHalts = 1;

        for (InventoryItem item : SessionStore.get().getInventory()) {
            if (item.qty > item.threshold && item.expiry.isAfter(LocalDateTime.now())) continue;

            // Avg demand = a rough estimate based on waste log + sessionData
            int consumed = item.openingQty - item.qty;
            long stationsPassed = SessionStore.get().getStations().stream()
                    .filter(s -> s.passed).count();
            long avgDemandPerHalt = stationsPassed > 0 ? Math.max(1, consumed / stationsPassed) : 5;
            long recommended = (avgDemandPerHalt * remainingHalts) - item.qty;
            recommended = Math.max(0, recommended);

            String urgency = item.qty == 0 || item.expiry.isBefore(LocalDateTime.now())
                    ? "CRITICAL" : "LOW";

            suggData.add(new SuggestionRow(
                    item.name,
                    item.qty + " " + item.unit,
                    String.valueOf(item.threshold),
                    String.valueOf(recommended),
                    (int) recommended,
                    urgency
            ));
        }
    }

    private void confirmRestock() {
        if (suggData.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No items need restocking right now.").show();
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "This will add adjusted quantities to inventory. Confirm?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm Restock Order");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                for (SuggestionRow row : suggData) {
                    SessionStore.get().addInventory(row.itemName, row.adjustedQty);
                }
                Alert ok = new Alert(Alert.AlertType.INFORMATION,
                        "Inventory updated. " + suggData.size() + " items restocked.", ButtonType.OK);
                ok.setHeaderText("Restock Confirmed ✔");
                ok.show();
                computeSuggestions();
            }
        });
    }

    private void generateVendorSlip() {
        StationEntry st = SessionStore.get().getNextRestockStation();
        String stationName = st != null ? st.name + " (" + st.code + ")" : "Unknown Station";

        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Save Vendor Slip");
        chooser.setInitialFileName("VendorSlip_" + (st != null ? st.code : "STN") + ".txt");
        chooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));

        java.io.File file = chooser.showSaveDialog(getScene().getWindow());
        if (file == null) return;

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            String border = "=".repeat(60);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
            pw.println(border);
            pw.println("        RAILPANTRY — VENDOR RESTOCK ORDER SLIP");
            pw.println(border);
            pw.println("Train   : " + SessionStore.get().getTrainName());
            pw.println("Route   : " + SessionStore.get().getTrainRoute());
            pw.println("Station : " + stationName);
            pw.println("Date    : " + LocalDateTime.now().format(dtf));
            pw.println("Manager : " + SessionStore.get().getManagerName() +
                    " (ID: " + SessionStore.get().getManagerId() + ")");
            pw.println(border);
            pw.printf("%-35s %-12s %-10s%n", "ITEM", "QTY ORDERED", "URGENCY");
            pw.println("-".repeat(60));
            for (SuggestionRow row : suggData) {
                pw.printf("%-35s %-12s %-10s%n", row.itemName, row.adjustedQty, row.urgency);
            }
            pw.println(border);
            pw.println("Vendor Signature : ______________________");
            pw.println("Manager Sign.    : ______________________");
            pw.println(border);

            Alert ok = new Alert(Alert.AlertType.INFORMATION,
                    "Vendor slip saved to:\n" + file.getAbsolutePath(), ButtonType.OK);
            ok.setHeaderText("Slip Generated ✔");
            ok.show();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Failed to save slip: " + ex.getMessage()).show();
        }
    }

    // ============================================================
    // Data Model
    // ============================================================
    public static class SuggestionRow {
        private String itemName;
        private String currentQty;
        private String threshold;
        private String recommendedQty;
        public int adjustedQty;
        private String urgency;

        public SuggestionRow(String itemName, String currentQty, String threshold,
                             String recommendedQty, int adjustedQty, String urgency) {
            this.itemName = itemName;
            this.currentQty = currentQty;
            this.threshold = threshold;
            this.recommendedQty = recommendedQty;
            this.adjustedQty = adjustedQty;
            this.urgency = urgency;
        }

        public String getItemName()       { return itemName; }
        public String getCurrentQty()     { return currentQty; }
        public String getThreshold()      { return threshold; }
        public String getRecommendedQty() { return recommendedQty; }
        public String getUrgency()        { return urgency; }
    }
}
