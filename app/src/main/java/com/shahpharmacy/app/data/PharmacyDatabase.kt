package com.shahpharmacy.app.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.shahpharmacy.app.model.CashSettingEntity
import com.shahpharmacy.app.model.DailySaleEntity
import com.shahpharmacy.app.model.ExpenseEntity
import com.shahpharmacy.app.model.PurchaseEntity
import com.shahpharmacy.app.model.UserEntity

class PharmacyDatabase(context: Context) : SQLiteOpenHelper(context, "shah_pharmacy_native.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS users (
                username TEXT PRIMARY KEY,
                password TEXT NOT NULL,
                role TEXT NOT NULL,
                lastLogin TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS purchases (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                company TEXT,
                qty INTEGER NOT NULL,
                purchase REAL NOT NULL,
                sale REAL NOT NULL,
                date TEXT NOT NULL,
                batch TEXT,
                expiry TEXT,
                total REAL NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS daily_sales (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT UNIQUE NOT NULL,
                amount REAL NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                category TEXT NOT NULL,
                amount REAL NOT NULL,
                note TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cash_setting (
                id INTEGER PRIMARY KEY,
                openingCash REAL NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Not needed for v1
    }

    // Users
    @Synchronized
    fun getAllUsers(): List<UserEntity> {
        val list = mutableListOf<UserEntity>()
        val cursor: Cursor = readableDatabase.query(
            "users", null, null, null, null, null, "username ASC"
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    UserEntity(
                        username = c.getString(c.getColumnIndexOrThrow("username")),
                        password = c.getString(c.getColumnIndexOrThrow("password")),
                        role = c.getString(c.getColumnIndexOrThrow("role")),
                        lastLogin = c.getString(c.getColumnIndexOrThrow("lastLogin"))
                    )
                )
            }
        }
        return list
    }

    @Synchronized
    fun getUserByUsername(username: String): UserEntity? {
        val cursor = readableDatabase.query(
            "users", null, "username = ?", arrayOf(username), null, null, null
        )
        return cursor.use { c ->
            if (c.moveToFirst()) {
                UserEntity(
                    username = c.getString(c.getColumnIndexOrThrow("username")),
                    password = c.getString(c.getColumnIndexOrThrow("password")),
                    role = c.getString(c.getColumnIndexOrThrow("role")),
                    lastLogin = c.getString(c.getColumnIndexOrThrow("lastLogin"))
                )
            } else null
        }
    }

    @Synchronized
    fun insertUser(user: UserEntity) {
        val cv = ContentValues().apply {
            put("username", user.username)
            put("password", user.password)
            put("role", user.role)
            put("lastLogin", user.lastLogin)
        }
        writableDatabase.insertWithOnConflict("users", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    @Synchronized
    fun updateUser(user: UserEntity) {
        val cv = ContentValues().apply {
            put("password", user.password)
            put("role", user.role)
            put("lastLogin", user.lastLogin)
        }
        writableDatabase.update("users", cv, "username = ?", arrayOf(user.username))
    }

    @Synchronized
    fun deleteUser(user: UserEntity) {
        writableDatabase.delete("users", "username = ?", arrayOf(user.username))
    }

    // Purchases
    @Synchronized
    fun getAllPurchases(): List<PurchaseEntity> {
        val list = mutableListOf<PurchaseEntity>()
        val cursor = readableDatabase.query("purchases", null, null, null, null, null, "id DESC")
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    PurchaseEntity(
                        id = c.getLong(c.getColumnIndexOrThrow("id")),
                        name = c.getString(c.getColumnIndexOrThrow("name")),
                        company = c.getString(c.getColumnIndexOrThrow("company")) ?: "",
                        qty = c.getInt(c.getColumnIndexOrThrow("qty")),
                        purchase = c.getDouble(c.getColumnIndexOrThrow("purchase")),
                        sale = c.getDouble(c.getColumnIndexOrThrow("sale")),
                        date = c.getString(c.getColumnIndexOrThrow("date")),
                        batch = c.getString(c.getColumnIndexOrThrow("batch")) ?: "",
                        expiry = c.getString(c.getColumnIndexOrThrow("expiry")) ?: "",
                        total = c.getDouble(c.getColumnIndexOrThrow("total"))
                    )
                )
            }
        }
        return list
    }

    @Synchronized
    fun insertPurchase(p: PurchaseEntity): Long {
        val cv = ContentValues().apply {
            put("name", p.name)
            put("company", p.company)
            put("qty", p.qty)
            put("purchase", p.purchase)
            put("sale", p.sale)
            put("date", p.date)
            put("batch", p.batch)
            put("expiry", p.expiry)
            put("total", p.total)
        }
        return writableDatabase.insert("purchases", null, cv)
    }

    @Synchronized
    fun insertPurchases(list: List<PurchaseEntity>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (p in list) {
                val cv = ContentValues().apply {
                    put("name", p.name)
                    put("company", p.company)
                    put("qty", p.qty)
                    put("purchase", p.purchase)
                    put("sale", p.sale)
                    put("date", p.date)
                    put("batch", p.batch)
                    put("expiry", p.expiry)
                    put("total", p.total)
                }
                db.insert("purchases", null, cv)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    @Synchronized
    fun deletePurchase(id: Long) {
        writableDatabase.delete("purchases", "id = ?", arrayOf(id.toString()))
    }

    @Synchronized
    fun clearAllPurchases() {
        writableDatabase.delete("purchases", null, null)
    }

    // Daily Sales
    @Synchronized
    fun getAllSales(): List<DailySaleEntity> {
        val list = mutableListOf<DailySaleEntity>()
        val cursor = readableDatabase.query("daily_sales", null, null, null, null, null, "date DESC")
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    DailySaleEntity(
                        id = c.getLong(c.getColumnIndexOrThrow("id")),
                        date = c.getString(c.getColumnIndexOrThrow("date")),
                        amount = c.getDouble(c.getColumnIndexOrThrow("amount"))
                    )
                )
            }
        }
        return list
    }

    @Synchronized
    fun getSaleByDate(date: String): DailySaleEntity? {
        val cursor = readableDatabase.query("daily_sales", null, "date = ?", arrayOf(date), null, null, null)
        return cursor.use { c ->
            if (c.moveToFirst()) {
                DailySaleEntity(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    date = c.getString(c.getColumnIndexOrThrow("date")),
                    amount = c.getDouble(c.getColumnIndexOrThrow("amount"))
                )
            } else null
        }
    }

    @Synchronized
    fun insertOrUpdateSale(sale: DailySaleEntity) {
        val cv = ContentValues().apply {
            put("date", sale.date)
            put("amount", sale.amount)
        }
        writableDatabase.insertWithOnConflict("daily_sales", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    @Synchronized
    fun insertSales(list: List<DailySaleEntity>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (s in list) {
                val cv = ContentValues().apply {
                    put("date", s.date)
                    put("amount", s.amount)
                }
                db.insertWithOnConflict("daily_sales", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    @Synchronized
    fun deleteSale(id: Long) {
        writableDatabase.delete("daily_sales", "id = ?", arrayOf(id.toString()))
    }

    @Synchronized
    fun clearAllSales() {
        writableDatabase.delete("daily_sales", null, null)
    }

    // Expenses
    @Synchronized
    fun getAllExpenses(): List<ExpenseEntity> {
        val list = mutableListOf<ExpenseEntity>()
        val cursor = readableDatabase.query("expenses", null, null, null, null, null, "id DESC")
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    ExpenseEntity(
                        id = c.getLong(c.getColumnIndexOrThrow("id")),
                        date = c.getString(c.getColumnIndexOrThrow("date")),
                        category = c.getString(c.getColumnIndexOrThrow("category")),
                        amount = c.getDouble(c.getColumnIndexOrThrow("amount")),
                        note = c.getString(c.getColumnIndexOrThrow("note")) ?: ""
                    )
                )
            }
        }
        return list
    }

    @Synchronized
    fun insertExpense(e: ExpenseEntity): Long {
        val cv = ContentValues().apply {
            put("date", e.date)
            put("category", e.category)
            put("amount", e.amount)
            put("note", e.note)
        }
        return writableDatabase.insert("expenses", null, cv)
    }

    @Synchronized
    fun insertExpenses(list: List<ExpenseEntity>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (e in list) {
                val cv = ContentValues().apply {
                    put("date", e.date)
                    put("category", e.category)
                    put("amount", e.amount)
                    put("note", e.note)
                }
                db.insert("expenses", null, cv)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    @Synchronized
    fun deleteExpense(id: Long) {
        writableDatabase.delete("expenses", "id = ?", arrayOf(id.toString()))
    }

    @Synchronized
    fun clearAllExpenses() {
        writableDatabase.delete("expenses", null, null)
    }

    // Cash Setting
    @Synchronized
    fun getCashSetting(): CashSettingEntity? {
        val cursor = readableDatabase.query("cash_setting", null, "id = 1", null, null, null, null)
        return cursor.use { c ->
            if (c.moveToFirst()) {
                CashSettingEntity(
                    id = 1,
                    openingCash = c.getDouble(c.getColumnIndexOrThrow("openingCash"))
                )
            } else null
        }
    }

    @Synchronized
    fun setCashSetting(setting: CashSettingEntity) {
        val cv = ContentValues().apply {
            put("id", 1)
            put("openingCash", setting.openingCash)
        }
        writableDatabase.insertWithOnConflict("cash_setting", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }
}
