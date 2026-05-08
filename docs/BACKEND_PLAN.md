# Backend Architecture - RailPantry

## 1. Core Structure
The backend follows a simple **MVC (Model-View-Controller)** pattern with **DAO (Data Access Object)** layers to keep the code clean and manageable for a college project.

## 2. Component Design

### 2.1 Model Layer (POJOs)
- `Item`: Base class for inventory items.
- `Inventory`: Item + journey-specific data (qty, expiry).
- `Order`: Passenger meal order details.
- `WasteLog`: Track individual waste events.
- `Station`: Halt station info (Name, Scheduled Arrival, Restock Capability).

### 2.2 Data Access Layer (DAO)
- `DatabaseConnection`: Single point of contact for SQLite initialization and connection pooling.
- `InventoryDAO`: CRUD operations for stock.
- `OrderDAO`: Logging meal orders.
- `WasteDAO`: Logging waste entries.
- `HaltDAO`: Managing route information.

### 2.3 Service Layer (Business Logic)
- `AlertEngine`: 
    - Implemented with `ScheduledExecutorService`.
    - Runs every 15 mins to check `expiryDate` and `minThreshold`.
    - Triggers JavaFX callbacks for UI notifications.
- `RestockService`: 
    - Logic for "Suggested Orders".
    - Formula: `(Expected Consumption - Current Stock) + Buffer`.
- `ReportService`: 
    - Aggregates data for EoJ.
    - Integrates with **iText** for PDF generation.

## 3. Technology Stack
- **Language:** Java 25 (OpenJDK).
- **Persistence:** SQLite 3.45.1.0 (JDBC).
- **PDF Generation:** iText Core 8.0.2.
- **Concurrency:** `java.util.concurrent` for background alert checks.

## 4. Error Handling
- **Database Failures:** Logging to `error.log` and showing a "System Error" dashboard alert.
- **Input Validation:** Rigorous checks on quantity and date inputs to prevent DB corruption.

## 5. Offline Sync (Simulated)
- Data is stored locally in `railpantry.db`.
- A "Sync" button (for stations) will simulate a backup upload when connectivity is available.
