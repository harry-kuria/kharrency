package com.harry.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
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
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Amount Input Section
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Amount",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color(0xFF374151)
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = state.amount,
                onValueChange = { viewModel.updateAmount(it) },
                placeholder = { 
                    Text(
                        "Enter amount",
                        color = Color(0xFF9CA3AF)
                    ) 
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color(0xFFF9FAFB),
                    unfocusedContainerColor = Color(0xFFF9FAFB)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Currency Selection Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // From Currency
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "From",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color(0xFF374151)
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                CurrencySelector(
                    selectedCurrency = state.fromCurrency,
                    onCurrencySelected = { viewModel.updateFromCurrency(it) },
                    label = ""
                )
            }

            // Swap Button
            Box(
                modifier = Modifier.padding(top = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                FilledIconButton(
                    onClick = { viewModel.swapCurrencies() },
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFF8B5CF6),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Swap currencies",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // To Currency
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "To",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color(0xFF374151)
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                CurrencySelector(
                    selectedCurrency = state.toCurrency,
                    onCurrencySelected = { viewModel.updateToCurrency(it) },
                    label = ""
                )
            }
        }

        // Convert Button
        Button(
            onClick = { viewModel.convertCurrency() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !state.isLoading && state.amount.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8B5CF6),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFE5E7EB),
                disabledContentColor = Color(0xFF9CA3AF)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (state.isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Text(
                        "Converting...",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                Text(
                    "Convert Currency",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Result Display
        if (state.result != "0.00" && !state.isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEDE9FE)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Converted Amount",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF6B46C1)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${state.result} ${state.toCurrency}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color(0xFF4C1D95)
                        )
                    )
                }
            }
        }
    }
} 