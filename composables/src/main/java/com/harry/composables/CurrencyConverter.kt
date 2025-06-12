package com.harry.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.harry.viewmodels.DashboardViewModel
import com.harry.viewmodels.DashboardState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverter(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // From Currency
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { },
            ) {
                OutlinedTextField(
                    value = state.fromCurrency,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("From") },
                    modifier = Modifier.menuAnchor()
                )
            }

            // Swap Button
            IconButton(onClick = { viewModel.swapCurrencies() }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                    contentDescription = "Swap currencies"
                )
            }

            // To Currency
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { },
            ) {
                OutlinedTextField(
                    value = state.toCurrency,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("To") },
                    modifier = Modifier.menuAnchor()
                )
            }
        }

        // Convert Button
        Button(
            onClick = { viewModel.convertCurrency() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Convert")
            }
        }

        // Result
        if (state.result.isNotEmpty()) {
            Text(
                text = state.result,
                style = MaterialTheme.typography.headlineMedium
            )
        }

        // Error Message
        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 