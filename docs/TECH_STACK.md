# Technical Stack Specification - RailPantry

To ensure the project is modern, multi-platform (Desktop & Mobile), and robust, we will use the following precisely defined stack.

## 1. Core Platform
- **JDK:** OpenJDK 25 (LTS)
  - *Rationale:* Latest stable features and high performance.
- **Build System:** Maven 3.9.6
  - *Rationale:* Industry standard for Java dependency management and cross-compilation.

## 2. UI / Frontend (Cross-Platform)
- **Framework:** JavaFX 25 (OpenJFX)
  - **Extension for Mobile:** Gluon Mobile 6.0.0
- **UI Architecture:** FXML (for layouts) + Screen-independent CSS.
- **Responsive Design:** Custom Layout Manager using JavaFX `Region` and `Bindings` to resize for Laptop vs Tablet screens.
- **Icons:** Ikonli 12.3.1 (FontAwesome 6.x pack)
- **Aesthetics:** Custom CSS Design System (Glassmorphism & Material 3 influenced).

## 3. Backend & Storage
- **Database:** SQLite 3.45.1.0
  - **Driver:** `sqlite-jdbc` 3.45.1.0
  - *Rationale:* File-based, zero-installation, perfect for offline pantry use.
- **Reporting Engine:** iText Core 8.0.2 (Open Source version)
  - *Rationale:* Precise PDF generation for end-of-journey reports.

## 4. Multi-Platform Support (Desktop & Mobile)
- **AOT Compilation:** GraalVM Community Edition 23.0
  - **Plugin:** Gluon Client Maven Plugin
  - *Details:* This allows the Java code to be compiled into a Native Image:
    - `.exe` for Windows (Desktop)
    - `.apk` for Android (Mobile)
    - `.ipa` for iOS (Mobile)

## 5. Security & Sync
- **Encryption:** SQLCipher (Optional) for DB security.
- **Simulation Layer:** Mock HTTP Client for station-based sync simulation.

## 6. Version Breakdown Summary

| Component | Version | Purpose |
| :--- | :--- | :--- |
| JavaFX | 25.0.0 | Main UI Framework |
| SQLite JDBC | 3.45.1.0 | Local Data Storage |
| iText Core | 8.0.2 | PDF Generation |
| Ikonli | 12.3.1 | Premium Vector Icons |
| Maven Wrapper | 3.9.6 | Ensures consistent builds |
| Gluon Client | 1.0.22 | Native Mobile/Desktop porting |

---

### Implementation Note for College Project
We will focus on the **Desktop (Windows)** build first as the primary target, but by using **JavaFX + Gluon**, the codebase remains 100% compatible for an Android build without rewriting a single line of logic.
