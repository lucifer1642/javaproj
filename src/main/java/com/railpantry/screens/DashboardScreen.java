package com.railpantry.screens;

import com.railpantry.SessionStore;
import com.railpantry.SessionStore.StationEntry;
import com.railpantry.SessionStore.WasteEntry;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DashboardScreen extends BorderPane {

    private Label clockLabel;
    private Label countdownLabel;
    private Label healthValueLabel;
    private PieChart wasteChart;
    private BarChart<String, Number> stockChart;
    private LineChart<String, Number> orderChart;
    private XYChart.Series<String, Number> stockSeries;
    private XYChart.Series<String, Number> thresholdSeries;
    private XYChart.Series<String, Number> orderSeries;
    private Timeline clockTimeline;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public DashboardScreen() {
        getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        // --- Top Bar (Header) ---
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("header-container");

        // IRCTC Logo in header
        InputStream headerLogoStream = getClass().getResourceAsStream("/images/irctc.jpg");
        if (headerLogoStream != null) {
            ImageView headerLogo = new ImageView(new Image(headerLogoStream));
            headerLogo.setFitWidth(52);
            headerLogo.setFitHeight(52);
            headerLogo.setPreserveRatio(true);
            topBar.getChildren().add(headerLogo);
        }

        VBox titleBox = new VBox(4);
        Label trainName = new Label(SessionStore.get().getTrainName());
        trainName.getStyleClass().add("train-title");

        HBox infoRow = new HBox(20);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        clockLabel = new Label("⏱ --:--:--");
        clockLabel.getStyleClass().add("train-subtitle");

        countdownLabel = new Label("🚉 Next Halt: --");
        countdownLabel.getStyleClass().add("train-subtitle");

        infoRow.getChildren().addAll(clockLabel, countdownLabel);
        titleBox.getChildren().addAll(trainName, infoRow);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox statusPills = new HBox(10);
        statusPills.setAlignment(Pos.CENTER_RIGHT);

        Label syncPill = new Label("● Live Sync: Active");
        syncPill.getStyleClass().add("train-info-pill");
        syncPill.setStyle("-fx-background-color: #059669;");

        Label routePill = new Label(SessionStore.get().getTrainRoute());
        routePill.getStyleClass().add("train-info-pill");

        statusPills.getChildren().addAll(routePill, syncPill);
        topBar.getChildren().addAll(titleBox, spacer, statusPills);
        setTop(topBar);


        // --- Main Content ---
        VBox centerBox = new VBox(25);
        centerBox.setPadding(new Insets(25, 0, 0, 0));

        // === Row 1: Metric Cards ===
        HBox metricsBox = new HBox(20);

        // Stock Health Card — dynamic colour pill
        VBox healthCard = buildHealthCard();
        VBox mealsCard  = buildMetricCard("Meals Served", String.valueOf(SessionStore.get().getTotalMealsServed()), "Total orders this journey", "summary-card-blue");
        VBox wasteCard  = buildMetricCard("Waste Cost", "₹ " + SessionStore.get().getTotalWasteCost(), "Track & reduce losses", "summary-card-amber");

        metricsBox.getChildren().addAll(healthCard, mealsCard, wasteCard);

        // === Row 2: Pie Chart + Bar Chart ===
        HBox chartsRow1 = new HBox(20);
        chartsRow1.setAlignment(Pos.CENTER);

        wasteChart = buildWastePieChart();
        wasteChart.setPrefHeight(300);
        wasteChart.setPrefWidth(370);

        stockChart = buildStockBarChart();
        stockChart.setPrefHeight(300);
        HBox.setHgrow(stockChart, Priority.ALWAYS);

        chartsRow1.getChildren().addAll(wasteChart, stockChart);

        // === Row 3: Line Chart ===
        orderChart = buildOrderLineChart();
        orderChart.setPrefHeight(240);

        // === Train Banner Strip (scenic accent between charts and line chart) ===
        InputStream bannerStream = getClass().getResourceAsStream("/images/train_banner.png");
        StackPane bannerPane = null;
        if (bannerStream != null) {
            ImageView bannerImg = new ImageView(new Image(bannerStream));
            bannerImg.setPreserveRatio(true);
            bannerImg.setSmooth(true);
            
            bannerPane = new StackPane(bannerImg);
            bannerPane.getStyleClass().add("dashboard-banner-pane");
            bannerPane.setPrefHeight(100);
            bannerPane.setMinHeight(100);
            bannerPane.setMaxHeight(100);
            bannerPane.setMaxWidth(Double.MAX_VALUE);

            // Clip for rounded corners and fixed height
            Rectangle clip = new Rectangle();
            clip.setArcWidth(24);
            clip.setArcHeight(24);
            clip.widthProperty().bind(bannerPane.widthProperty());
            clip.heightProperty().bind(bannerPane.heightProperty());
            bannerPane.setClip(clip);

            // Bind image width to pane width, height will follow preserveRatio
            bannerImg.fitWidthProperty().bind(bannerPane.widthProperty());
        }

        centerBox.getChildren().add(metricsBox);
        centerBox.getChildren().add(chartsRow1);
        if (bannerPane != null) centerBox.getChildren().add(bannerPane);
        centerBox.getChildren().add(orderChart);

        // Wrap centerBox in ScrollPane for visibility on smaller screens
        ScrollPane scrollPane = new ScrollPane(centerBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("dashboard-scroll-pane");
        
        setCenter(scrollPane);

        // --- Data Listeners (Real-time Sync) ---
        SessionStore.get().getOrders().addListener(
                (javafx.collections.ListChangeListener<SessionStore.OrderEntry>) c ->
                        javafx.application.Platform.runLater(this::refreshAll));

        SessionStore.get().getInventory().addListener(
                (javafx.collections.ListChangeListener<SessionStore.InventoryItem>) c ->
                        javafx.application.Platform.runLater(this::refreshAll));

        SessionStore.get().getWasteLog().addListener(
                (javafx.collections.ListChangeListener<SessionStore.WasteEntry>) c ->
                        javafx.application.Platform.runLater(this::refreshAll));

        // Start live clock
        startClock();

        // Post-render interactions
        Platform.runLater(() -> {
            attachInteractivity();
            animateChartEntrance(wasteChart);
            animateChartEntrance(stockChart);
            animateChartEntrance(orderChart);
        });
    }

    // ============================================================
    // Health Card
    // ============================================================
    private VBox buildHealthCard() {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("summary-card");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label title = new Label("STOCK HEALTH");
        title.getStyleClass().add("card-title");

        // Compute health: count expired / low items
        long expired = SessionStore.get().getInventory().stream()
                .filter(i -> i.expiry.isBefore(LocalDateTime.now())).count();
        long low = SessionStore.get().getInventory().stream()
                .filter(i -> i.qty <= i.threshold && i.expiry.isAfter(LocalDateTime.now())).count();

        String healthText;
        String pillStyle;
        String borderColor;
        if (expired > 0) {
            healthText = "⬤  Critical";
            pillStyle  = "-fx-background-color: #dc2626; -fx-text-fill: white;";
            borderColor = "-fx-border-color: #dc2626;";
        } else if (low > 0) {
            healthText = "⬤  Low Stock";
            pillStyle  = "-fx-background-color: #f59e0b; -fx-text-fill: white;";
            borderColor = "-fx-border-color: #f59e0b;";
        } else {
            healthText = "⬤  Excellent";
            pillStyle  = "-fx-background-color: #10b981; -fx-text-fill: white;";
            borderColor = "-fx-border-color: #10b981;";
        }

        healthValueLabel = new Label(healthText);
        healthValueLabel.setStyle(pillStyle +
                " -fx-font-weight: 900; -fx-font-size: 18px; -fx-padding: 6px 20px; -fx-background-radius: 30px;");

        int total = SessionStore.get().getInventory().size();
        Label sub = new Label(expired + " expired · " + low + " low of " + total + " items");
        sub.getStyleClass().add("card-subtext");

        card.setStyle("-fx-background-color: white; -fx-background-radius: 12px;" +
                " -fx-padding: 25px 20px; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,4);" +
                " -fx-min-width: 250px; -fx-border-width: 0 0 0 6px; -fx-border-radius: 12px;" + borderColor);
        card.getChildren().addAll(title, healthValueLabel, sub);

        return card;
    }

    private VBox buildMetricCard(String title, String value, String subtext, String colorClass) {
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

        ScaleTransition stIn = new ScaleTransition(Duration.millis(150), card);
        stIn.setToX(1.03); stIn.setToY(1.03);
        ScaleTransition stOut = new ScaleTransition(Duration.millis(150), card);
        stOut.setToX(1.0);  stOut.setToY(1.0);
        card.setOnMouseEntered(e -> stIn.playFromStart());
        card.setOnMouseExited(e ->  stOut.playFromStart());
        return card;
    }

    // ============================================================
    // Charts
    // ============================================================
    private PieChart buildWastePieChart() {
        PieChart chart = new PieChart();
        chart.setAnimated(true);
        chart.setTitle("Waste by Reason");
        refreshWastePieData(chart);
        chart.setStyle("-fx-background-color: white; -fx-background-radius: 12px;" +
                " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        return chart;
    }

    private void refreshWastePieData(PieChart chart) {
        chart.getData().clear();
        Map<String, Integer> byReason = SessionStore.get().getWasteByReason();
        if (byReason.isEmpty()) {
            chart.getData().add(new PieChart.Data("No Waste Logged", 1));
        } else {
            byReason.forEach((reason, qty) ->
                    chart.getData().add(new PieChart.Data(reason, qty)));
        }
    }

    private BarChart<String, Number> buildStockBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        yAxis.setLabel("Quantity");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setAnimated(true);
        chart.setCategoryGap(18);
        chart.setTitle("Critical Stock vs Threshold (Top 5)");
        chart.setStyle("-fx-background-color: white; -fx-background-radius: 12px;" +
                " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        stockSeries = new XYChart.Series<>();
        stockSeries.setName("Current Qty");

        thresholdSeries = new XYChart.Series<>();
        thresholdSeries.setName("Threshold");

        refreshStockChart();
        chart.getData().addAll(stockSeries, thresholdSeries);
        return chart;
    }

    private void refreshStockChart() {
        stockSeries.getData().clear();
        thresholdSeries.getData().clear();

        // Pick 5 most critical items (sorted by qty/threshold ratio asc)
        SessionStore.get().getInventory().stream()
                .filter(i -> i.expiry.isAfter(LocalDateTime.now()))
                .sorted((a, b) -> {
                    double ratioA = (a.threshold == 0) ? 1.0 : (double)a.qty/a.threshold;
                    double ratioB = (b.threshold == 0) ? 1.0 : (double)b.qty/b.threshold;
                    return Double.compare(ratioA, ratioB);
                })
                .limit(5)
                .forEach(item -> {
                    stockSeries.getData().add(new XYChart.Data<>(item.name.split(" ")[0], item.qty));
                    thresholdSeries.getData().add(new XYChart.Data<>(item.name.split(" ")[0], item.threshold));
                });
    }

    private LineChart<String, Number> buildOrderLineChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        xAxis.setLabel("Station");
        yAxis.setLabel("Orders");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(true);
        chart.setTitle("Live Order Flow Across Journey (" + SessionStore.get().getTrainRoute() + ")");
        chart.setStyle("-fx-background-color: white; -fx-background-radius: 12px;" +
                " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        orderSeries = new XYChart.Series<>();
        orderSeries.setName("Orders");

        // Populate from real SessionStore data
        refreshOrderSeries();

        chart.getData().add(orderSeries);
        chart.setLegendVisible(false);

        return chart;
    }

    /** Rebuild the order line-chart series from live SessionStore data */
    private void refreshOrderSeries() {
        orderSeries.getData().clear();
        java.util.Map<String, Integer> perStation = SessionStore.get().getOrdersPerStation();
        if (perStation.isEmpty()) return;
        perStation.forEach((code, count) ->
                orderSeries.getData().add(new XYChart.Data<>(code, count)));
    }

    // ============================================================
    // Live Clock + Countdown
    // ============================================================
    private void startClock() {
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            clockLabel.setText("⏱ " + LocalDateTime.now().format(TIME_FMT));
            updateCountdown();
        }));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();

        // Stop when scene is removed
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && clockTimeline != null) {
                clockTimeline.stop();
            }
        });
    }

    private void updateCountdown() {
        StationEntry next = SessionStore.get().getNextHalt();
        if (next == null) {
            countdownLabel.setText("🏁 Journey Complete");
            return;
        }
        // Parse ETA HH:mm on today
        try {
            String[] parts = next.eta.split(":");
            LocalDateTime etaToday = LocalDateTime.now()
                    .withHour(Integer.parseInt(parts[0]))
                    .withMinute(Integer.parseInt(parts[1]))
                    .withSecond(0);
            long diffMin = java.time.Duration.between(LocalDateTime.now(), etaToday).toMinutes();
            if (diffMin < 0) {
                countdownLabel.setText("🚉 Arriving: " + next.name);
            } else {
                countdownLabel.setText("🚉 Next: " + next.name + " in " + diffMin + " min");
            }
        } catch (Exception ex) {
            countdownLabel.setText("🚉 Next: " + next.name);
        }
    }

    // ============================================================
    // Interactivity (tooltips + hover + floaters)
    // ============================================================
    private void animateChartEntrance(Node chart) {
        chart.setOpacity(0);
        chart.setTranslateY(20);
        FadeTransition ft = new FadeTransition(Duration.millis(800), chart);
        ft.setToValue(1.0);
        TranslateTransition tt = new TranslateTransition(Duration.millis(800), chart);
        tt.setToY(0);
        new ParallelTransition(ft, tt).play();
    }

    private void attachInteractivity() {
        // Pie chart tooltips (Floaters)
        double totalWaste = wasteChart.getData().stream().mapToDouble(PieChart.Data::getPieValue).sum();

        for (PieChart.Data data : wasteChart.getData()) {
            Node node = data.getNode();
            if (node != null) {
                double percentage = (totalWaste > 0) ? (data.getPieValue() / totalWaste) * 100 : 0;
                String info = String.format("%s\nValue: %d units\nShare: %.1f%%",
                        data.getName(), (int)data.getPieValue(), percentage);

                Tooltip t = new Tooltip(info);
                t.setShowDelay(Duration.millis(100));
                Tooltip.install(node, t);

                // Hover Animation: Pop-out effect
                ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
                node.setOnMouseEntered(e -> {
                    node.setViewOrder(-1); // Bring to front
                    st.setToX(1.1); st.setToY(1.1);
                    st.playFromStart();
                    node.setStyle("-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0.2, 0, 0);");
                });
                node.setOnMouseExited(e -> {
                    st.setToX(1.0); st.setToY(1.0);
                    st.playFromStart();
                    node.setStyle("");
                });
            }
        }

        // Bar chart tooltips + Hover animations
        for (XYChart.Series<String, Number> series : stockChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    String seriesName = series.getName();
                    Tooltip t = new Tooltip(seriesName + ": " + data.getYValue() + " units\nItem: " + data.getXValue());
                    Tooltip.install(node, t);

                    ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
                    node.setOnMouseEntered(e -> {
                        st.setToX(1.05); st.setToY(1.1);
                        st.playFromStart();
                        node.setStyle("-fx-cursor: hand; -fx-opacity: 0.9;");
                    });
                    node.setOnMouseExited(e -> {
                        st.setToX(1.0); st.setToY(1.0);
                        st.playFromStart();
                        node.setStyle(seriesName.equals("Threshold") ? "-fx-bar-fill: rgba(239,68,68,0.35); -fx-background-radius: 4px;" : "");
                    });
                }
            }
        }

        // Line chart tooltips + Pulse effect on hover
        for (XYChart.Data<String, Number> data : orderSeries.getData()) {
            Node node = data.getNode();
            if (node != null) {
                Tooltip t = new Tooltip("Station: " + data.getXValue() + "\nOrders: " + data.getYValue());
                Tooltip.install(node, t);

                node.setOnMouseEntered(e -> {
                    node.setScaleX(1.8); node.setScaleY(1.8);
                    node.setStyle("-fx-cursor: hand; -fx-effect: dropshadow(gaussian, #3b82f6, 10, 0.5, 0, 0);");
                });
                node.setOnMouseExited(e -> {
                    node.setScaleX(1.0); node.setScaleY(1.0);
                    node.setStyle("");
                });
            }
        }

        // Transparent chart backgrounds
        Node plotBg = orderChart.lookup(".chart-plot-background");
        if (plotBg != null) plotBg.setStyle("-fx-background-color: transparent;");
    }

    /** Called by other screens or via listeners to refresh all dashboard data */
    public void refreshAll() {
        // Refresh Charts
        refreshWastePieData(wasteChart);
        refreshStockChart();
        refreshOrderSeries();

        // Refresh Metric Cards (we rebuild the center to keep it simple and clean)
        VBox center = (VBox) getCenter();
        if (center != null && !center.getChildren().isEmpty() && center.getChildren().get(0) instanceof HBox) {
            HBox metricsBox = (HBox) center.getChildren().get(0);
            metricsBox.getChildren().clear();

            VBox healthCard = buildHealthCard();
            VBox mealsCard  = buildMetricCard("Meals Served", String.valueOf(SessionStore.get().getTotalMealsServed()), "Total orders this journey", "summary-card-blue");
            VBox wasteCard  = buildMetricCard("Waste Cost", "₹ " + SessionStore.get().getTotalWasteCost(), "Track & reduce losses", "summary-card-amber");

            metricsBox.getChildren().addAll(healthCard, mealsCard, wasteCard);
        }

        Platform.runLater(this::attachInteractivity);
    }
}
