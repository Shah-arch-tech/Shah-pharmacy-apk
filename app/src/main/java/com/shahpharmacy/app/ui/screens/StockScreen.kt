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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shahpharmacy.app.ui.PharmacyViewModel
import com.shahpharmacy.app.ui.components.HeroBanner
import com.shahpharmacy.app.ui.components.StatCard
import com.shahpharmacy.app.ui.components.formatCurrency
import com.shahpharmacy.app.ui.theme.AmberOrange
import com.shahpharmacy.app.ui.theme.BlueAccent
import com.shahpharmacy.app.ui.theme.BorderColor
import com.shahpharmacy.app.ui.theme.EmeraldGreen
import com.shahpharmacy.app.ui.theme.NavyPrimary
import com.shahpharmacy.app.ui.theme.TextDark
import com.shahpharmacy.app.ui.theme.TextMuted
import java.util.Locale

@Composable
fun StockScreen(
    viewModel: PharmacyViewModel,
    modifier: Modifier = Modifier
) {
    val stockList by viewModel.stockList.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    val filteredStock = stockList.filter {
        val q = searchQuery.trim().lowercase(Locale.ROOT)
        q.isEmpty() || it.name.lowercase(Locale.ROOT).contains(q) || it.company.lowercase(Locale.ROOT).contains(q)
    }

    val totalPurchaseValue = stockList.sumOf { it.purchaseValue }
    val totalSaleValue = stockList.sumOf { it.saleValue }
    val grossMargin = totalSaleValue - totalPurchaseValue

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        HeroBanner(title = "Current Stock")

        // 4 KPI Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                label = "Purchase Value",
                value = formatCurrency(totalPurchaseValue),
                valueColor = AmberOrange,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Sale Value",
                value = formatCurrency(totalSaleValue),
                valueColor = BlueAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                label = "Potential Margin",
                value = formatCurrency(grossMargin),
                valueColor = EmeraldGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Medicine Lines",
                value = stockList.size.toString(),
                valueColor = TextDark,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stock List
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Stock by Medicine (${filteredStock.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search medicine name or company...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_medicine_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredStock.isEmpty()) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No medicine matches your search." else "No stock yet. Add purchases first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    filteredStock.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${if (item.company.isNotBlank()) "${item.company} • " else ""}Qty: ${item.qty}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                                Text(
                                    text = "Buy: ${formatCurrency(item.purchase)} • Sell: ${formatCurrency(item.sale)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Buy Val: ${formatCurrency(item.purchaseValue)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberOrange
                                )
                                Text(
                                    text = "Sale Val: ${formatCurrency(item.saleValue)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }
                        }
                        if (index < filteredStock.lastIndex) {
                            HorizontalDivider(color = BorderColor)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
