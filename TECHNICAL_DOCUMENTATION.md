# RailPantry — Technical Documentation
**Project Title:** IRCTC Onboard Pantry Management System
**Target Platform:** Windows Desktop (JRE 17+)
**Author:** [Your Name]

---

## 1. Project Overview
RailPantry is a JavaFX-based desktop application designed to digitize the manual workflows of pantry car operations on long-distance Indian trains. It focuses on inventory accuracy, waste reduction, and data-driven restocking decisions.

## 2. System Architecture
The system follows the **Model-View-Controller (MVC)** design pattern to ensure high maintainability and scalability.

- **View Layer**: Implemented using FXML and styled with custom CSS. It provides a responsive and high-contrast UI suitable for high-pressure environments.
- **Controller Layer**: Handles user events, input validation, and invokes service/DAO methods.
- **DAO Layer (Data Access Object)**: Encapsulates all SQL logic. Uses JDBC to communicate with the local SQLite database.
- **Model Layer**: Plain Old Java Objects (POJOs) representing entities like `Item`, `Order`, `WasteRecord`, etc.

## 3. Database Design (SQLite)
The application uses an embedded **SQLite** database (`railpantry.db`). This was chosen for its zero-configuration nature, making it ideal for offline use on train-deployed laptops.

### 3.1 ER Diagram Summary
- **Inventory** (1:M) **WasteLog**: An inventory item can have multiple waste entries.
- **Inventory** (1:M) **OrderDetails**: Ingredients from inventory are linked to passenger orders.
- **Route/Halts** (1:1) **RestockOrders**: Each halt can trigger a restocking order.

### 3.2 Key Table Schema
- `inventory`: `id`, `name`, `qty`, `unit`, `expiry_date`, `threshold`, `price`.
- `waste_log`: `id`, `item_id`, `qty`, `reason_code`, `timestamp`.
- `passenger_orders`: `id`, `coach`, `seat`, `meal_type`, `amount`, `status`.

## 4. Key Technical Features & Implementation

### 4.1 Real-Time Alert Engine
The system employs a `ScheduledExecutorService` that runs in the background. It periodically checks the `inventory` table for:
- **Expiry Risk**: Items with `expiry_date` < 4 hours from current time.
- **Stock Risk**: Items with `qty` < `threshold`.
These are pushed to the UI as non-blocking notifications.

### 4.2 Smart Restocking Algorithm
To prevent mid-journey stockouts, the system calculates the "Target Stock" needed to reach the destination:
`Suggested_Order = (Avg_Consumption_Rate * Remaining_Hours) - Current_Stock`
This ensures the manager orders exactly what is needed at the next station.

### 4.3 PDF Generation (iText 8)
At the end of every journey, the `EOJReportScreen` aggregates data into a professional PDF. 
- **Libraries used:** `itext-core`.
- **Content:** Journey summary, detailed stock reconciliation, financial loss due to waste, and revenue breakdown.

## 5. Security & Access Control
- **Authentication:** Password-protected login for Managers and Admins.
- **Bcrypt Hashing:** User passwords are not stored in plain text but as secure Bcrypt hashes.
- **Admin Center:** A restricted module for managing the "Master Menu" and Route details.

## 6. Development Stack
- **Language:** Java 17+
- **Framework:** JavaFX 21
- **Build Tool:** Apache Maven
- **Database:** SQLite (JDBC Driver: `sqlite-jdbc`)
- **Reporting:** iText PDF
- **UI Icons:** Ikonli / FontAwesome

## 7. Installation & Deployment
1. **Prerequisites:** OpenJDK 17 and Maven installed.
2. **Build:** Run `mvn clean install` to resolve dependencies.
3. **Execution:** Launch via `mvn javafx:run` or the provided `.bat` file.
4. **Data Persistence:** The `railpantry.db` file is automatically created in the root directory on first launch.

## 8. Conclusion
RailPantry successfully bridges the gap between traditional manual logs and modern digital tracking. By providing an offline-first, data-driven interface, it ensures operational efficiency and financial transparency for IRCTC catering contractors.
