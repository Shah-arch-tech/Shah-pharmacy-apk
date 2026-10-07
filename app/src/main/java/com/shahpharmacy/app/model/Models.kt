package com.shahpharmacy.app.model

data class UserEntity(
    val username: String,
    val password: String,
    val role: String, // "admin" or "staff"
    val lastLogin: String? = null
)

data class PurchaseEntity(
    val id: Long = 0,
    val name: String,
    val company: String = "",
    val qty: Int = 0,
    val purchase: Double = 0.0,
    val sale: Double = 0.0,
    val date: String,
    val batch: String = "",
    val expiry: String = "",
    val total: Double = 0.0
)

data class DailySaleEntity(
    val id: Long = 0,
    val date: String,
    val amount: Double = 0.0
)

data class ExpenseEntity(
    val id: Long = 0,
    val date: String,
    val category: String, // Electricity, Rent, Salary, Transport, Other
    val amount: Double = 0.0,
    val note: String = ""
)

data class CashSettingEntity(
    val id: Int = 1,
    val openingCash: Double = 0.0
)

data class StockItem(
    val name: String,
    val company: String,
    val qty: Int,
    val purchase: Double,
    val sale: Double,
    val purchaseValue: Double,
    val saleValue: Double
)

data class TimeframeTotals(
    val sale: Double,
    val purchase: Double,
    val expense: Double,
    val estimatedProfit: Double
)
