# Project Synopsis

---

## RailPantry — IRCTC Pantry Management System

**Course:** Master of Computer Applications (MCA) — 2nd Semester
**Project Type:** Desktop Application
**Technology:** JavaFX 25 | SQLite | Maven | iText Core
**Platform:** Windows (Desktop — Offline First)

---

## 1. Introduction

The Indian Railways operates one of the largest railway networks in the world, serving millions of passengers daily across thousands of long-distance trains. Among the critical yet under-digitized segments of this ecosystem is the **pantry car** — the onboard kitchen unit responsible for meal preparation, inventory management, and food supply throughout the journey.

Trains like the **Rajdhani Express (Train No. 12424, Delhi–Mumbai)** carry hundreds of passengers over 16+ hour journeys, requiring the pantry team to manage dozens of perishable and non-perishable items simultaneously. Currently, most pantry operations are conducted using **manual, paper-based systems** — a method that is both error-prone and incompatible with the pace and scale of modern railway operations.

**RailPantry** is proposed as a modern, offline-first JavaFX desktop application to digitize, streamline, and intelligently assist pantry operations aboard long-distance Indian trains.

---

## 2. Problem Statement

The existing manual approach to pantry car management suffers from several critical operational issues:

| Problem | Impact |
|---|---|
| **Inventory Chaos** | No systematic tracking of perishable items leads to undetected spoilage and guesswork during stock reviews. |
| **High Wastage** | Annual IRCTC pantry wastage is estimated at **₹8–12 crore** due to poor stock rotation and expiry tracking. |
| **Mid-Journey Stockouts** | Without data-driven demand forecasting, critical items (water, meals) frequently run short before destination. |
| **Revenue Leakage** | No automated cross-check between items consumed and items billed results in unaccounted losses. |
| **No Halt Planning** | Restocking at intermediate stations is decided by intuition, not calculation, causing over- or under-ordering. |
| **No Audit Trail** | A lack of structured records makes it impossible for contractors to dispute or verify IRCTC billing at journey's end. |

---

## 3. Proposed Solution

**RailPantry** is a fully offline JavaFX desktop application designed to replace the above manual system with a robust digital solution. It provides the pantry manager with:

- **Live inventory tracking** with automated expiry and low-stock alerts.
- **Recipe-linked consumption logging** — one action deducts all relevant ingredients.
- **Predictive restocking suggestions** based on demand analytics and remaining journey time.
- **Passenger meal order tracking** by coach and seat number.
- **End-of-Journey (EoJ) reconciliation reports** exportable as PDFs.
- **Role-based access control** with a secure Admin Control Center for system-level management.

The application is designed to operate **100% offline** on a standard laptop, making it practical for real-world pantry environments with no reliable internet connectivity.

---

## 4. Objectives

- **Primary Objectives:**
  - Eliminate paper-based inventory tracking in pantry cars.
  - Enable real-time stock level monitoring with automated alerts.
  - Reduce food wastage through accurate expiry and threshold tracking.
  - Support data-driven restocking decisions at intermediate halts.
  - Generate structured, auditable End-of-Journey reports.

- **Secondary Objectives:**
  - Provide a high-visibility, minimal-effort UI for high-pressure pantry environments.
  - Establish a codebase architecture compatible with future Android/iOS deployment.
  - Implement role-based access to prevent unauthorized data manipulation.

---

## 5. Scope of the Project

### In Scope
- Inventory management (CRUD operations, expiry alerts, threshold alerts).
- Meal consumption logging with recipe-based ingredient deduction.
- Waste logging with categorized reason codes.
- Halt-based restocking assistant with smart quantity suggestions.
- Passenger order tracking (coach, seat, payment status).
- End-of-Journey report with PDF export.
- Admin Dashboard for master menu management and user role control.
- Offline data persistence using an embedded SQLite database.

### Out of Scope
- Real-time internet connectivity or cloud sync (simulated only).
- Integration with official IRCTC APIs or live train data feeds.
- Android/iOS mobile deployment (architecture is compatible but not implemented in this phase).
- Multi-train or multi-journey data simultaneously.

---

## 6. System Architecture

### 6.1 Architectural Pattern

RailPantry follows a clean **MVC (Model-View-Controller)** pattern with a dedicated **DAO (Data Access Object)** layer, ensuring separation of concerns between the UI, business logic, and data persistence.

