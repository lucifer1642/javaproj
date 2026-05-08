# PRD - IRCTC Pantry Management System (RailPantry)

## 1. Project Overview

**RailPantry** is a JavaFX-based desktop application designed to streamline pantry operations on long-distance trains (e.g., Rajdhani Express). It replaces manual paper-based tracking with a robust offline system that tracks inventory, logs consumption/waste, manages restocking at halts, and tracks passenger orders.

## 1. Core Structure

The backend follows a simple **MVC (Model-View-Controller)** pattern with **DAO (Data Access Object)** layers to keep the code clean and manageable for a college project.

## 2. Component Design

### 2.1 Model Layer (POJOs)

- `Item`: Base class for inventory items.
- `Inventory`: Item + journey-specific data (qty, expiry).
- `Order`: Passenger meal order details.
- `WasteLog`: Track individual waste events.
- `Station`: Halt station info (Name, Scheduled Arrival, Restock Capability).

## 2. Problem Statement

- **Inventory Chaos:** No systematic tracking of perishable items.
- **Wastage:** High annual pantry wastage (₹8-12 crore IRCTC estimate).
- **Stockouts:** Mid-journey shortages leading to passenger complaints.
- **Revenue Leakage:** No cross-check between consumption and billing.

## 3. Target Audience

- Pantry Managers on long-distance trains.
- Contracting agencies managing IRCTC pantry cars.

## 4. Functional Requirements

### 4.1 Inventory Management
- **Initial Load:** Input starting stock (name, qty, unit, expiry, category).
- **Real-time Updates:** Adjust stock levels based on consumption or waste.
- **Alert System:**
    - Expiry Warning: Items expiring within 4 hours.
    - Low Stock Alert: Items below a set threshold.

### 4.2 Consumption & Meal Logging
- **Recipe Management:** Link meal types (Lunch, Breakfast) to ingredient deductions.
- **Log Preparation:** One-click logging for prepared meals.

### 4.3 Waste Management
- **Waste Logging:** Log quantity + reason code (Spoiled, Expired, Dropped, etc.).
- **Wastage Analytics:** Identify top-wasted items and common reasons.

### 4.4 Halt Restocking
- **Route Tracking:** Delhi → Mathura → Kota → Ratlam → Vadodara → Mumbai.
- **Smart Suggestions:** Auto-calculate restocking needs based on remaining journey time and expected demand.
- **Order Finalization:** Manager reviews and confirms restocking orders for upcoming halts.

### 4.5 Passenger Order Tracker
- **Meal Distribution:** Track orders by Coach and Seat.
- **Payment Status:** Log Paid/Pending/Prepaid (IRCTC).

### 4.6 Reporting
- **End of Journey (EoJ) Report:** Comprehensive summary of stock, revenue, and waste.
- **PDF Export:** Generate contractor-ready reports using iText.

## 5. Non-Functional Requirements
- **Offline First:** Must operate 100% offline during the journey.
- **Simplicity:** UX tailored for high-pressure pantry environments (minimal typing, large buttons).
- **Visual Feedback:** Color-coded status alerts (Green/Yellow/Red).
- **Performance:** Fast enough to run on standard laptops.

## 6. Success Metrics
- 90% reduction in "gut-feeling" ordering errors.
- Accurate identification of waste patterns.
- Zero mid-journey stockouts for critical items (water, meals).
