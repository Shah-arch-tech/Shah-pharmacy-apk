package com.shahpharmacy.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shahpharmacy.app.model.TimeframeTotals
import com.shahpharmacy.app.ui.PharmacyViewModel
import com.shahpharmacy.app.ui.components.HeroBanner
import com.shahpharmacy.app.ui.components.formatCurrency
import com.shahpharmacy.app.ui.theme.AmberOrange
import com.shahpharmacy.app.ui.theme.BlueAccent
import com.shahpharmacy.app.ui.theme.BorderColor
import com.shahpharmacy.app.ui.theme.CrimsonRed
import com.shahpharmacy.app.ui.theme.EmeraldGreen
import com.shahpharmacy.app.ui.theme.NavyPrimary
import com.shahpharmacy.app.ui.theme.TextMuted

@Composable
fun ReportsScreen(
    viewModel: PharmacyViewModel,
    modifier: Modifier = Modifier
) {
    val weekly by viewModel.weeklyTotals.collectAsStateWithLifecycle()
    val monthly by viewModel.monthlyTotals.collectAsStateWithLifecycle()
    val allTime by viewModel.allTimeTotals.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Weekly", "Monthly", "All-Time")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        HeroBanner(title = "Reports & Analytics")

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = NavyPrimary,
            modifier = Modifier.border(1.dp, BorderColor, RoundedCornerShape(12.dp))
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val currentData: TimeframeTotals = when (selectedTabIndex) {
            0 -> weekly
            1 -> monthly
            else -> allTime
        }
        val currentPeriodName = tabTitles[selectedTabIndex]

        ReportSectionCard(
            title = "$currentPeriodName Financial Overview",
            totals = currentData
        )

        Spacer(modifier = Modifier.height(16.dp))

        // All periods comparison
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Summary Comparison",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                ComparisonRow("Weekly (Last 7 Days)", weekly)
                Spacer(modifier = Modifier.height(10.dp))
                ComparisonRow("Monthly (This Month)", monthly)
                Spacer(modifier = Modifier.height(10.dp))
                ComparisonRow("All-Time Cumulative", allTime)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ReportSectionCard(
    title: String,
    totals: TimeframeTotals
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFFF7FAFF),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total Sales", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatCurrency(totals.sale), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = BlueAccent)
                    }
                }

                Surface(
                    color = Color(0xFFF7FAFF),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Purchases", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatCurrency(totals.purchase), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AmberOrange)
                    }
                }

                Surface(
                    color = Color(0xFFF7FAFF),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Expenses", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatCurrency(totals.expense), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CrimsonRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = Color(0xFFF0FDF4),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Estimated Net Profit",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF166534)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatCurrency(totals.estimatedProfit),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (totals.estimatedProfit >= 0) EmeraldGreen else CrimsonRed
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Calculated as Sales minus estimated Cost of Goods Sold (derived from inventory markup ratio) minus Expenses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF166534)
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonRow(
    period: String,
    totals: TimeframeTotals
) {
    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(period, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sales: ${formatCurrency(totals.sale)}", style = MaterialTheme.typography.bodySmall, color = BlueAccent)
                Text("Expenses: ${formatCurrency(totals.expense)}", style = MaterialTheme.typography.bodySmall, color = CrimsonRed)
                Text(
                    "Est Profit: ${formatCurrency(totals.estimatedProfit)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (totals.estimatedProfit >= 0) EmeraldGreen else CrimsonRed
                )
            }
        }
    }
}
