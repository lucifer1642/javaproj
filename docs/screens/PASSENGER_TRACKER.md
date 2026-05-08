# Screen 6: Passenger Order Tracker (Optional/Impressive)

## Purpose
Track individual meal orders by coach and seat, managing payments and real-time revenue tally.

## Layout Structure
- **Coach Selector (Top):**
    - `HBox` with buttons for COACH: A1, A2, B1, B2, S1, S2...
- **Seat Map / Order List (Center):**
    - `TableView`:
        - `TableColumn`: Seat Number
        - `TableColumn`: Meal Type
        - `TableColumn`: Order Time
        - `TableColumn`: Payment Status (Paid / Pending / IRCTC Prepaid)
        - `TableColumn`: Price
- **Action Sidebar:**
    - `Button`: Add New Order
    - `Button`: Mark as Paid
    - `Button`: Print Receipt
- **Revenue Footer:**
    - `Label`: "Total Cash Collected: ₹[Amount]"
    - `Label`: "Total Prepaid: ₹[Amount]"

## Logic
- **Real-time Revenue:** Automatically updates when an order is added or payment status changes.
- **Pre-fill:** Syncs with "Today's meal count" on the Dashboard.
- **Persistence:** Logs data to the `passenger_orders` table.

## Aesthetics
- Use a "Ticket" style card for each order.
- Color coding for payment:
    - **Green:** Paid
    - **Amber:** Pending
    - **Blue:** IRCTC Prepaid
