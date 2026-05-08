# Screen 1: Dashboard (Overview)

## Purpose
The central nerve center of the RailPantry app. Provides immediate situational awareness for the pantry manager.

## Layout Structure
- **Top Bar (Header):**
    - `Label`: Train Name & Service Number (e.g., 12424 Rajdhani Exp)
    - `Label`: Real-time Clock (Digital Format)
    - `Label`: Next Halt countdown (e.g., "Next Halt: Kota in 45 min")
- **Main Content (Visual Analytics Layout):**
    - **Top Row (Metrics Cards):**
        - Indicator Light (Circle: Green/Yellow/Red)
        - Text: "Stock Health: [Status]"
        - "Meals Served Today: [Count]"
        - "Total Waste Cost: ₹[Amount]"
    - **Middle Row (Data Visualization):**
        - `PieChart`: **Waste Distribution** (Shows SPOILED vs EXPIRED vs RETURNED reasons).
        - `BarChart`: **Stock Capacity** (Visualizes Current Qty vs Threshold for Top 5 critical items).
    - **Bottom Row (Trends):**
        - `LineChart`: **Order Flow** (Displays orders mapped to each station halt Delhi -> Kota -> Ratlam...).
- **Bottom Status Bar:**
    - Connectivity Indicator (Simulated)
    - Sync Status (Last sync: 10 mins ago)

## Logic & Visuals
- **Dynamic Charts:** Charts update in real-time as Log Consumption or Log Waste actions are performed.
- **Color Palette:** Using distinct, modern colors for chart series (e.g., Indigo for Meals, Rose for Waste, Emerald for Stock).
- **Tooltips:** Hovering over chart nodes shows exact values (e.g., "50 Veg Meals served at Kota halt").
- **Hero Icons:** Using `FontIcon` for "Critical Alerts" and "Projected Order" notifications.
