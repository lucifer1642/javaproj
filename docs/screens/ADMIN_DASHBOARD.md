# Screen 6: Admin Control Center

## Purpose
A highly secured, master override dashboard where system administrators or high-ranking officials can override daily operations, add/remove core menu items natively, and view system health.

## Layout Structure

- **Sidebar Navigation:**
    - `Button`: Master Inventory (Add/Edit/Remove globally)
    - `Button`: Route & Journey Setup
    - `Button`: User Roles & Authentication
    - `Button`: Analytics & System Health

- **Active Workspace (Center):**
  *(e.g., When "Master Inventory" is selected)*
    - `HBox`: Action Bar
        - `TextField`: Search Master Database
        - `Button`: "+ Add New Menu Item"
        - `Button`: "Remove Selected Item"
    - `TableView`: Master Database View
        - `TableColumn`: ID 
        - `TableColumn`: Item Name
        - `TableColumn`: Category (Meal, Beverage, Snack)
        - `TableColumn`: Base Price (₹)
        - `TableColumn`: Critical Threshold Level
        - `TableColumn`: Status (Active/Deprecated)
        
- **Add/Edit Item Dialog/form (Overlay):**
    - `TextField`: Item Name
    - `ComboBox`: Category
    - `TextField`: Price
    - `TextField`: Minimum Warning Threshold
    - `Button`: "Save to Database"

- **System Actions (Footer):**
    - `Button`: Force Database Sync/Backup 
    - `Button`: Generate Master Audit Trail (CSV)

## Logic & Security
- **Authentication Bypass:** Requires a master password (e.g., `IRCTC-ADMIN` or hashed DB verify).
- **CRUD Hooks:** Adding or editing meals here will permanently alter the SQLite `Item` dictionary table, propagating the changes to the regular Inventory and Waste Logger screens in future journeys.
- **Danger Zone:** Deleting an active item prompts a cascade warning to avoid breaking historical relation metrics.
