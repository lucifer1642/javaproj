# RailPantry Source Code Walkthrough Guide

If the examiners ask to see the code, do not just open files at random. Follow this structured path to show that you understand **Architecture**, **Data Management**, and **Business Logic**.

---

## 1. Show the Architecture (The "MVC" Pattern)
**File to open:** Project Explorer / Package Structure
- **Point to explain:** "I have followed a clean separation of concerns. All my UI screens are in the `screens` package, while the core data logic and state management are handled by the `SessionStore` singleton."
- **Why?** Examiners love seeing that you haven't put all your code in one giant file.

---

## 2. Show the "Data Brain" (SessionStore.java)
**File to open:** [SessionStore.java](file:///c:/Users/kiit/Desktop/MCA%202ND%20SEMESTER/MCA%202ND%20SEMESTER/PROJECT%20%202/javaproject/src/main/java/com/railpantry/SessionStore.java)
- **What to highlight (Lines 77–120):** Show the `ObservableList<InventoryItem>` and `ObservableList<WasteEntry>`.
- **Logic to show (Lines 326–351):** `getOrdersPerStation()`
    - **Explain:** "This method dynamically buckets orders into stations based on the train's timeline. This is what powers the line charts on the dashboard without using hardcoded data."
- **Logic to show (Lines 261–274):** `archiveCurrentJourney()`
    - **Explain:** "This logic takes a 'snapshot' of the entire journey metrics—revenue, waste percentage, and meals served—and stores it in the history before resetting for the next trip."

---

## 3. Show UI-Logic Binding (InventoryScreen.java)
**File to open:** [InventoryScreen.java](file:///c:/Users/kiit/Desktop/MCA%202ND%20SEMESTER/MCA%202ND%20SEMESTER/PROJECT%20%202/javaproject/src/main/java/com/railpantry/screens/InventoryScreen.java)
- **What to highlight (Lines 130–141):** `FilteredList` and `SortedList`.
- **Explain:** "I've used JavaFX's `FilteredList`. When a user types in the search bar, the table filters in real-time (O(n) complexity) without reloading data from a database, making the UI feel extremely fast."
- **Logic to show (Lines 144–165):** The `RowFactory`.
    - **Explain:** "This is the 'Health Check' logic. Every row is dynamically styled: if an item is expired, it turns Red; if it's under the threshold, it turns Yellow. This is reactive programming."

---

## 4. Show Professional Error Handling & Modals
**File to open:** `InventoryScreen.java` (Lines 267–305: `handleLogWaste`)
- **Explain:** "I've used custom `Dialog` classes with `Spinner` and `ComboBox` inputs. This prevents 'Dirty Data' entry—users can't waste more items than are actually in stock because the Spinner is bounded by `sel.qty`."

---

## 5. Show the Entry Point (Main.java)
**File to open:** `Main.java`
- **Point to explain:** "This is where the JavaFX stage is initialized and the CSS is loaded. I use a global stylesheet to keep the design consistent across all 12 screens."

---

### 💡 Pro-Tips for the Code Review:
1.  **Use Ctrl+Click:** When showing code, Ctrl+Click on method names to "jump" to their definitions. It shows you know your way around the IDE.
2.  **Mention "Clean Code":** Mention that you use descriptive variable names (`openingQty`, `wastePct`) and Java's modern `Stream API` for calculations.
3.  **The "Why" matters more than the "What":** Don't just say "This is a loop." Say "This loop ensures that every item is checked against its expiry date every time the screen refreshes."
