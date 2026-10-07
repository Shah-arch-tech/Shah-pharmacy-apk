package com.shahpharmacy.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shahpharmacy.app.data.PharmacyRepository
import com.shahpharmacy.app.model.CashSettingEntity
import com.shahpharmacy.app.model.DailySaleEntity
import com.shahpharmacy.app.model.ExpenseEntity
import com.shahpharmacy.app.model.PurchaseEntity
import com.shahpharmacy.app.model.StockItem
import com.shahpharmacy.app.model.TimeframeTotals
import com.shahpharmacy.app.model.UserEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class NavigationTab(val title: String) {
    DASHBOARD("Dashboard"),
    PURCHASE("Purchase"),
    SALE("Daily Sale"),
    EXPENSE("Expenses"),
    STOCK("Stock"),
    REPORTS("Reports"),
    CASH("Cash In Hand"),
    SETTINGS("Settings")
}

class PharmacyViewModel(application: Application) : AndroidViewModel(application) {
    val repository = PharmacyRepository(application)

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentTab = MutableStateFlow(NavigationTab.DASHBOARD)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    val purchases: StateFlow<List<PurchaseEntity>> = repository.allPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<DailySaleEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cashSetting: StateFlow<CashSettingEntity?> = repository.cashSetting
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CashSettingEntity(1, 0.0))

    val users: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockList: StateFlow<List<StockItem>> = purchases
        .combine(sales) { pList, _ ->
            repository.calculateStock(pList)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cashInHand: StateFlow<Double> = combine(
        cashSetting, sales, purchases, expenses
    ) { cash, sList, pList, eList ->
        val opening = cash?.openingCash ?: 0.0
        val totalSale = sList.sumOf { it.amount }
        val totalPurchase = pList.sumOf { it.total }
        val totalExpense = eList.sumOf { it.amount }
        opening + totalSale - totalPurchase - totalExpense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayTotals: StateFlow<TimeframeTotals> = combine(
        sales, purchases, expenses
    ) { sList, pList, eList ->
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        repository.calculateTotals(sList, pList, eList, today, today)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeframeTotals(0.0, 0.0, 0.0, 0.0))

    val weeklyTotals: StateFlow<TimeframeTotals> = combine(
        sales, purchases, expenses
    ) { sList, pList, eList ->
        val cal = Calendar.getInstance()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -6)
        val startWeekStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        repository.calculateTotals(sList, pList, eList, startWeekStr, todayStr)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeframeTotals(0.0, 0.0, 0.0, 0.0))

    val monthlyTotals: StateFlow<TimeframeTotals> = combine(
        sales, purchases, expenses
    ) { sList, pList, eList ->
        val monthStr = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        repository.calculateTotals(sList, pList, eList, "$monthStr-01", "$monthStr-31")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeframeTotals(0.0, 0.0, 0.0, 0.0))

    val allTimeTotals: StateFlow<TimeframeTotals> = combine(
        sales, purchases, expenses
    ) { sList, pList, eList ->
        repository.calculateTotals(sList, pList, eList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeframeTotals(0.0, 0.0, 0.0, 0.0))

    init {
        viewModelScope.launch {
            repository.initialize()
        }
    }

    fun selectTab(tab: NavigationTab) {
        val user = _currentUser.value
        if (user?.role == "staff") {
            // Staff allowed tabs
            if (tab == NavigationTab.PURCHASE || tab == NavigationTab.REPORTS || tab == NavigationTab.CASH) {
                viewModelScope.launch { _uiMessage.emit("Access restricted to Admin role.") }
                return
            }
        }
        _currentTab.value = tab
    }

    fun login(username: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.authenticate(username, pass)
            if (user != null) {
                _currentUser.value = user
                _currentTab.value = NavigationTab.DASHBOARD
                onResult(true, "Login successful")
            } else {
                onResult(false, "Invalid username or password.")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentTab.value = NavigationTab.DASHBOARD
    }

    fun changePassword(oldPass: String, newPass: String, confirmPass: String, onResult: (Boolean, String) -> Unit) {
        val user = _currentUser.value ?: return
        if (user.password != oldPass) {
            onResult(false, "Current password is incorrect.")
            return
        }
        if (newPass.length < 4) {
            onResult(false, "New password must be at least 4 characters.")
            return
        }
        if (newPass != confirmPass) {
            onResult(false, "New passwords do not match.")
            return
        }
        viewModelScope.launch {
            repository.changePassword(user.username, newPass)
            _currentUser.value = user.copy(password = newPass)
            onResult(true, "Password changed successfully.")
        }
    }

    fun addStaff(username: String, pass: String, onResult: (Boolean, String) -> Unit) {
        val user = _currentUser.value
        if (user?.role != "admin") {
            onResult(false, "Only admin can add staff.")
            return
        }
        viewModelScope.launch {
            val success = repository.addStaff(username, pass)
            if (success) {
                onResult(true, "Staff user '$username' added successfully.")
            } else {
                onResult(false, "Username already exists or password is less than 4 chars.")
            }
        }
    }

    fun deleteUser(target: UserEntity) {
        val user = _currentUser.value
        if (user?.role != "admin") return
        if (target.username == "admin") {
            viewModelScope.launch { _uiMessage.emit("Admin account cannot be deleted.") }
            return
        }
        viewModelScope.launch {
            repository.deleteUser(target)
            _uiMessage.emit("User '${target.username}' deleted.")
        }
    }

    fun addPurchase(
        name: String,
        company: String,
        qty: Int,
        purchasePrice: Double,
        salePrice: Double,
        date: String,
        batch: String,
        expiry: String,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank() || qty <= 0) {
            viewModelScope.launch { _uiMessage.emit("Please enter medicine name and valid quantity.") }
            return
        }
        val safeDate = if (date.isBlank()) SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) else date
        viewModelScope.launch {
            repository.addPurchase(
                PurchaseEntity(
                    name = name.trim(),
                    company = company.trim(),
                    qty = qty,
                    purchase = purchasePrice,
                    sale = salePrice,
                    date = safeDate,
                    batch = batch.trim(),
                    expiry = expiry.trim(),
                    total = qty * purchasePrice
                )
            )
            _uiMessage.emit("Purchase saved.")
            onSuccess()
        }
    }

    fun deletePurchase(purchase: PurchaseEntity) {
        viewModelScope.launch {
            repository.deletePurchase(purchase)
            _uiMessage.emit("Purchase deleted.")
        }
    }

    fun saveDailySale(date: String, amount: Double, onSuccess: () -> Unit) {
        if (date.isBlank() || amount < 0) {
            viewModelScope.launch { _uiMessage.emit("Please enter valid date and amount.") }
            return
        }
        viewModelScope.launch {
            repository.saveOrUpdateSale(date, amount)
            _uiMessage.emit("Daily sale for $date saved.")
            onSuccess()
        }
    }

    fun deleteSale(sale: DailySaleEntity) {
        viewModelScope.launch {
            repository.deleteSale(sale)
            _uiMessage.emit("Daily sale deleted.")
        }
    }

    fun addExpense(
        date: String,
        category: String,
        amount: Double,
        note: String,
        onSuccess: () -> Unit
    ) {
        if (amount <= 0) {
            viewModelScope.launch { _uiMessage.emit("Please enter a valid amount greater than 0.") }
            return
        }
        val safeDate = if (date.isBlank()) SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) else date
        viewModelScope.launch {
            repository.addExpense(
                ExpenseEntity(
                    date = safeDate,
                    category = category.trim(),
                    amount = amount,
                    note = note.trim()
                )
            )
            _uiMessage.emit("Expense saved.")
            onSuccess()
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _uiMessage.emit("Expense deleted.")
        }
    }

    fun updateOpeningCash(amount: Double) {
        viewModelScope.launch {
            repository.updateOpeningCash(amount)
            _uiMessage.emit("Opening cash updated to Rs. ${String.format(Locale.US, "%,.2f", amount)}")
        }
    }

    fun importCsv(content: String, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.importCsvText(content)
            _uiMessage.emit("$count purchase records imported.")
            onResult(count)
        }
    }

    fun exportBackupJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportJsonBackup()
            onResult(json)
        }
    }

    fun restoreBackupJson(json: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = repository.importJsonBackup(json)
            if (ok) {
                _uiMessage.emit("Backup restored successfully.")
            } else {
                _uiMessage.emit("Failed to restore backup: invalid format.")
            }
            onResult(ok)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _uiMessage.emit("All pharmacy data cleared.")
        }
    }
}
