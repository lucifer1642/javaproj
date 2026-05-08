# Screen 5: End of Journey Report (Simulated)

## Purpose
Aggregate all journey data into a final reconciliation report for the contractor.

## Layout Structure
- **Report Dashboard:**
    - `Label`: Journey Recap (Delhi -> Mumbai)
    - `SummaryBox`: 
        - Total Water Bottles Sold
        - Total Meals Served
        - Total Waste Percentage (%)
- **EoJ Table (Full Data):**
    - `TableColumn`: Item
    - `TableColumn`: Opening Stock
    - `TableColumn`: Final Stock
    - `TableColumn`: Total Consumed
    - `TableColumn`: Total Wasted
    - `TableColumn`: Loss to Waste (₹)
- **Footer:**
    - `Button`: Export Final Report (PDF)
    - `Button`: Reset App for New Journey (Warning: Deletes local session data)

## Report Logic
- Aggregates data from `InventoryDAO`, `WasteDAO`, and `OrderDAO`.
- Performs a final "Reconciliation Check": `Opening + Restock - Consumed - Wasted = Closing`.
- Any discrepancy is highlighted in Amber as "Unaccounted/Revenue Leakage".

## Export Details
- PDF includes timestamps, train info, and manager digital signature placeholder.
