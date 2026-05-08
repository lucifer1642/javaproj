package com.railpantry.screens;

import com.railpantry.SessionStore;
import com.railpantry.SessionStore.OrderEntry;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PassengerTrackerScreen extends BorderPane {

    private TableView<OrderEntry> table;
    private FilteredList<OrderEntry> filteredData;
    private Label totalCashLabel;
    private Label totalPrepaidLabel;
    private Label totalMealsLabel;
    private String currentCoach = "A1";

    public PassengerTrackerScreen() {
        getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        // ── TOP ──────────────────────────────────────────────────────────────
        VBox topBox = new VBox(20);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(5);
        Label headerTitle = new Label("Passenger Order Tracker");
        headerTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
        Label subTitle = new Label("Track real-time orders, payments, and seat allocations.");
        subTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        titleBox.getChildren().addAll(headerTitle, subTitle);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        header.getChildren().addAll(titleBox, headerSpacer);

        // Coach Selector
        HBox coachToolbar = new HBox(10);
        coachToolbar.setAlignment(Pos.CENTER_LEFT);
        Label coachLabel = new Label("Coach:");
        coachLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        coachToolbar.getChildren().add(coachLabel);

        String[] coaches = {"A1", "A2", "B1", "B2", "S1", "S2", "S3"};
        ToggleGroup coachGroup = new ToggleGroup();

        for (String coach : coaches) {
            ToggleButton coachBtn = new ToggleButton(coach);
            coachBtn.setToggleGroup(coachGroup);
            coachBtn.setStyle("-fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 8px 15px; -fx-cursor: hand;");
            if (coach.equals(currentCoach)) {
                coachBtn.setSelected(true);
                coachBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white;" +
                        " -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 8px 15px; -fx-cursor: hand;");
            } else {
                coachBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;" +
                        " -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 8px 15px; -fx-cursor: hand;");
            }
            coachBtn.setOnAction(e -> {
                if (coachBtn.isSelected()) {
                    currentCoach = coach;
                    updateCoachStyles(coachGroup);
                    filterByCoach(currentCoach);
                } else {
                    coachBtn.setSelected(true);
                }
            });
            coachToolbar.getChildren().add(coachBtn);
        }

        topBox.getChildren().addAll(header, coachToolbar);
        setTop(topBox);

        // ── CENTER: Order Table ───────────────────────────────────────────────
        table = new TableView<>();
        table.getStyleClass().add("table-view");
        BorderPane.setMargin(table, new Insets(20, 20, 20, 0));

        TableColumn<OrderEntry, String> seatCol = new TableColumn<>("Seat");
        seatCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().seat));
        seatCol.setPrefWidth(80);

        TableColumn<OrderEntry, String> mealCol = new TableColumn<>("Meal Type");
        mealCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().mealType));
        mealCol.setPrefWidth(220);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        TableColumn<OrderEntry, String> timeCol = new TableColumn<>("Order Time");
        timeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().orderTime.format(fmt)));
        timeCol.setPrefWidth(110);

        TableColumn<OrderEntry, String> statusCol = new TableColumn<>("Payment");
        statusCol.setPrefWidth(160);
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                String bg, fg;
                switch (item) {
                    case "PAID"          -> { bg = "#d1fae5"; fg = "#047857"; }
                    case "IRCTC PREPAID" -> { bg = "#dbeafe"; fg = "#1d4ed8"; }
                    default              -> { bg = "#fef3c7"; fg = "#b45309"; }
                }
                Label pill = new Label(item);
                pill.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + fg +
                        "; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 3px 10px;" +
                        " -fx-background-radius: 20px;");
                setGraphic(pill); setText(null);
            }
        });

        TableColumn<OrderEntry, String> priceCol = new TableColumn<>("Price (₹)");
        priceCol.setCellValueFactory(d -> new SimpleStringProperty("₹ " + d.getValue().price));
        priceCol.setPrefWidth(90);

        table.getColumns().addAll(seatCol, mealCol, timeCol, statusCol, priceCol);

        // Bind to SessionStore orders
        filteredData = new FilteredList<>(SessionStore.get().getOrders(),
                p -> p.coach.equals(currentCoach));
        table.setItems(filteredData);

        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(OrderEntry item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-paid", "row-pending", "row-prepaid");
                if (item == null || empty) { setStyle(""); return; }
                switch (item.status) {
                    case "PAID"          -> getStyleClass().add("row-paid");
                    case "PENDING"       -> getStyleClass().add("row-pending");
                    case "IRCTC PREPAID" -> getStyleClass().add("row-prepaid");
                }
            }
        });

        setCenter(table);

        // ── RIGHT: Actions & Revenue ──────────────────────────────────────────
        VBox rightBox = new VBox(20);
        rightBox.setPadding(new Insets(20, 0, 0, 0));
        rightBox.setPrefWidth(250);

        Label actionsTitle = new Label("Order Actions");
        actionsTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #475569; -fx-font-size: 14px;");

        Button newOrderBtn = createActionBtn("+ Add New Order",    "-fx-background-color: #3b82f6; -fx-text-fill: white;");
        Button markPaidBtn = createActionBtn("✔ Mark as Paid",    "-fx-background-color: #10b981; -fx-text-fill: white;");
        Button printBtn    = createActionBtn("🖨 Print Receipt",   "-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-border-color: #e2e8f0; -fx-border-width: 1px;");

        newOrderBtn.setOnAction(e -> handleAddOrder());
        markPaidBtn.setOnAction(e -> handleMarkPaid());
        printBtn.setOnAction(e    -> handlePrintReceipt());

        VBox actionsBox = new VBox(12, actionsTitle, newOrderBtn, markPaidBtn, printBtn);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Revenue + Meals Served tally
        VBox revenueBox = new VBox(10);
        revenueBox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 20px;" +
                " -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");

        Label revTitle = new Label("Journey Tally");
        revTitle.setStyle("-fx-font-weight: 800; -fx-font-size: 16px; -fx-text-fill: #0f172a;");

        totalMealsLabel = new Label("Meals Served: 0");
        totalMealsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #7c3aed; -fx-font-size: 14px;");

        totalCashLabel = new Label("Cash Collected: ₹ 0");
        totalCashLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #10b981; -fx-font-size: 14px;");

        totalPrepaidLabel = new Label("Prepaid (IRCTC): ₹ 0");
        totalPrepaidLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #3b82f6; -fx-font-size: 14px;");

        revenueBox.getChildren().addAll(revTitle, totalMealsLabel, totalCashLabel, totalPrepaidLabel);

        rightBox.getChildren().addAll(actionsBox, spacer, revenueBox);
        setRight(rightBox);

        // Bind live update to SessionStore order list
        SessionStore.get().getOrders().addListener(
                (javafx.collections.ListChangeListener<OrderEntry>) c -> calculateRevenue());

        calculateRevenue();
    }

    // ── Handlers ─────────────────────────────────────────────────────────────

    private void handleMarkPaid() {
        OrderEntry sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Select an order first."); return; }
        if (sel.status.equals("PAID"))          { info("Already paid."); return; }
        if (sel.status.equals("IRCTC PREPAID")) { info("Prepaid via IRCTC."); return; }
        sel.status = "PAID";
        table.refresh();
        calculateRevenue();
    }

    private void handlePrintReceipt() {
        OrderEntry sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Select an order first."); return; }
        info("Printing receipt for Coach " + sel.coach + " Seat " + sel.seat +
                "\nMeal: " + sel.mealType + "\nTotal: ₹" + sel.price);
    }

    private void handleAddOrder() {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("New Order — Coach " + currentCoach);
        dlg.setHeaderText("Add a walk-in order for Coach " + currentCoach);

        ButtonType saveType = new ButtonType("Add Order", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField seatField = new TextField();
        seatField.setPromptText("Seat number");

        var mealNames = SessionStore.get().getInventory().stream()
                .map(i -> i.name).collect(java.util.stream.Collectors.toList());
        ComboBox<String> mealBox = new ComboBox<>(FXCollections.observableArrayList(mealNames));
        mealBox.getSelectionModel().selectFirst();
        mealBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("PENDING", "PAID", "IRCTC PREPAID"));
        statusBox.getSelectionModel().selectFirst();

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(12); grid.setPadding(new Insets(20, 100, 10, 10));
        grid.add(new Label("Seat No:"), 0, 0);  grid.add(seatField, 1, 0);
        grid.add(new Label("Meal:"),    0, 1);  grid.add(mealBox,   1, 1);
        grid.add(new Label("Status:"),  0, 2);  grid.add(statusBox, 1, 2);
        dlg.getDialogPane().setContent(grid);

        dlg.setResultConverter(btn -> {
            if (btn == saveType && !seatField.getText().isBlank()) {
                SessionStore.InventoryItem inv = SessionStore.get().getInventory().stream()
                        .filter(i -> i.name.equals(mealBox.getValue())).findFirst().orElse(null);
                int price = inv != null ? inv.pricePerUnit : 50;
                SessionStore.get().getOrders().add(new OrderEntry(
                        currentCoach, seatField.getText(), mealBox.getValue(),
                        LocalTime.now(), statusBox.getValue(), price));
                calculateRevenue();
            }
            return null;
        });
        dlg.show();
    }

    private void updateCoachStyles(ToggleGroup group) {
        for (Toggle t : group.getToggles()) {
            ToggleButton tb = (ToggleButton) t;
            tb.setStyle(tb.isSelected()
                    ? "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 8px 15px; -fx-cursor: hand;"
                    : "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 8px 15px; -fx-cursor: hand;");
        }
    }

    private void filterByCoach(String coach) {
        filteredData.setPredicate(p -> p.coach.equals(coach));
    }

    private void calculateRevenue() {
        int cash = 0, prepaid = 0, total = SessionStore.get().getOrders().size();
        for (OrderEntry o : SessionStore.get().getOrders()) {
            if (o.status.equals("PAID"))           cash    += o.price;
            else if (o.status.equals("IRCTC PREPAID")) prepaid += o.price;
        }
        totalMealsLabel.setText("Meals Served: " + total);
        totalCashLabel.setText("Cash Collected: ₹ " + cash);
        totalPrepaidLabel.setText("Prepaid (IRCTC): ₹ " + prepaid);
    }

    private Button createActionBtn(String text, String style) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(style + " -fx-font-weight: bold; -fx-padding: 12px; -fx-background-radius: 6px; -fx-cursor: hand;");
        return btn;
    }

    private void warn(String msg) { new Alert(Alert.AlertType.WARNING, msg).show(); }
    private void info(String msg) { new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).show(); }
}
