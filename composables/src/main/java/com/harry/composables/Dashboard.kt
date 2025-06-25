package com.harry.composables

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.harry.model.ConversionRecord
import com.harry.viewmodels.DashboardViewModel
import com.harry.viewmodels.DashboardState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Dashboard(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
    appVersion: String = "1.0.0"
) {
    val state: DashboardState by viewModel.state.collectAsState()
    var conversionHistory: List<ConversionRecord> by remember { mutableStateOf(emptyList()) }
    var isDarkMode by remember { mutableStateOf(false) } // Local theme state
    
    // Update system state
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<com.harry.model.UpdateInfo?>(null) }
    var checkInterval by remember { mutableStateOf(30) } // Start at 30 seconds
    var lastUpdateCheck by remember { mutableStateOf(0L) }
    var refreshMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    
    // Smart network and battery checking
    val shouldCheckForUpdates = @androidx.annotation.RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) { forceCheck: Boolean ->
        if (forceCheck) {
            true
        } else {
            try {
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                val isCharging = batteryManager.isCharging
                
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val network = connectivityManager.activeNetwork
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                val isWifi = networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                val isUnmetered = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == true
                
                // Best practices: Check more frequently on Wi-Fi and when charging/high battery
                when {
                    batteryLevel < 15 && !isCharging -> false // Skip if battery very low and not charging
                    batteryLevel < 30 && !isCharging && !isWifi -> false // Skip on cellular when battery low
                    !isUnmetered && checkInterval < 120 -> false // Don't check too frequently on metered connections
                    else -> true
                }
            } catch (e: Exception) {
                // If we can't check network/battery state, default to allowing updates
                true
            }
        }
    }
    
    // Function to check for updates with smart throttling
    val checkForUpdate = { forceCheck: Boolean ->
        val currentTime = System.currentTimeMillis()
        val timeSinceLastCheck = currentTime - lastUpdateCheck
        
        // Force check bypasses all conditions, normal check has throttling and conditions
        if (forceCheck || ((timeSinceLastCheck > 10_000) && shouldCheckForUpdates(forceCheck))) {
            lastUpdateCheck = currentTime
            if (forceCheck) refreshMessage = "Checking for updates..."
            
            viewModel.forceUpdateCheck { update ->
                Log.d("DEBUG", "Update check callback - hasUpdate: ${update.hasUpdate}, error: ${update.error}")
                if (update.updateInfo != null) {
                    Log.d("DEBUG", "Update info - version: ${update.updateInfo!!.latestVersion}, versionCode: ${update.updateInfo!!.latestVersionCode}")
                }
                
                if (update.hasUpdate && update.updateInfo != null) {
                    updateInfo = update.updateInfo
                    showUpdateDialog = true
                    // Reset interval when update found
                    checkInterval = 30
                    if (forceCheck) {
                        refreshMessage = "Update found!"
                        // Clear message after delay
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            delay(1500)
                            refreshMessage = null
                        }
                    }
                } else {
                    // Exponential backoff: 30s → 1m → 2m → 5m → 10m (max)
                    checkInterval = when (checkInterval) {
                        30 -> 60
                        60 -> 120
                        120 -> 300
                        300 -> 600
                        else -> 600 // Max 10 minutes
                    }
                    if (forceCheck) {
                        refreshMessage = if (update.error != null) {
                            "Error: ${update.error}"
                        } else {
                            "You're up to date!"
                        }
                        // Clear message after delay
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            delay(1500)
                            refreshMessage = null
                        }
                    }
                }
            }
        } else if (forceCheck) {
            refreshMessage = "Checked recently"
            // Clear message after delay
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                delay(1500)
                refreshMessage = null
            }
        }
    }
    
    // Simple debug function that bypasses ALL conditions
    val debugUpdateCheck = {
        Log.d("DEBUG", "DEBUG: Force checking for updates, bypassing all conditions")
        refreshMessage = "DEBUG: Checking for updates..."
        viewModel.forceUpdateCheck { update ->
            Log.d("DEBUG", "DEBUG Update check result - hasUpdate: ${update.hasUpdate}, error: ${update.error}")
            if (update.updateInfo != null) {
                Log.d("DEBUG", "DEBUG Update info - version: ${update.updateInfo!!.latestVersion}, versionCode: ${update.updateInfo!!.latestVersionCode}")
            }
            if (update.hasUpdate && update.updateInfo != null) {
                updateInfo = update.updateInfo
                showUpdateDialog = true
                refreshMessage = "DEBUG: Update found!"
            } else {
                refreshMessage = if (update.error != null) {
                    "DEBUG: Error - ${update.error}"
                } else {
                    "DEBUG: You're up to date!"
                }
            }
            // Clear message after delay
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                delay(3000)
                refreshMessage = null
            }
        }
    }
    
    // Lifecycle observer for checking updates when app becomes visible
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkForUpdate(false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Collect conversion history updates
    LaunchedEffect(Unit) {
        viewModel.conversionHistory.collectLatest { history ->
            conversionHistory = history
        }
    }
    
    // Check for updates on app start
    LaunchedEffect(Unit) {
        checkForUpdate(false)
    }
    
    // Frequent background update checks for near-instant detection
    LaunchedEffect(checkInterval) {
        while (true) {
            delay(checkInterval * 1000L)
            checkForUpdate(false)
        }
    }
    
    // Check for updates when user performs conversions (user is actively using the app)
    LaunchedEffect(state.result) {
        if (state.result != "0.00") { // Only check if there's been a conversion
            // Reset interval on user activity - they're actively using the app
            checkInterval = 30
            checkForUpdate(false)
        }
    }
    
    // Reset check interval when user interacts with the app (lifecycle events)
    LaunchedEffect(isDarkMode) {
        // User is interacting with UI (theme changes, etc.)
        checkInterval = 30
    }
    
    // Pull-to-refresh state for manual instant updates
    val pullToRefreshState = rememberPullToRefreshState()
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            delay(300) // Small delay to show the feedback text
            checkForUpdate(true)
            delay(1200) // Show result message
            pullToRefreshState.endRefresh()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
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
                    isDarkMode = isDarkMode,
                    appVersion = appVersion,
                    onVersionLongPress = debugUpdateCheck
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
        
        // Pull-to-refresh indicator
        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullToRefreshState,
        )
        
        // Pull-to-refresh feedback text
        if (pullToRefreshState.isRefreshing || refreshMessage != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) {
                        Color(0xFF1A1B3A).copy(alpha = 0.95f)
                    } else {
                        Color.White.copy(alpha = 0.95f)
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val message = refreshMessage ?: "Checking for updates..."
                    val isChecking = message == "Checking for updates..."
                    
                    if (isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = if (isDarkMode) Color.White else Color(0xFF8B5CF6)
                        )
                    }
                    
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isDarkMode) Color.White else Color(0xFF374151)
                        )
                    )
                }
            }
        }
    }
    
    // Update Dialog
    if (showUpdateDialog && updateInfo != null) {
        UpdateDialog(
            updateInfo = updateInfo!!,
            onDismiss = { showUpdateDialog = false },
            isDarkMode = isDarkMode,
            downloadService = viewModel.downloadService
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HeaderSection(
    modifier: Modifier, 
    isDarkMode: Boolean, 
    appVersion: String,
    onVersionLongPress: () -> Unit = {}
) {
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
            text = appVersion,
            style = MaterialTheme.typography.bodySmall.copy(
                color = textColor.copy(alpha = 0.6f)
            ),
            modifier = Modifier.combinedClickable(
                onClick = { onVersionLongPress() },
                onLongClick = { onVersionLongPress() }
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    MaterialTheme {
        Dashboard(appVersion = "v1.0.0")
    }
}