```
┌─────────────────────────────────────────────┐
│              VIEW LAYER (JavaFX)            │
│   Screens: Dashboard, Inventory, Waste,     │
│   Restock, Order Tracker, EoJ Report        │
└────────────────────┬────────────────────────┘
                     │ Events / UI Bindings
┌────────────────────▼────────────────────────┐
│          CONTROLLER / SERVICE LAYER         │
│   AlertEngine | RestockService | Reports    │
└────────────────────┬────────────────────────┘
                     │ DAO calls
┌────────────────────▼────────────────────────┐
│            DATA ACCESS LAYER (DAO)          │
│  InventoryDAO | WasteDAO | OrderDAO         │
│  HaltDAO | DatabaseConnection               │
└────────────────────┬────────────────────────┘
                     │ JDBC
┌────────────────────▼────────────────────────┐
│         PERSISTENCE (SQLite .db file)       │
│         railpantry.db (Local Disk)          │
└─────────────────────────────────────────────┘
```

### 6.2 Key Architectural Decisions

- **Offline First:** SQLite's file-based, zero-installation nature makes it ideal for deployment on a train laptop with no network dependency.
- **Background Services:** A `ScheduledExecutorService` runs every 15 minutes, querying the database for items nearing their expiry threshold and pushing non-blocking UI alerts.
- **Restocking Formula:**
  ```
  OrderQty = (AvgDemandPerHour × RemainingJourneyHours) − CurrentStock
  ```
- **Reconciliation Logic:**
  ```
  Opening Stock + Restock − Consumed − Wasted = Closing Stock
  ```
  Any discrepancy is flagged as potential revenue leakage.

---

## 7. Database Schema

The application uses a single **SQLite database file** (`railpantry.db`) with the following core tables:

| Table | Purpose |
|---|---|
| `inventory` | Stores all stocked items with quantity, expiry, and pricing data |
| `waste_log` | Records each waste event with reason code and timestamp |
| `passenger_orders` | Tracks meal orders by coach, seat, and payment status |
| `halts` | Stores route stations with ETA and restock capability flag |
| `restock_orders` | Logs confirmed restocking orders per station halt |

### Key Fields

**`inventory`:**
`id` | `name` | `category` (Dairy/Meals/Beverages/Snacks) | `current_qty` | `unit` | `expiry_date` | `min_threshold` | `price_per_unit`

**`waste_log`:**
`id` | `item_id (FK)` | `qty` | `reason` (Expired/Spoiled/Overcooked/Dropped/Passenger Return) | `timestamp`

**`passenger_orders`:**
`id` | `coach` | `seat` | `meal_type` | `payment_status` (Paid/Pending/Prepaid) | `amount` | `timestamp`

---

## 8. Module Descriptions

### Module 1: Dashboard (Visual Analytics Center)

The Dashboard is the first screen a manager sees when the application launches, and it is designed to deliver full situational awareness at a glance. The **header** anchors the experience with the train name and service number (e.g., *12424 Rajdhani Express*), a live digital clock, and a dynamic countdown to the next scheduled halt (e.g., *"Next Halt: Kota in 45 min"*).

Below the header, three **metric cards** summarise the operational state of the journey:
- **Stock Health** — a colour-coded indicator (Emerald Green / Amber / Rose Red) reflecting the overall inventory condition.
- **Meals Served** — a running count of all meals prepared and distributed during the current journey.
- **Waste Cost (₹)** — the cumulative financial loss attributed to waste events, updated in real time.

Three **live charts** complete the situational picture:
- **Pie Chart** — Waste Distribution broken down by reason code (Spoiled, Expired, Overcooked, Passenger Return, Dropped).
- **Bar Chart** — Stock Capacity comparing current quantity against the minimum threshold for the five most critical inventory items.
- **Line Chart** — Order Flow tracking the volume of passenger orders mapped across each station in the route (Delhi → Mathura → Kota → Ratlam → Vadodara → Mumbai).

All charts update dynamically in real time whenever a consumption or waste event is logged in any other module.

---

### Module 2: Inventory Manager

A comprehensive stock management interface built around a **TableView** with alternating row colours for visual clarity. Each row represents a single inventory item and carries a **colour-coded status pill**:
- 🟢 **Emerald Green** — In Stock (quantity above threshold)
- 🟡 **Amber** — Low Stock (quantity at or below minimum threshold)
- 🔴 **Rose Red** — Expired (past expiry date)

A **real-time search bar** allows the manager to instantly filter the table by item name or category (Dairy, Meals, Beverages, Snacks) without navigating away from the screen.

A **quick-action sidebar** exposes the three most frequent operations without any screen transition:
- **Log Consumption** — deducts quantities per the selected recipe.
- **Log Waste** — forwards context to the Waste Logger module.
- **Edit Stock** — allows direct quantity correction for manual adjustments.

An **automated expiry alert** is raised for any item whose `expiry_date` falls within four hours of the current system time, surfacing a non-blocking floating notification to prompt immediate action.

---

### Module 3: Waste Logger

A clean, form-centric module dedicated to recording every wastage event with precision and speed. The manager selects the affected item from a dropdown, enters the quantity wasted, and picks from a standardised **reason code dropdown**:

