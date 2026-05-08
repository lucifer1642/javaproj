package com.railpantry;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SessionStore — A lightweight static singleton acting as an in-memory data bus.
 * All modules read/write to this store so that, for example, logging waste in
 * WasteLoggerScreen immediately reflects on DashboardScreen's pie chart.
 */
public class SessionStore {

    // =========================================================================
    // Singleton Instance
    // =========================================================================
    private static final SessionStore INSTANCE = new SessionStore();

    public static SessionStore get() {
        return INSTANCE;
    }

    private SessionStore() {
        initDefaults();
    }

    // =========================================================================
    // Train / Journey Info
    // =========================================================================
    private String trainName = "12424 - Rajdhani Express";
    private String trainRoute = "New Delhi (NDLS) → Mumbai Central (MMCT)";
    private String managerName = "Rajesh Kumar";
    private String managerId = "RK-492";
    private LocalDateTime journeyStart = LocalDateTime.now().minusHours(3);
    private boolean journeyEnded = false;

    public String getTrainName()   { return trainName; }
    public String getTrainRoute()  { return trainRoute; }
    public String getManagerName() { return managerName; }
    public String getManagerId()   { return managerId; }
    public LocalDateTime getJourneyStart() { return journeyStart; }
    public boolean isJourneyEnded() { return journeyEnded; }
    public void setJourneyEnded(boolean v) { journeyEnded = v; }

    // =========================================================================
    // Inventory Items
    // =========================================================================
    public static class InventoryItem {
        public String id;
        public String name;
        public String category;
        public int qty;
        public int openingQty;    // For EoJ
        public String unit;
        public LocalDateTime expiry;
        public int threshold;
        public int pricePerUnit;

