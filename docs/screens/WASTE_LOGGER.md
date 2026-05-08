# Screen 3: Waste Logger

## Purpose
Log spoiled or expired stock with detailed reason codes to help identify patterns of loss.

## Layout Structure
- **VBox Container (Centered Form):**
    - `Label`: "Log New Wastage Event" (Heading style)
    - `ComboBox`: Select Inventory Item (Searchable)
    - `Spinner<Double>`: Quantity to log as waste
    - `ComboBox`: Reason Code
        - Expired
        - Spoiled
        - Overcooked
        - Passenger Return
        - Dropped / Spilled
    - `TextArea`: Optional notes
    - `Button`: Submit Entry (Deep Rose/Red style)

## Logic
- **Validation:** Ensure `quantity <= currentStock`.
- **Database Action:**
    1. Create entry in `waste_log` table.
    2. Subtract quantity from `inventory` table for the selected item.
- **UI Update:** Clear form and show a brief "Success" banner/toast message.
