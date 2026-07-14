package com.alessandrognola.docscanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alessandrognola.docscanner.billing.BillingRepository
import com.alessandrognola.docscanner.billing.PRODUCT_MONTH
import com.alessandrognola.docscanner.billing.PRODUCT_YEAR
import com.alessandrognola.docscanner.files.SaveShareHelper
import com.alessandrognola.docscanner.quota.QuotaState
import com.alessandrognola.docscanner.quota.ScanQuotaRepository
import com.alessandrognola.docscanner.scan.rememberDocumentScannerLauncher
import com.alessandrognola.docscanner.ui.HomeScreen
import com.alessandrognola.docscanner.ui.PaywallDialog
import com.alessandrognola.docscanner.ui.ResultScreen
import com.alessandrognola.docscanner.ui.theme.DocScannerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DAY_MILLIS = 24L * 60 * 60 * 1000

private sealed class Screen {
    data object Home : Screen()
    data class Result(val pageUri: Uri) : Screen()
}

class MainActivity : ComponentActivity() {

    private lateinit var quotaRepository: ScanQuotaRepository
    private lateinit var saveShareHelper: SaveShareHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        quotaRepository = ScanQuotaRepository(applicationContext)
        saveShareHelper = SaveShareHelper(applicationContext)

        setContent {
            val scope = rememberCoroutineScope()
            val billingRepository = remember {
                BillingRepository(
                    context = applicationContext,
                    scope = scope,
                    onPurchaseCompleted = { productId ->
                        val days = if (productId == PRODUCT_YEAR) 365L else 30L
                        quotaRepository.extendPremium(days * DAY_MILLIS)
                    }
                )
            }
            DisposableEffect(Unit) {
                billingRepository.startConnection()
                onDispose { billingRepository.endConnection() }
            }

            DocScannerTheme {
                AppContent(
                    quotaRepository = quotaRepository,
                    billingRepository = billingRepository,
                    saveShareHelper = saveShareHelper
                )
            }
        }
    }
}

@Composable
private fun AppContent(
    quotaRepository: ScanQuotaRepository,
    billingRepository: BillingRepository,
    saveShareHelper: SaveShareHelper
) {
    val activity = LocalContext.current as ComponentActivity
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    var showPaywall by remember { mutableStateOf(false) }
    val quotaState by quotaRepository.state.collectAsState(
        initial = QuotaState(0, ScanQuotaRepository.FREE_SCANS_PER_MONTH, false, 0L)
    )

    val launchScanner = rememberDocumentScannerLauncher { result ->
        val uri = result.pages?.firstOrNull()?.imageUri
        if (uri != null) {
            scope.launch { quotaRepository.recordScan() }
            screen = Screen.Result(uri)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(modifier = Modifier.padding(16.dp)) {
                    Text(text = data.visuals.message, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val current = screen) {
                is Screen.Home -> HomeScreen(
                    quotaState = quotaState,
                    onScanClick = {
                        scope.launch {
                            if (quotaRepository.canScan()) {
                                launchScanner()
                            } else {
                                showPaywall = true
                            }
                        }
                    },
                    onUpgradeClick = { showPaywall = true }
                )
                is Screen.Result -> ResultScreen(
                    pageUri = current.pageUri,
                    onSave = { uri ->
                        scope.launch {
                            val saved = withContext(Dispatchers.IO) { saveShareHelper.saveToGallery(uri) }
                            snackbarHostState.showSnackbar(
                                message = if (saved != null) "✓ Salvato nella galleria" else "Salvataggio non riuscito",
                                duration = SnackbarDuration.Long
                            )
                        }
                    },
                    onShare = { uri ->
                        val shareIntent = saveShareHelper.shareImage(uri)
                        activity.startActivity(Intent.createChooser(shareIntent, "Condividi tramite"))
                    },
                    onDone = { screen = Screen.Home }
                )
            }
        }
    }

    if (showPaywall) {
        PaywallDialog(
            onDismiss = { showPaywall = false },
            onBuyMonth = {
                billingRepository.launchPurchaseFlow(activity, PRODUCT_MONTH)
                showPaywall = false
            },
            onBuyYear = {
                billingRepository.launchPurchaseFlow(activity, PRODUCT_YEAR)
                showPaywall = false
            }
        )
    }
}
