package com.harry.composables

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.harry.model.ConversionRecord
import com.harry.viewmodels.DashboardViewModel
import com.harry.viewmodels.DashboardState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Dashboard(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state: DashboardState by viewModel.state.collectAsState()
    var conversionHistory: List<ConversionRecord> by remember { mutableStateOf(emptyList()) }
    var isDarkMode by remember { mutableStateOf(false) } // Local theme state
    
    // Update system state
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<com.harry.model.UpdateInfo?>(null) }
    
    // Collect conversion history updates
    LaunchedEffect(Unit) {
        viewModel.conversionHistory.collectLatest { history ->
            conversionHistory = history
        }
    }
    
    // Check for updates on app start
    LaunchedEffect(Unit) {
        viewModel.checkForUpdates { update ->
            if (update.hasUpdate && update.updateInfo != null) {
                updateInfo = update.updateInfo
                showUpdateDialog = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDarkMode) {
                        listOf(
                            Color(0xFF1A1B3A),
                            Color(0xFF2D1B69),
                            Color(0xFF1F2937)
                        )
                    } else {
                        listOf(
                            Color(0xFF8B5CF6),
                            Color(0xFF3B82F6),
                            Color(0xFFF3F4F6)
                        )
                    }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Header Section with Theme Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                HeaderSection(
                    modifier = Modifier.weight(1f),
                    isDarkMode = isDarkMode
                )
                
                // Theme Toggle Button
                ThemeToggleButton(
                    isDarkMode = isDarkMode,
                    onToggle = { isDarkMode = !isDarkMode }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Quick Stats Card
            QuickStatsCard(state = state, isDarkMode = isDarkMode)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Currency Converter Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) {
                        Color(0xFF1A1B3A).copy(alpha = 0.8f)
                    } else {
                        Color.White.copy(alpha = 0.9f)
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Convert Currency",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = if (isDarkMode) Color.White else Color(0xFF374151)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CurrencyConverter(viewModel = viewModel, isDarkMode = isDarkMode)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Conversion History Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) {
                        Color(0xFF1A1B3A).copy(alpha = 0.8f)
                    } else {
                        Color.White.copy(alpha = 0.9f)
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                ConversionHistory(
                    conversions = conversionHistory,
                    isDarkMode = isDarkMode,
                    modifier = Modifier.padding(24.dp)
                )
            }

            // Network Error Section
            state.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                NetworkError(
                    message = error,
                    onRetry = { viewModel.convertCurrency() },
                    isDarkMode = isDarkMode
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
    
    // Update Dialog
    if (showUpdateDialog && updateInfo != null) {
        UpdateDialog(
            updateInfo = updateInfo!!,
            onDismiss = { showUpdateDialog = false },
            onDownload = { showUpdateDialog = false },
            isDarkMode = isDarkMode
        )
    }
}

@Composable
private fun HeaderSection(modifier: Modifier, isDarkMode: Boolean) {
    val textColor = if (isDarkMode) Color.White else Color.White
    Column(modifier = modifier) {
        Text(
            text = "Welcome to",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = textColor.copy(alpha = 0.8f)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Kharrency",
            style = MaterialTheme.typography.headlineLarge.copy(
                color = textColor
            )
        )
        Text(
            text = "Currency Exchange Made Simple",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = textColor.copy(alpha = 0.7f)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "v1.0.0",
            style = MaterialTheme.typography.bodySmall.copy(
                color = textColor.copy(alpha = 0.6f)
            )
        )
    }
}

@Composable
private fun QuickStatsCard(state: DashboardState, isDarkMode: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) {
                Color(0xFF1A1B3A).copy(alpha = 0.6f)
            } else {
                Color.White.copy(alpha = 0.8f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Exchange Rate",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color(0xFF374151).copy(alpha = 0.8f)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${state.fromCurrency}/${state.toCurrency}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = if (isDarkMode) Color.White else Color(0xFF374151)
                    )
                )
            }
            
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = if (isDarkMode) Color.White else Color(0xFF8B5CF6),
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Result",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color(0xFF374151).copy(alpha = 0.8f)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.result,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = if (isDarkMode) Color.White else Color(0xFF374151)
                        )
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