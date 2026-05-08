package com.railpantry.screens;

import com.railpantry.SessionStore;
import com.railpantry.SessionStore.InventoryItem;
import com.railpantry.SessionStore.WasteEntry;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.stream.Collectors;

public class WasteLoggerScreen extends BorderPane {

    // Recent log (shown below form)
    private VBox recentLogBox;
    private ComboBox<String> itemCombo;

    public WasteLoggerScreen() {
        getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        // ── TOP: Header ──────────────────────────────────────────────────────
        VBox topBox = new VBox(20);
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        // Pantry food image icon
        InputStream foodStream = getClass().getResourceAsStream("/images/pantry_food.png");
        if (foodStream != null) {
            ImageView foodIcon = new ImageView(new Image(foodStream));
            foodIcon.setFitWidth(72);
            foodIcon.setFitHeight(72);
            foodIcon.setPreserveRatio(true);
            foodIcon.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);");
            header.getChildren().add(foodIcon);
        }

        VBox titleBox = new VBox(5);
        Label headerTitle = new Label("🗑  Waste Management Logger");
        headerTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
        Label subTitle = new Label("Record every wastage event. Data flows directly into the Dashboard.");
        subTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        titleBox.getChildren().addAll(headerTitle, subTitle);
        header.getChildren().add(titleBox);
        topBox.getChildren().add(header);
        setTop(topBox);

        // ── CENTER: Two-column layout ────────────────────────────────────────
        HBox centerLayout = new HBox(30);
        centerLayout.setPadding(new Insets(25, 0, 0, 0));

        // Left: Entry Form
        VBox formContainer = buildWasteForm();
        HBox.setHgrow(formContainer, Priority.SOMETIMES);

        // Right: Recent Log
        VBox logPanel = buildRecentLogPanel();
        logPanel.setPrefWidth(340);

