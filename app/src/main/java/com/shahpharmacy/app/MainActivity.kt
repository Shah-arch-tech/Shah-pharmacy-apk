package com.shahpharmacy.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shahpharmacy.app.ui.NavigationTab
import com.shahpharmacy.app.ui.PharmacyViewModel
import com.shahpharmacy.app.ui.components.AppHeader
import com.shahpharmacy.app.ui.screens.CashScreen
import com.shahpharmacy.app.ui.screens.DailySaleScreen
import com.shahpharmacy.app.ui.screens.DashboardScreen
import com.shahpharmacy.app.ui.screens.ExpenseScreen
import com.shahpharmacy.app.ui.screens.LoginScreen
import com.shahpharmacy.app.ui.screens.PurchaseScreen
import com.shahpharmacy.app.ui.screens.ReportsScreen
import com.shahpharmacy.app.ui.screens.SettingsScreen
import com.shahpharmacy.app.ui.screens.StockScreen
import com.shahpharmacy.app.ui.theme.BackgroundLight
import com.shahpharmacy.app.ui.theme.DeepNavy
import com.shahpharmacy.app.ui.theme.NavyPrimary
import com.shahpharmacy.app.ui.theme.ShahPharmacyTheme

class MainActivity : ComponentActivity() {
    private val viewModel: PharmacyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ShahPharmacyTheme {
                PharmacyApp(viewModel = viewModel)
            }
        }
    }
}

fun tabIcon(tab: NavigationTab): ImageVector = when (tab) {
    NavigationTab.DASHBOARD -> Icons.Default.Dashboard
    NavigationTab.PURCHASE -> Icons.Default.Inventory
    NavigationTab.SALE -> Icons.Default.PointOfSale
    NavigationTab.EXPENSE -> Icons.Default.Receipt
    NavigationTab.STOCK -> Icons.Default.MedicalServices
    NavigationTab.REPORTS -> Icons.Default.Analytics
    NavigationTab.CASH -> Icons.Default.AccountBalanceWallet
    NavigationTab.SETTINGS -> Icons.Default.Settings
}

@Composable
fun PharmacyApp(viewModel: PharmacyViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (currentUser == null) {
        LoginScreen(viewModel = viewModel)
    } else {
        // Back handler: if not on dashboard, back key takes back to dashboard
        BackHandler(enabled = currentTab != NavigationTab.DASHBOARD) {
            viewModel.selectTab(NavigationTab.DASHBOARD)
        }

        val availableTabs = remember(currentUser?.role) {
            if (currentUser?.role == "admin") {
                NavigationTab.entries
            } else {
                listOf(
                    NavigationTab.DASHBOARD,
                    NavigationTab.SALE,
                    NavigationTab.EXPENSE,
                    NavigationTab.STOCK,
                    NavigationTab.SETTINGS
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpanded = maxWidth >= 700.dp

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    AppHeader(
                        currentUser = currentUser,
                        onLogout = { viewModel.logout() }
                    )
                },
                bottomBar = {
                    if (!isExpanded) {
                        // Horizontal scrollable tab chips / bar for mobile to fit all tabs nicely
                        Surface(
                            color = DeepNavy,
                            shadowElevation = 8.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                availableTabs.forEach { tab ->
                                    val isSelected = currentTab == tab
                                    Surface(
                                        color = if (isSelected) NavyPrimary else Color.Transparent,
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .clickable { viewModel.selectTab(tab) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = tabIcon(tab),
                                                contentDescription = tab.title,
                                                tint = if (isSelected) Color.White else Color(0xFFDCEAFF),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = tab.title,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else Color(0xFFDCEAFF)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(BackgroundLight)
                ) {
                    if (isExpanded) {
                        NavigationRail(
                            containerColor = DeepNavy,
                            contentColor = Color(0xFFDCEAFF),
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            availableTabs.forEach { tab ->
                                val isSelected = currentTab == tab
                                NavigationRailItem(
                                    selected = isSelected,
                                    onClick = { viewModel.selectTab(tab) },
                                    icon = {
                                        Icon(
                                            imageVector = tabIcon(tab),
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = { Text(tab.title, fontSize = 11.sp) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = Color.White,
                                        indicatorColor = NavyPrimary,
                                        unselectedIconColor = Color(0xFFDCEAFF),
                                        unselectedTextColor = Color(0xFFDCEAFF)
                                    )
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        when (currentTab) {
                            NavigationTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                            NavigationTab.PURCHASE -> PurchaseScreen(viewModel = viewModel)
                            NavigationTab.SALE -> DailySaleScreen(viewModel = viewModel)
                            NavigationTab.EXPENSE -> ExpenseScreen(viewModel = viewModel)
                            NavigationTab.STOCK -> StockScreen(viewModel = viewModel)
                            NavigationTab.REPORTS -> ReportsScreen(viewModel = viewModel)
                            NavigationTab.CASH -> CashScreen(viewModel = viewModel)
                            NavigationTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
