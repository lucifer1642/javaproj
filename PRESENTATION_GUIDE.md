# RailPantry Presentation Guide — Slide-by-Slide

This guide is designed to help you deliver a professional, technical, and high-impact presentation.

---

## Slide 1: Title Slide
**Visuals:** RailPantry Logo (if any) or a high-quality train image.
**Text:**
- **RailPantry: IRCTC Pantry Management System**
- Modernizing Onboard Railway Catering Operations
- Presented by: [Your Name]
- Course: MCA 2nd Semester
- Tech Stack: JavaFX | SQLite | Maven | iText PDF

---

## Slide 2: The Core Problem (The "Pain Point")
**Talking Points:**
- **Manual Chaos:** Most pantry cars today use paper registers.
- **Wastage:** ₹8–12 crore is lost annually due to food expiry and poor tracking.
- **Stockouts:** Trains often run out of water or meals mid-journey because restocking is based on "guesswork."
- **Revenue Leakage:** No way to cross-check what was cooked vs. what was billed.

---

## Slide 3: Existing vs. Non-Existing Solution
**Comparison Table:**
| Feature | Manual System (Existing) | RailPantry (Our Solution) |
|---|---|---|
| **Inventory** | Paper-based, slow, error-prone | Digital, Real-time, Color-coded |
| **Expiry Alerts** | None (Visual checks only) | **Auto-alerts** (4 hours before) |
| **Restocking** | Intuition / Guesswork | **Smart Prediction** (Demand-based) |
| **Reporting** | Messy hand-written logs | **One-click PDF** (Professional Audit) |

---

## Slide 4: Technical Architecture (The "Brain")
**Visuals:** MVC Diagram.
- **View (JavaFX):** Clean, high-visibility UI with CSS-based "Glassmorphism."
- **Controller (Java Logic):** Background services for alerts and smart restocking formulas.
- **DAO (Data Access Object):** Clean separation of database queries from UI logic.
- **Storage (SQLite):** An **Offline-First** local database. No internet? No problem.

---

## Slide 5: Key Technical Modules
1. **Live Dashboard:** Situational awareness with charts (Pie, Bar, Line).
2. **Waste Logger:** Categorized loss tracking (Expired, Dropped, Overcooked).
3. **Smart Restocking:** Auto-calculates what to buy at the next station based on journey time left.
4. **Passenger Tracker:** Maps meals to Coach/Seat for accurate billing.

---

## Slide 6: Database & Storage Strategy
**Text:**
- **Technology:** SQLite (JDBC)
- **Why?** Zero installation required. It's a single `.db` file that travels with the laptop.
- **Core Tables:**
    - `inventory`: Stock & Expiry.
    - `waste_log`: Reasons for loss.
    - `passenger_orders`: Revenue tracking.
    - `halts`: Route planning.

---

## Slide 7: The "Smart" Algorithm (The USP)
**Talking Points:**
Explain your **Restocking Formula**:
`OrderQty = (AvgDemandPerHour × RemainingJourneyHours) − CurrentStock`
*Explain:* "If we are 5 hours away and sell 10 water bottles an hour, but only have 12 in stock, the system suggests ordering 38 at the next halt."

---

## Slide 8: Future Roadmap
- **Cloud Sync:** Sync to Supabase when the train hits Wi-Fi.
- **Mobile App:** An Android version for pantry staff to use on the move.
- **AI Forecasting:** Predicting demand based on passenger load and weather.

---

## Slide 9: Conclusion & Q&A
- **Summary:** RailPantry isn't just an app; it's a productivity tool for the Indian Railways.
- "I'm now open for any technical or functional questions."

---

### Tips for Success:
- **Mention "Offline-First":** This is a huge selling point for railway environments.
- **Highlight "iText PDF":** Show that you generate professional, unalterable audit reports.
- **Demo the "Alerts":** If possible, show a low-stock alert popping up.