        centerLayout.getChildren().addAll(formContainer, logPanel);
        setCenter(centerLayout);
    }

    // ============================================================
    // Waste Entry Form
    // ============================================================
    private VBox buildWasteForm() {
        VBox form = new VBox(20);
        form.setMaxWidth(520);
        form.setPadding(new Insets(35));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 12px;" +
                " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 4);");

        Label formHeader = new Label("Log New Wastage Event");
        formHeader.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");

        // Inventory Item Combo — populated from SessionStore
        VBox itemBox = new VBox(6);
        Label itemLbl = new Label("Select Inventory Item");
        itemLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");

        var itemNames = SessionStore.get().getInventory().stream()
                .map(i -> i.name).collect(Collectors.toList());
        itemCombo = new ComboBox<>(FXCollections.observableArrayList(itemNames));
        itemCombo.setMaxWidth(Double.MAX_VALUE);
        itemCombo.setStyle("-fx-padding: 8px; -fx-background-radius: 6px;" +
                " -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-background-color: #ffffff;");

        // Sync with inventory changes
        SessionStore.get().getInventory().addListener((javafx.collections.ListChangeListener<InventoryItem>) c -> {
            javafx.application.Platform.runLater(() -> {
                var updatedNames = SessionStore.get().getInventory().stream()
                        .map(i -> i.name).collect(Collectors.toList());
                itemCombo.setItems(FXCollections.observableArrayList(updatedNames));
                if (!updatedNames.isEmpty()) itemCombo.getSelectionModel().selectFirst();
            });
        });
        itemCombo.getSelectionModel().selectFirst();
        itemBox.getChildren().addAll(itemLbl, itemCombo);

        // Quantity + Reason Row
        HBox qtyRow = new HBox(15);

        VBox qtyBox = new VBox(6);
        Label qtyLbl = new Label("Quantity");
        qtyLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        Spinner<Integer> qtySpinner = new Spinner<>(1, 500, 1);
        qtySpinner.setEditable(true);
        qtySpinner.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(qtyBox, Priority.ALWAYS);
        qtyBox.getChildren().addAll(qtyLbl, qtySpinner);

        VBox reasonBox = new VBox(6);
        Label reasonLbl = new Label("Reason Code");
        reasonLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        ComboBox<String> reasonCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Expired", "Spoiled", "Overcooked", "Passenger Return", "Dropped / Spilled"));
        reasonCombo.setMaxWidth(Double.MAX_VALUE);
        reasonCombo.setStyle("-fx-padding: 8px; -fx-background-radius: 6px;" +
                " -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-background-color: #ffffff;");
        reasonCombo.getSelectionModel().selectFirst();
        HBox.setHgrow(reasonBox, Priority.ALWAYS);
        reasonBox.getChildren().addAll(reasonLbl, reasonCombo);
        qtyRow.getChildren().addAll(qtyBox, reasonBox);

        // Notes
        VBox notesBox = new VBox(6);
        Label notesLbl = new Label("Optional Notes");
        notesLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Any additional details...");
        notesArea.setPrefRowCount(3);
        notesArea.setStyle("-fx-background-radius: 6px; -fx-border-color: #cbd5e1;" +
                " -fx-border-radius: 6px; -fx-background-color: #ffffff;");
        notesBox.getChildren().addAll(notesLbl, notesArea);

        // Error banner
        Label errorBanner = new Label();
        errorBanner.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: bold;");
        errorBanner.setVisible(false); errorBanner.setManaged(false);

        // Submit button
        Button submitBtn = new Button("Submit Waste Entry");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold;" +
                " -fx-padding: 14px; -fx-background-radius: 8px; -fx-cursor: hand; -fx-font-size: 14px;");

        // Pulse overlay (confirmation animation)
        StackPane submitStack = new StackPane();
        Circle pulse = new Circle(30);
        pulse.setFill(Color.rgb(220, 38, 38, 0.0));
        pulse.setMouseTransparent(true);
        pulse.setVisible(false);
        submitStack.getChildren().addAll(submitBtn, pulse);

        submitBtn.setOnAction(e -> {
            String selectedItem = itemCombo.getValue();
            int qty = qtySpinner.getValue();

            // Validate against SessionStore stock
            InventoryItem inv = SessionStore.get().getInventory().stream()
                    .filter(i -> i.name.equals(selectedItem))
                    .findFirst().orElse(null);

            if (inv != null && qty > inv.qty + 5) {
                errorBanner.setText("⚠ Quantity exceeds available stock (" + inv.qty + " " + inv.unit + ").");
                errorBanner.setVisible(true); errorBanner.setManaged(true);
                return;
            }
            errorBanner.setVisible(false); errorBanner.setManaged(false);

            int cost = (inv != null) ? qty * inv.pricePerUnit : qty * 50;
            SessionStore.get().addWasteEntry(
                    new WasteEntry(selectedItem, qty, reasonCombo.getValue(),
                            notesArea.getText(), cost));

            // Pulse animation
            playPulseAnimation(pulse, submitBtn, () -> {
                qtySpinner.getValueFactory().setValue(1);
                notesArea.clear();
                refreshRecentLog();
            });
        });

        form.getChildren().addAll(formHeader, itemBox, qtyRow, notesBox, errorBanner, submitStack);
        return form;
    }

    // ============================================================
    // Pulse animation
    // ============================================================
    private void playPulseAnimation(Circle pulse, Button btn, Runnable afterDone) {
        pulse.setVisible(true);

        // Fade + scale on circle
        FadeTransition ft = new FadeTransition(Duration.millis(500), pulse);
        ft.setFromValue(0.8); ft.setToValue(0.0);

        ScaleTransition st = new ScaleTransition(Duration.millis(500), pulse);
        st.setFromX(0.5); st.setFromY(0.5);
        st.setToX(3.5); st.setToY(3.5);

        // Flash the button green briefly
        String origStyle = btn.getStyle();
        btn.setStyle(origStyle.replace("#dc2626", "#10b981"));
        btn.setText("✔ Recorded!");

        ParallelTransition pt = new ParallelTransition(ft, st);
        pt.setOnFinished(ev -> {
            pulse.setVisible(false);
            btn.setStyle(origStyle);
            btn.setText("Submit Waste Entry");
            afterDone.run();
        });
        pt.play();
    }

    // ============================================================
    // Recent Log Panel
    // ============================================================
    private VBox buildRecentLogPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));
        panel.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 12px;");

        Label title = new Label("Recent Waste Log");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: white;");

        recentLogBox = new VBox(10);
        ScrollPane scroll = new ScrollPane(recentLogBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        panel.getChildren().addAll(title, scroll);
        VBox.setVgrow(panel, Priority.ALWAYS);

        refreshRecentLog();
        return panel;
    }

    private void refreshRecentLog() {
        recentLogBox.getChildren().clear();
        var log = SessionStore.get().getWasteLog();
        int size = log.size();
        int start = Math.max(0, size - 8); // show last 8

        for (int i = size - 1; i >= start; i--) {
            WasteEntry entry = log.get(i);
            recentLogBox.getChildren().add(buildLogCard(entry));
        }
    }

    private VBox buildLogCard(WasteEntry entry) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 8px;");

        Label itemLbl = new Label(entry.itemName + " × " + entry.qty);
        itemLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 13px;");

        String reasonColor = switch (entry.reason) {
            case "Expired"           -> "#f87171";
            case "Spoiled"           -> "#fb923c";
            case "Overcooked"        -> "#facc15";
            case "Passenger Return"  -> "#60a5fa";
            default                  -> "#a78bfa";
        };

        Label reasonLbl = new Label("⬤ " + entry.reason + "  |  ₹" + entry.costEstimate);
        reasonLbl.setStyle("-fx-text-fill:" + reasonColor + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label timeLbl = new Label(entry.timestamp.format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm, dd MMM")));
        timeLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        card.getChildren().addAll(itemLbl, reasonLbl, timeLbl);
        return card;
    }
}
