package com.railpantry.screens;

import com.railpantry.SessionStore;
import com.railpantry.SessionStore.InventoryItem;
import com.railpantry.SessionStore.WasteEntry;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EOJReportScreen extends BorderPane {

    private TableView<EoJRow> table;
    private ObservableList<EoJRow> tableData = FXCollections.observableArrayList();

    public EOJReportScreen() {
        setPadding(new Insets(30, 40, 30, 40));
        getStyleClass().add("root");

        // ── TOP: Train Header ─────────────────────────────────────────────────
        VBox headerContainer = new VBox(10);
        headerContainer.getStyleClass().add("header-container");

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label trainName = new Label(SessionStore.get().getTrainName());
        trainName.getStyleClass().add("train-title");
        Label routeData = new Label("Journey: " + SessionStore.get().getTrainRoute());
        routeData.getStyleClass().add("train-subtitle");
        titleBox.getChildren().addAll(trainName, routeData);

        Region sp1 = new Region(); HBox.setHgrow(sp1, Priority.ALWAYS);

        HBox pillsBox = new HBox(10);
        pillsBox.setAlignment(Pos.CENTER_RIGHT);
        Label datePill = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        datePill.getStyleClass().add("train-info-pill");
        Label statusPill = new Label(SessionStore.get().isJourneyEnded() ? "Journey Ended" : "Journey Active");
        statusPill.getStyleClass().add("train-info-pill");
        statusPill.setStyle(SessionStore.get().isJourneyEnded()
                ? "-fx-background-color: #10b981;"
                : "-fx-background-color: #3b82f6;");
        pillsBox.getChildren().addAll(datePill, statusPill);
        topRow.getChildren().addAll(titleBox, sp1, pillsBox);

        HBox botRow = new HBox(10);
        botRow.setPadding(new Insets(10, 0, 0, 0));
        Label mgr = new Label("Manager: " + SessionStore.get().getManagerName() + " (ID: " + SessionStore.get().getManagerId() + ")");
        mgr.getStyleClass().add("train-info-pill");
        botRow.getChildren().add(mgr);

        headerContainer.getChildren().addAll(topRow, botRow);
        setTop(headerContainer);

        // ── CENTER ───────────────────────────────────────────────────────────
        VBox centerLayout = new VBox(20);
        centerLayout.setPadding(new Insets(25, 0, 0, 0));

        // Summary Cards
        Label sectionTitle = new Label("Executive Summary");
        sectionTitle.getStyleClass().add("section-title");

        HBox summaryBox = new HBox(20);
        summaryBox.getStyleClass().add("summary-container");
        summaryBox.getChildren().addAll(
                createSummaryCard("Total Meals Served",
                        String.valueOf(SessionStore.get().getTotalMealsServed()), "All coaches", "summary-card-green"),
                createSummaryCard("Total Waste Cost",
                        "₹ " + SessionStore.get().getTotalWasteCost(), "Across journey", "summary-card-amber"),
                createSummaryCard("Waste %",
                        computeWastePct() + "%", "Of total consumed", "summary-card-blue")
        );

        Label tableTitle = new Label("Full Reconciliation Breakdown");
        tableTitle.getStyleClass().add("section-title");
        tableTitle.setPadding(new Insets(10, 0, 0, 0));

        // Build table
        buildTable();

        centerLayout.getChildren().addAll(sectionTitle, summaryBox, tableTitle, table);
        setCenter(centerLayout);

        // ── BOTTOM: Action Buttons ────────────────────────────────────────────
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(20, 0, 0, 0));
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button exportBtn = new Button("⬇ Export Report (TXT)");
        exportBtn.getStyleClass().add("btn-primary");
        exportBtn.setOnAction(e -> exportReport());

        Button resetBtn = new Button("⚠ End Journey & Reset");
        resetBtn.getStyleClass().add("btn-danger");
        resetBtn.setOnAction(e -> handleReset());

        Region footSpacer = new Region(); HBox.setHgrow(footSpacer, Priority.ALWAYS);
        footer.getChildren().addAll(footSpacer, resetBtn, exportBtn);
        setBottom(footer);
    }

    // ============================================================
    // Reconciliation Table
    // ============================================================
    private void buildTable() {
        table = new TableView<>();
        table.getStyleClass().add("table-view");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<EoJRow, String> itemCol = new TableColumn<>("Item Name");
        itemCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().item));
        itemCol.setPrefWidth(240);

        TableColumn<EoJRow, Integer> openCol = new TableColumn<>("Opening Stock");
        openCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().openingStock).asObject());
        openCol.setPrefWidth(120);

        TableColumn<EoJRow, Integer> finalCol = new TableColumn<>("Final Stock");
        finalCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().finalStock).asObject());
        finalCol.setPrefWidth(110);

        TableColumn<EoJRow, Integer> consumedCol = new TableColumn<>("Consumed");
        consumedCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().consumed).asObject());
        consumedCol.setPrefWidth(100);

        TableColumn<EoJRow, Integer> wastedCol = new TableColumn<>("Wasted");
        wastedCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().wasted).asObject());
        wastedCol.setPrefWidth(90);

        TableColumn<EoJRow, String> lossCol = new TableColumn<>("Loss (₹)");
        lossCol.setCellValueFactory(d -> new SimpleStringProperty("₹" + d.getValue().lossRs));
        lossCol.setPrefWidth(100);

        TableColumn<EoJRow, String> noteCol = new TableColumn<>("Note");
        noteCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().discrepancyNote));
        noteCol.setPrefWidth(200);

        table.getColumns().addAll(itemCol, openCol, finalCol, consumedCol, wastedCol, lossCol, noteCol);

        // Build rows from SessionStore
        for (InventoryItem inv : SessionStore.get().getInventory()) {
            int wasted = SessionStore.get().getWasteLog().stream()
                    .filter(w -> w.itemName.equalsIgnoreCase(inv.name))
                    .mapToInt(w -> w.qty).sum();
            int consumed = inv.openingQty - inv.qty - wasted;
            consumed = Math.max(0, consumed);
            int lossRs = wasted * inv.pricePerUnit;

            // Discrepancy: opening != final + consumed + wasted
            int unaccounted = inv.openingQty - inv.qty - consumed - wasted;
            String note = "";
            if (unaccounted != 0) note = "⚠ Unaccounted: " + Math.abs(unaccounted);

            tableData.add(new EoJRow(inv.name, inv.openingQty, inv.qty, consumed, wasted, lossRs, note));
        }

        table.setItems(tableData);

        // Row factory — amber highlight for discrepancies
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(EoJRow item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setStyle(""); return; }
                if (!item.discrepancyNote.isEmpty()) {
                    setStyle("-fx-background-color: #fef9c3;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    // ============================================================
    // Export
    // ============================================================
    private void exportReport() {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Save EoJ Report");
        chooser.setInitialFileName("EoJ_Report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMMyyyy")) + ".txt");
        chooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));

        java.io.File file = chooser.showSaveDialog(getScene().getWindow());
        if (file == null) return;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            String border = "=".repeat(80);
            pw.println(border);
            pw.println("         RAILPANTRY — END OF JOURNEY RECONCILIATION REPORT");
            pw.println(border);
            pw.println("Train   : " + SessionStore.get().getTrainName());
            pw.println("Route   : " + SessionStore.get().getTrainRoute());
            pw.println("Date    : " + LocalDateTime.now().format(dtf));
            pw.println("Manager : " + SessionStore.get().getManagerName() +
                    " (ID: " + SessionStore.get().getManagerId() + ")");
            pw.println(border);
            pw.println("EXECUTIVE SUMMARY");
            pw.println("  Total Meals Served : " + SessionStore.get().getTotalMealsServed());
            pw.println("  Total Waste Cost   : ₹" + SessionStore.get().getTotalWasteCost());
            pw.println("  Waste %            : " + computeWastePct() + "%");
            pw.println(border);
            pw.printf("%-28s %10s %10s %10s %8s %10s  %-22s%n",
                    "ITEM", "OPENING", "FINAL", "CONSUMED", "WASTED", "LOSS(Rs)", "NOTE");
            pw.println("-".repeat(80));
            for (EoJRow row : tableData) {
                pw.printf("%-28s %10d %10d %10d %8d %10s  %-22s%n",
                        row.item, row.openingStock, row.finalStock,
                        row.consumed, row.wasted, "Rs." + row.lossRs, row.discrepancyNote);
            }
            pw.println(border);
            pw.println("Manager Signature : ______________________");
            pw.println("Date Signed       : ______________________");
            pw.println(border);

            Alert ok = new Alert(Alert.AlertType.INFORMATION,
                    "Report exported to:\n" + file.getAbsolutePath(), ButtonType.OK);
            ok.setHeaderText("Export Successful ✔");
            ok.show();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).show();
        }
    }

    // ============================================================
    // Reset
    // ============================================================
    private void handleReset() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "This will archive current journey metrics to History and clear all session data for the next trip. Are you sure?",
                ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("⚠ End Journey & Reset System");
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                // Archive current journey data before resetting
                SessionStore.get().archiveCurrentJourney();
                
                SessionStore.get().setJourneyEnded(true);
                SessionStore.get().resetJourney();
                tableData.clear();
                com.railpantry.Main.navigateTo(new LoginScreen());
            }
        });
    }

    // ============================================================
    // Helpers
    // ============================================================
    private String computeWastePct() {
        int totalOpening = SessionStore.get().getInventory().stream().mapToInt(i -> i.openingQty).sum();
        int totalWasted  = SessionStore.get().getWasteLog().stream().mapToInt(w -> w.qty).sum();
        if (totalOpening == 0) return "0.0";
        return String.format("%.1f", (totalWasted * 100.0) / totalOpening);
    }

    private VBox createSummaryCard(String title, String value, String subtext, String colorClass) {
        VBox card = new VBox(6);
        card.getStyleClass().addAll("summary-card", colorClass);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label titleLbl = new Label(title.toUpperCase());
        titleLbl.getStyleClass().add("card-title");
        Label valLbl = new Label(value);
        valLbl.getStyleClass().add("card-value");
        Label subLbl = new Label(subtext);
        subLbl.getStyleClass().add("card-subtext");

        card.getChildren().addAll(titleLbl, valLbl, subLbl);
        return card;
    }

    // ============================================================
    // Data Model
    // ============================================================
    public static class EoJRow {
        public String item;
        public int openingStock, finalStock, consumed, wasted, lossRs;
        public String discrepancyNote;

        public EoJRow(String item, int openingStock, int finalStock,
                      int consumed, int wasted, int lossRs, String discrepancyNote) {
            this.item = item;
            this.openingStock = openingStock;
            this.finalStock = finalStock;
            this.consumed = consumed;
            this.wasted = wasted;
            this.lossRs = lossRs;
            this.discrepancyNote = discrepancyNote;
        }
    }
}