        public InventoryItem(String id, String name, String category, int qty, String unit,
                             LocalDateTime expiry, int threshold, int pricePerUnit) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.qty = qty;
            this.openingQty = qty;
            this.unit = unit;
            this.expiry = expiry;
            this.threshold = threshold;
            this.pricePerUnit = pricePerUnit;
        }
    }

    private ObservableList<InventoryItem> inventory = FXCollections.observableArrayList();

    public ObservableList<InventoryItem> getInventory() { return inventory; }

    public void deductInventory(String itemName, int qty) {
        for (InventoryItem item : inventory) {
            if (item.name.equalsIgnoreCase(itemName)) {
                item.qty = Math.max(0, item.qty - qty);
                break;
            }
        }
    }

    public void addInventory(String itemName, int qty) {
        for (InventoryItem item : inventory) {
            if (item.name.equalsIgnoreCase(itemName)) {
                item.qty += qty;
                break;
            }
        }
    }

    // =========================================================================
    // Waste Log
    // =========================================================================
    public static class WasteEntry {
        public String itemName;
        public int qty;
        public String reason;
        public String notes;
        public LocalDateTime timestamp;
        public int costEstimate;

        public WasteEntry(String itemName, int qty, String reason, String notes, int costEstimate) {
            this.itemName = itemName;
            this.qty = qty;
            this.reason = reason;
            this.notes = notes;
            this.timestamp = LocalDateTime.now();
            this.costEstimate = costEstimate;
        }
    }

    private ObservableList<WasteEntry> wasteLog = FXCollections.observableArrayList();

    public ObservableList<WasteEntry> getWasteLog() { return wasteLog; }

    public void addWasteEntry(WasteEntry entry) {
        wasteLog.add(entry);
        deductInventory(entry.itemName, entry.qty);
    }

    /** Total waste cost in rupees */
    public int getTotalWasteCost() {
        return wasteLog.stream().mapToInt(e -> e.costEstimate).sum();
    }

    /** Count waste by reason code, for pie chart */
    public java.util.Map<String, Integer> getWasteByReason() {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        for (WasteEntry e : wasteLog) {
            map.merge(e.reason, e.qty, Integer::sum);
        }
        return map;
    }

    // =========================================================================
    // Passenger Orders
    // =========================================================================
    public static class OrderEntry {
        public String coach;
        public String seat;
        public String mealType;
        public LocalTime orderTime;
        public String status; // PAID, PENDING, IRCTC PREPAID
        public int price;

        public OrderEntry(String coach, String seat, String mealType, LocalTime orderTime, String status, int price) {
            this.coach = coach;
            this.seat = seat;
            this.mealType = mealType;
            this.orderTime = orderTime;
            this.status = status;
            this.price = price;
        }
    }

    private ObservableList<OrderEntry> orders = FXCollections.observableArrayList();

    public ObservableList<OrderEntry> getOrders() { return orders; }

    public int getTotalMealsServed() { return orders.size(); }

    // =========================================================================
    // Stations / Route
    // =========================================================================
    public static class StationEntry {
        public String name;
        public String code;
        public String eta;          // HH:mm format
        public boolean supportsRestocking;
        public boolean passed;

        public StationEntry(String name, String code, String eta, boolean supportsRestocking, boolean passed) {
            this.name = name;
            this.code = code;
            this.eta = eta;
            this.supportsRestocking = supportsRestocking;
            this.passed = passed;
        }
    }

    private ObservableList<StationEntry> stations = FXCollections.observableArrayList();

    public ObservableList<StationEntry> getStations() { return stations; }

    /** Next upcoming station that supports restocking */
    public StationEntry getNextRestockStation() {
        for (StationEntry s : stations) {
            if (!s.passed && s.supportsRestocking) return s;
        }
        return null;
    }

    /** Next upcoming halt (any station not yet passed) */
    public StationEntry getNextHalt() {
        for (StationEntry s : stations) {
            if (!s.passed) return s;
        }
        return null;
    }

    // =========================================================================
    // Staff / Users
    // =========================================================================
    public static class StaffEntry {
        public String name;
        public String role;
        public String status; // ACTIVE, INACTIVE
        public String empId;

        public StaffEntry(String name, String empId, String role, String status) {
            this.name = name;
            this.empId = empId;
            this.role = role;
            this.status = status;
        }
    }

    private ObservableList<StaffEntry> staff = FXCollections.observableArrayList();

    public ObservableList<StaffEntry> getStaff() { return staff; }

    // =========================================================================
    // Journey History
    // =========================================================================
    public static class JourneyHistoryEntry {
        public String trainName;
        public String route;
        public String managerName;
        public LocalDateTime date;
        public int totalRevenue;
        public int totalWasteCost;
        public int mealsServed;
        public double wastePct;

        public JourneyHistoryEntry(String trainName, String route, String managerName, 
                                   int totalRevenue, int totalWasteCost, int mealsServed, double wastePct) {
            this.trainName = trainName;
            this.route = route;
            this.managerName = managerName;
            this.date = LocalDateTime.now();
            this.totalRevenue = totalRevenue;
            this.totalWasteCost = totalWasteCost;
            this.mealsServed = mealsServed;
            this.wastePct = wastePct;
        }
    }

    private ObservableList<JourneyHistoryEntry> journeyHistory = FXCollections.observableArrayList();

    public ObservableList<JourneyHistoryEntry> getJourneyHistory() { return journeyHistory; }

    /** Snapshots the current journey metrics into history before reset */
    public void archiveCurrentJourney() {
        int totalRev = orders.stream().mapToInt(o -> o.price).sum();
        int totalWaste = getTotalWasteCost();
        int meals = orders.size();
        
        // Calculate Waste %
        int totalOpening = inventory.stream().mapToInt(i -> i.openingQty).sum();
        int totalWastedQty  = wasteLog.stream().mapToInt(w -> w.qty).sum();
        double wastePct = (totalOpening == 0) ? 0.0 : (totalWastedQty * 100.0) / totalOpening;

        journeyHistory.add(new JourneyHistoryEntry(
            trainName, trainRoute, managerName, totalRev, totalWaste, meals, wastePct
        ));
    }

    // =========================================================================
    // Reset
    // =========================================================================
    public void resetJourney() {
        wasteLog.clear();
        orders.clear();
        for (InventoryItem item : inventory) {
            item.qty = item.openingQty;
        }
        journeyEnded = false;
    }

    // =========================================================================
    // Real-time Aggregations (used by charts — no hardcoded data)
    // =========================================================================

    /**
     * Groups orders by the inventory category of the ordered item.
     * Returns a map of category → total revenue (price * count).
     * Used by Admin Analytics pie chart.
     */
    public java.util.Map<String, Double> getMealCategoryRevenue() {
        java.util.Map<String, Double> map = new java.util.LinkedHashMap<>();
        for (OrderEntry o : orders) {
            String category = inventory.stream()
                    .filter(i -> i.name.equalsIgnoreCase(o.mealType))
                    .map(i -> i.category)
                    .findFirst().orElse("Other");
            map.merge(category, (double) o.price, Double::sum);
        }
        return map;
    }

    /**
     * Returns map of item name → number of orders for that item.
     * Used by Admin Analytics bar chart.
     */
    public java.util.Map<String, Integer> getOrdersPerItem() {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        for (OrderEntry o : orders) {
            map.merge(o.mealType, 1, Integer::sum);
        }
        return map;
    }

    /**
     * Returns map of station code → number of orders placed up to that station
     * (orders bucketed by order time against station ETA sequence).
     * Used by Dashboard line chart.
     */
    public java.util.Map<String, Integer> getOrdersPerStation() {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        // Initialize all stations with 0
        for (StationEntry s : stations) {
            map.put(s.code, 0);
        }
        if (stations.isEmpty()) return map;

        // Bucket each order to the nearest station by order time
        for (OrderEntry o : orders) {
            String bucketCode = stations.get(0).code; // default to first
            for (StationEntry s : stations) {
                try {
                    String[] parts = s.eta.split(":");
                    java.time.LocalTime stationTime =
                            java.time.LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                    if (!o.orderTime.isAfter(stationTime)) {
                        bucketCode = s.code;
                        break;
                    }
                } catch (Exception ignored) {}
            }
            map.merge(bucketCode, 1, Integer::sum);
        }
        return map;
    }

    // =========================================================================
    // Default Data Initializer
    // =========================================================================
    private void initDefaults() {
        LocalDateTime now = LocalDateTime.now();

        // ── Inventory (configuration data — kept as seed) ──────────────────────
        inventory.addAll(
            new InventoryItem("ITM-101", "Rail Neer Water (1L)", "Beverage", 200, "bottles", now.plusDays(30), 50, 15),
            new InventoryItem("ITM-204", "Veg Biryani", "Hot Meal", 45, "boxes", now.plusHours(8), 10, 80),
            new InventoryItem("ITM-205", "Chicken Curry", "Hot Meal", 12, "boxes", now.plusMinutes(90), 10, 120),
            new InventoryItem("ITM-300", "Tomato Soup", "Hot Meal", 0, "cups", now.minusHours(1), 5, 50),
            new InventoryItem("ITM-310", "Frooti (200ml)", "Beverage", 80, "bottles", now.plusDays(10), 30, 20),
            new InventoryItem("ITM-401", "Lays Classic", "Snack", 120, "pkts", now.plusDays(60), 20, 10),
            new InventoryItem("ITM-405", "Paneer Meal", "Hot Meal", 3, "boxes", now.plusHours(12), 15, 120),
            new InventoryItem("ITM-406", "Haldiram Bhujia", "Snack", 60, "pkts", now.plusDays(90), 25, 30)
        );

        // ── wasteLog and orders INITIAL MOCK DATA (Synced across roles) ────────
        orders.addAll(
            new OrderEntry("B4", "22", "Veg Biryani",   now.minusHours(2).toLocalTime(), "PAID", 80),
            new OrderEntry("A1", "12", "Chicken Curry", now.minusHours(1).toLocalTime(), "IRCTC PREPAID", 120),
            new OrderEntry("B2", "45", "Veg Biryani",   now.minusMinutes(45).toLocalTime(), "PENDING", 80),
            new OrderEntry("C3", "08", "Paneer Meal",   now.minusMinutes(20).toLocalTime(), "PAID", 120),
            new OrderEntry("B4", "23", "Veg Biryani",   now.minusMinutes(10).toLocalTime(), "PAID", 80)
        );

        wasteLog.addAll(
            new WasteEntry("Tomato Soup", 5, "Expired", "Found in storage", 250),
            new WasteEntry("Chicken Curry", 2, "Spillage", "During service", 240),
            new WasteEntry("Veg Biryani", 3, "Customer Refused", "Delayed delivery", 240)
        );

        // Deduct inventory for mock orders/waste (optional but realistic)
        deductInventory("Veg Biryani", 11); // 8 from orders, 3 from waste
        deductInventory("Chicken Curry", 3); // 1 from order, 2 from waste
        deductInventory("Paneer Meal", 1);
        deductInventory("Tomato Soup", 5);

        // ── Stations (NDLS → MMCT route — configuration data) ─────────────────
        stations.addAll(
            new StationEntry("New Delhi",     "NDLS", "06:00", false, true),
            new StationEntry("Mathura Jn.",   "MTJ",  "07:30", false, true),
            new StationEntry("Kota Jn.",      "KOTA", "10:45", true,  false),
            new StationEntry("Ratlam Jn.",    "RTM",  "14:00", true,  false),
            new StationEntry("Vadodara Jn.", "BRC",  "17:30", true,  false),
            new StationEntry("Surat",         "ST",   "19:15", false, false),
            new StationEntry("Mumbai Central","MMCT", "22:00", false, false)
        );

        // ── Staff (configuration data — kept) ─────────────────────────────────
        staff.addAll(
            new StaffEntry("Rajesh Kumar", "RK-492", "Pantry Manager",  "ACTIVE"),
            new StaffEntry("Amit Sharma",  "AS-103", "Pantry Staff",    "ACTIVE"),
            new StaffEntry("Priya Singh",  "PS-221", "Pantry Staff",    "INACTIVE"),
            new StaffEntry("Dev Admin",    "DA-001", "Administrator",   "ACTIVE")
        );
    }
}
