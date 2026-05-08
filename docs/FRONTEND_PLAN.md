# Frontend Design Plan - RailPantry

## 1. Design Philosophy

- **Modern & Premium:** Using sleek CSS, glassmorphism-inspired cards, and high-quality typography (Inter/Roboto).
- **High Visibility:** High contrast for low-light pantry environments.
- **Micro-animations:** Subtle transitions when switching tabs or submitting forms.

## 2. Color Palette

- **Primary:** `#1E3A8A` (Deep Navy Blue - Trust/Process)
- **Secondary:** `#10B981` (Emerald Green - Safe Stock)
- **Warning:** `#F59E0B` (Amber - Low Stock/Expiring Soon)
- **Danger:** `#EF4444` (Rose Red - Expired/Critical)
- **Background:** `#F9FAFB` (Light Grey for Dashboard) / `#111827` (Dark Mode option)

## 3. Screen Breakdowns

### Screen 1: Dashboard (Visual Analytics Center)

- **Header:** Train Number (12424 Rajdhani), Current Station, Next Halt Countdown.
- **Top Metrics:** High-impact cards for "Low Stock", "Meals Served", and "Waste Cost".
- **Visual Charts:** 
    - `PieChart`: Breakdown of waste reasons (Spoiled vs Expired).
    - `BarChart`: Stock levels vs. Thresholds for critical items.
    - `LineChart`: Revenue/Consumption trend over the journey duration.

### Screen 2: Inventory Manager
- **TableView:** Modern styling with alternating row colors.
- **Status Pills:** Color-coded badges for "In Stock", "Low", "Expired".
- **Search Bar:** Real-time filtering by item name or category.
- **Quick Action Sidebar:** Log Consumption, Log Waste, Edit Stock.

### Screen 3: Waste Logger
- **Form-centric:** Clean, large input fields.
- **Dropdowns:** Simple categorization of waste reasons.
- **Submit Button:** Visual feedback (pulse effect) on successful log.

### Screen 4: Halt Restocking Assistant
- **Halt Table:** List of upcoming stations with ETA.
- **Smart Order Table:** Suggested quantities vs Stock levels.
- **Print Preview:** Digital mockup of the vendor slip.

### Screen 5: Analytics & Reports
- **Summary Dashboard:** Visual representation of EoJ data.
- **Export UI:** Big "Generate PDF" button with progress bar.

## 4. UI Library & Styling

- **JavaFX CSS:** Custom stylesheet `style.css` for all components.
- **Charts:** Native JavaFX Chart API with custom CSS for a premium look.
- **Icons:** FontAwesome (via Ikonli) for visual cues on cards and buttons.
- **Dialogs:** Custom styled alerts instead of default Windows-style popups.