> *Expired · Spoiled · Overcooked · Passenger Return · Dropped*

On submission, two actions occur simultaneously:
1. A new record is written to the `waste_log` table with the item ID, quantity, reason, and a system timestamp.
2. The corresponding quantity is automatically deducted from the `inventory` table.

A **pulse animation** on the submit button provides clear visual confirmation that the action was successful. All waste data is immediately reflected in the Dashboard's Pie Chart and Waste Cost metric card, ensuring analytics remain current without any manual refresh.

---

### Module 4: Halt Restocking Assistant

The most strategically powerful module in the application. It presents a list of all upcoming stations on the route, each annotated with their scheduled **ETA** and whether they support restocking operations.

A **Smart Suggestion Table** auto-calculates the recommended order quantity for every item that is running low, using the demand forecasting formula:

```
OrderQty = (AvgDemandPerHour × RemainingJourneyHours) − CurrentStock
```

The manager reviews the suggestions, adjusts quantities if required based on personal judgement or known demand patterns, and clicks **Confirm**. On confirmation:
- A **printable vendor slip (PDF)** is generated via iText Core for handover to the station supplier.
- The `inventory` table is updated immediately to reflect the incoming stock.
- A new record is written to `restock_orders` for audit purposes.

---

### Module 5: Passenger Order Tracker

The revenue module that captures the commercial side of pantry operations. Every meal order is logged against a **Coach Number** and **Seat Number**, with payment status tracked across three states:

| Status | Meaning |
|---|---|
| **Paid** | Cash or card payment collected at point of delivery |
| **Pending** | Order delivered; payment not yet collected |
| **Prepaid (IRCTC)** | Pre-booked via the IRCTC platform; amount already settled |

A **running total** of meals served and rupees collected is maintained continuously throughout the journey. This data forms the **revenue-side foundation** of the final reconciliation report — allowing the End-of-Journey module to cross-check consumption against billing and surface any discrepancy.

---

### Module 6: End-of-Journey (EoJ) Report

Activated at journey end, this module aggregates data from every DAO — `InventoryDAO`, `WasteDAO`, and `OrderDAO` — to produce a complete, auditable financial reconciliation.

**Summary Statistics:**
- Total meals served across the journey.
- Total water bottles / beverages sold.
- Overall waste percentage relative to opening stock.

**Full EoJ Table** (one row per inventory item):

| Item | Opening Stock | Restocked | Consumed | Wasted | Closing Stock | Loss (₹) |
|---|---|---|---|---|---|---|

A **Reconciliation Check** is enforced automatically:
```
Opening Stock + Restock − Consumed − Wasted = Closing Stock
```
Any discrepancy between the calculated and actual closing stock is highlighted in **Amber** and labelled *"Unaccounted / Revenue Leakage"*, providing an actionable audit flag for the contractor.

The report is exported as a **formatted PDF** via iText Core 8.0.2, including train details, journey timestamps, and a manager digital signature placeholder — ready for submission to the IRCTC contractor.

A **Reset Journey** button then wipes the current session data from the database, resetting the application cleanly for the next scheduled journey.

---

### Module 7: Admin Control Centre

A password-protected master configuration panel, separated from the regular manager workflow. Access requires **bcrypt-verified credentials** stored in the SQLite database — ensuring no unauthorised user can alter system-wide settings.

From the Admin Control Centre, an authorised administrator can:

- **Master Inventory CRUD** — Add, edit, or deprecate food items in the global menu dictionary. Changes propagate to the regular Inventory and Waste Logger screens in all subsequent journeys.
- **Route & Journey Setup** — Define the station list, scheduled ETAs, and restock capability flags for upcoming journeys.
- **User Role Management** — Create, modify, or revoke access credentials for pantry staff, with role-level permissions (Manager / Staff / Admin).
- **System Analytics** — View aggregated health metrics across multiple journeys.
- **Master Audit Trail** — Export a full CSV log of every system event (logins, stock edits, waste logs, restock confirmations) for compliance and oversight purposes.

> ⚠️ Deleting an active inventory item triggers a **cascade warning**, alerting the admin that historical waste and order records linked to that item will be affected.

---

## 9. Technology Stack

| Layer | Technology | Version | Rationale |
|---|---|---|---|
| **Build System** | Apache Maven | 3.9.6 | Dependency management and cross-compilation |
| **Runtime** | OpenJDK | 25 (LTS) | Latest stable Java, modern features |
| **UI Framework** | JavaFX (OpenJFX) | 25.0.0 | Rich desktop UI with CSS styling support |
| **UI Architecture** | FXML + Custom CSS | — | Separation of layout from logic |
| **Icons** | Ikonli (FontAwesome 6) | 12.3.1 | Professional vector icons for cards and buttons |
| **Database** | SQLite (JDBC) | 3.45.1.0 | File-based, offline, zero-install |
| **PDF Reporting** | iText Core | 8.0.2 | Precise PDF generation for EoJ reports |
| **Concurrency** | java.util.concurrent | JDK built-in | Background alert engine every 15 minutes |
| **Mobile (Future)** | Gluon Client Plugin | 1.0.22 | Native Android/iOS build without code changes |
| **Native Build (Future)** | GraalVM CE | 23.0 | Compile to .exe / .apk / .ipa |

