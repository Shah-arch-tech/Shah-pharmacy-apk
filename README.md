# Shah Pharmacy Management System (Android)

Shah Pharmacy is a full-featured, modern Android pharmacy management system rewritten from the ground up using **Kotlin**, **Jetpack Compose**, **Material 3**, and **SQLite**.

## Features

- **Authentication & Role-Based Access Control**:
  - Secure login screen for staff and administrators.
  - **Admin** role: Full access to Dashboard, Medicine Purchases, Daily Sales, Expenses, Stock Management, Financial Reports, Cash In Hand, and User Management.
  - **Staff** role: Access to Dashboard, Daily Sales, Expenses, and Stock.
  - Staff user management (add new staff members, delete staff accounts).
  - Password change functionality with verification.
  - Default credentials:
    - Admin: `admin` / `1234`
    - Staff: `staff` / `1234`

- **Dashboard**:
  - Real-time KPI summary cards: Today's Sale, Today's Purchase, Today's Expenses, and Cash In Hand.
  - Quick action buttons to record daily sales, medicine purchases, and expenses.
  - Stock value summary: Purchase Value, Sale Value, and Potential Gross Margin.
  - Estimated net profit tracking with accounting guidance.

- **Medicine Purchases & Inventory**:
  - Add medicine inventory entries with name, company, quantity, purchase price, sale price, date, batch number, and expiry date.
  - Batch CSV import for rapid inventory entry.
  - Recent purchase log with delete capabilities.

- **Daily Sales**:
  - Simplified day-end total sales entry (designed specifically for pharmacies tracking daily total register revenue).
  - Auto-prefill and update support for today's sales.
  - Historical daily sales log with date filtering and deletion.

- **Expense Tracking**:
  - Categorized expenses: Electricity, Rent, Salary, Transport, and Other.
  - Detailed expense records with dates, notes, and formatted Pakistani Rupee amounts.

- **Stock Management**:
  - Live stock metrics: Total Purchase Value, Potential Sale Value, Potential Margin, and Medicine Lines.
  - Instant medicine search and company filtering.
  - Per-medicine breakdown of available quantity, buy/sell prices, and inventory valuation.

- **Financial Reports & Analytics**:
  - Timeframe tabs: Weekly (last 7 days), Monthly (current month), and All-Time cumulative.
  - Automated calculation of Estimated Net Profit using weighted stock markup margins.
  - Side-by-side performance summary table.

- **Cash In Hand Reconciliation**:
  - Cash formula: `Opening Cash + Sales − Purchases − Expenses = Cash In Hand`.
  - Configurable opening cash balance with instant balance updates.

- **Data Backup & Restore**:
  - Export complete pharmacy database as formatted JSON with copy-to-clipboard.
  - Restore backup from JSON text.
  - Automatic migration from legacy SQLite databases if present.

## Architecture

- **UI**: Jetpack Compose, Material 3, Adaptive Navigation (Bottom Bar on phones, Navigation Rail on tablets and landscape).
- **Architecture**: MVVM with AndroidViewModel, Kotlin Coroutines, and StateFlow.
- **Persistence**: Native Android SQLite with backward-compatible schema and automatic seeding.
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 16 (API 36)

## How to Install on Android Phone

### Option 1: Direct APK Installation
1. Locate the built APK file at:
   `app/build/outputs/apk/debug/app-debug.apk`
2. Send or copy `app-debug.apk` to your Android phone (via USB cable, WhatsApp, Telegram, Google Drive, or Email).
3. On your Android phone, tap the APK file and select **Install** (enable "Install unknown apps" if prompted).
4. Launch **Shah Pharmacy** and log in with:
   - **Admin**: `admin` / `1234`
   - **Staff**: `staff` / `1234`

### Option 2: Build APK with Gradle Wrapper
If you cloned or exported the project, build the APK locally or on CI with:
```bash
./gradlew assembleDebug
```
(On Windows: `gradlew.bat assembleDebug`)
The output APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

