package com.harry.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.harry.viewmodels.DashboardState
import com.harry.viewmodels.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Currency Converter",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Amount Input
            OutlinedTextField(
                value = state.amount,
                onValueChange = { viewModel.updateAmount(it) },
                label = { Text("Amount") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )

            // Currency Selection Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // From Currency
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = state.fromCurrency,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("From") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .padding(end = 8.dp)
                    )
                }

                // Swap Button
                IconButton(
                    onClick = { viewModel.swapCurrencies() },
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap currencies"
                    )
                }

                // To Currency
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = state.toCurrency,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("To") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .padding(start = 8.dp)
                    )
                }
            }

            // Convert Button
            Button(
                onClick = { viewModel.convertCurrency() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Convert",
                        fontSize = 18.sp
                    )
                }
            }

            // Error Message
            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Result Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Result",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = state.result,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    MaterialTheme {
        Dashboard()
    }
}