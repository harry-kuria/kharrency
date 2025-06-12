package com.harry.composables

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.harry.model.ConversionRecord
import com.harry.viewmodels.DashboardState
import com.harry.viewmodels.DashboardViewModel
import kotlinx.coroutines.flow.collectLatest

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state: DashboardState by viewModel.state.collectAsState()
    var conversionHistory: List<ConversionRecord> by remember { mutableStateOf(emptyList()) }

    // Collect conversion history updates
    LaunchedEffect(Unit) {
        viewModel.conversionHistory.collectLatest { history ->
            conversionHistory = history
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Currency Converter Section
        CurrencyConverter(viewModel = viewModel)

        // Conversion History Section
        ConversionHistory(
            conversions = conversionHistory,
            modifier = Modifier.weight(1f)
        )

        // Network Error Section (shown when there's an error)
        state.error?.let { error ->
            NetworkError(
                message = error,
                onRetry = { viewModel.convertCurrency() }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    MaterialTheme {
        Dashboard()
    }
}