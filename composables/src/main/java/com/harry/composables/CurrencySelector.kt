package com.harry.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelector(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val currencies = listOf("USD", "EUR", "JPY")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCurrency,
            onValueChange = {},
            readOnly = true,
            label = if (label.isNotEmpty()) { { Text(label) } } else null,
            trailingIcon = { 
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                ) 
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF8B5CF6),
                unfocusedBorderColor = if (isDarkMode) Color(0xFF4B5563) else Color(0xFFE5E7EB),
                focusedContainerColor = if (isDarkMode) Color(0xFF1F2937) else Color(0xFFF9FAFB),
                unfocusedContainerColor = if (isDarkMode) Color(0xFF1F2937) else Color(0xFFF9FAFB),
                focusedLabelColor = Color(0xFF8B5CF6),
                unfocusedLabelColor = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                focusedTextColor = if (isDarkMode) Color.White else Color(0xFF374151),
                unfocusedTextColor = if (isDarkMode) Color.White else Color(0xFF374151)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .height(56.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .wrapContentWidth()
        ) {
            currencies.forEach { currency ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = currency,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = if (currency == selectedCurrency) {
                                    Color(0xFF8B5CF6)
                                } else {
                                    if (isDarkMode) Color.White else Color(0xFF374151)
                                }
                            ),
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth()
                        ) 
                    },
                    onClick = {
                        onCurrencySelected(currency)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = if (isDarkMode) Color.White else Color(0xFF374151)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                )
            }
        }
    }
} 