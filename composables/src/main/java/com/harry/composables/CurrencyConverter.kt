package com.harry.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.harry.composables.components.CurrencyDropdown
import com.harry.viewmodels.DashboardViewModel
import com.harry.viewmodels.DashboardState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverter(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Simple static list; ideally fetch from repo or constants
    val currencies = listOf("USD", "EUR", "GBP", "JPY", "AUD", "CAD")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount Input
            OutlinedTextField(
                value = state.amount,
                onValueChange = { viewModel.updateAmount(it) },
                label = { Text("Amount") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Currency Selection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // From Currency
                CurrencyDropdown(
                    label = "From",
                    value = state.fromCurrency,
                    options = currencies,
                    onCurrencySelected = { viewModel.updateFromCurrency(it) },
                    modifier = Modifier.weight(1f)
                )

                // Swap Button with rotation animation
                var rotated by remember { mutableStateOf(false) }

                IconButton(onClick = {
                    rotated = !rotated
                    viewModel.swapCurrencies()
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Swap currencies",
                        modifier = Modifier.rotate(if (rotated) 180f else 0f)
                    )
                }

                // To Currency
                CurrencyDropdown(
                    label = "To",
                    value = state.toCurrency,
                    options = currencies,
                    onCurrencySelected = { viewModel.updateToCurrency(it) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Convert Button
            Button(
                onClick = { viewModel.convertCurrency() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Convert")
                }
            }

            // Result
            if (state.result.isNotEmpty()) {
                Text(
                    text = state.result,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
} 