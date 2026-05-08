# Screen 2: Inventory Management

## Purpose
Visual database of all food and beverage items loaded onto the train.

## Layout Structure
- **Toolbar (Top):**
    - `TextField`: Search items by name or category.
    - `Button`: Refresh List.
    - `Button`: Add New Item (Manual load).
- **Center: TableView**
    - `TableColumn`: Item Name
    - `TableColumn`: Category (Dairy, Meal, Snack, Drink)
    - `TableColumn`: Current Qty (with Units)
    - `TableColumn`: Expiry Date
    - `TableColumn`: Health Status (Visual Pill/Badge)
- **Side Panel (Quick Actions):**
    - `Button`: Log Consumption
    - `Button`: Log Waste
    - `Button`: Edit Selected Item

## Visual Logic (Color Coding)
- **Red Row:** `expiryDate < now`.
- **Orange Row:** `expiryDate` between `now` and `now + 4 hours`.
- **Yellow Row:** `currentQty <= minThreshold`.
- **Grey/White Row:** Normal status.

## Controller Logic
- Data is fetched from `InventoryDAO` and wrapped in a `FilteredList` for real-time searching.
- Custom `TableCell` factory to handle the color-coded health badges.