---

## 10. Hardware & Software Requirements

### Minimum Hardware Requirements

| Component | Minimum Specification |
|---|---|
| Processor | Intel Core i3 (or equivalent, 2.0 GHz+) |
| RAM | 4 GB |
| Storage | 500 MB free disk space |
| Display | 1280 × 720 resolution |
| OS | Windows 10 / 11 (64-bit) |

### Software Requirements

| Software | Details |
|---|---|
| **JDK** | OpenJDK 25 or higher |
| **JavaFX SDK** | OpenJFX 25 (bundled via Maven) |
| **Apache Maven** | 3.9.6+ |
| **SQLite** | Embedded via `sqlite-jdbc` driver (no separate installation needed) |
| **IDE (Dev)** | VS Code with Extension Pack for Java, or IntelliJ IDEA |

> ⚠️ **No internet connection is required at runtime.** All dependencies are resolved at build time via Maven.

---

## 11. Application Flow

The application models a complete **train journey lifecycle**:

```
[App Launch] → [Login / Authentication]
     ↓
[Load Initial Inventory] → [Journey Begins]
     ↓
[In-Transit Operations]:
  ├── Log Meal Consumption → Inventory Deducted
  ├── Log Waste Events → Waste Log Updated
  ├── Monitor Alerts (every 15 min):
  │     ├── Expiry < 4hrs → RED alert
  │     └── Stock < Threshold → YELLOW alert
  └── Halt Approaching:
        ├── Review Smart Restock Suggestions
        ├── Confirm & Print Vendor Slip
        └── Update Inventory with New Stock
     ↓
[Reach Destination]
     ↓
[Generate EoJ Report] → [Export as PDF] → [Reset for New Journey]
```

---

## 12. UI Design Philosophy

The user interface is designed for the challenging environment of a working pantry car:

- **High Visibility:** Deep Navy Blue (`#1E3A8A`) primary palette with high-contrast status indicators.
- **Color-Coded Status System:**
  - 🟢 **Emerald Green** (`#10B981`) — Safe Stock / Confirmed
  - 🟡 **Amber** (`#F59E0B`) — Low Stock / Expiring Soon
  - 🔴 **Rose Red** (`#EF4444`) — Critical / Expired
- **Glassmorphism-inspired Cards** for metric panels.
- **Micro-animations:** Smooth tab transitions, pulse effects on successful form submission.
- **Minimal Typing Required:** Dropdowns, pre-populated fields, and one-click actions reduce input burden.
- **Premium Typography:** Inter / Roboto font families for readability.

---

## 13. Conclusion

RailPantry addresses a genuine and financially significant operational problem within the Indian Railways pantry ecosystem. By replacing fragmented, paper-based workflows with an intelligent, data-driven desktop application, the system aims to:

- Achieve a **90% reduction** in "gut-feeling" ordering errors at restocking halts.
- Deliver **accurate identification of waste patterns**, enabling corrective action over time.
- Ensure **zero mid-journey stockouts** for critical items such as water and packaged meals.
- Provide **contractor-ready PDF reports** that improve billing transparency and reduce revenue leakage.

The application's MVC architecture, offline-first database design, and JavaFX codebase make it not only a sound college project submission but also a **production-viable prototype** ready for pilot deployment.

---

## 14. Future Enhancements

| Enhancement | Description |
|---|---|
| **Cloud Sync Integration** | Sync journey data to a Supabase/Firebase backend when Wi-Fi is available at major stations. |
| **Android / iOS Port** | Leverage the Gluon Mobile plugin to deploy the same Java codebase as a native mobile app for pantry supervisors. |
| **AI-Powered Demand Forecasting** | Integrate ML models (seasonal demand, passenger load factors) to improve restocking accuracy. |
| **IRCTC API Integration** | Pull live passenger pre-order data from IRCTC's booking system to forecast meal demand in advance. |
| **Barcode / QR Scanning** | Allow pantry staff to scan item barcodes for faster and more accurate stock entry. |
| **Multi-Train Management** | An admin portal to manage pantry data across multiple trains/journeys simultaneously. |

---

*Synopsis prepared for MCA 2nd Semester Project Submission.*
*Application: RailPantry | Platform: JavaFX Desktop | Database: SQLite*
