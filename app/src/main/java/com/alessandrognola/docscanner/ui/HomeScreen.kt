package com.alessandrognola.docscanner.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alessandrognola.docscanner.R
import com.alessandrognola.docscanner.quota.QuotaState
import com.alessandrognola.docscanner.quota.ScanQuotaRepository
import java.text.SimpleDateFormat
import java.time.Year
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

private const val DAY_MILLIS = 24L * 60 * 60 * 1000

@Composable
fun HomeScreen(
    quotaState: QuotaState,
    onScanClick: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Simple Scanner",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (quotaState.isPremium) {
                PremiumStatusCard(premiumUntilMillis = quotaState.premiumUntilMillis)
            } else {
                Text(
                    text = "${quotaState.scansUsed}/${ScanQuotaRepository.FREE_SCANS_PER_MONTH} scansioni gratuite usate questo mese",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = quotaState.scansUsed / ScanQuotaRepository.FREE_SCANS_PER_MONTH.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onScanClick) {
                Text("Scansiona un documento")
            }

            if (!quotaState.isPremium) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onUpgradeClick) {
                    Text("Sblocca senza limiti")
                }
            }
        }

        Text(
            text = "IstoreLab © ${Year.now().value}",
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun PremiumStatusCard(premiumUntilMillis: Long) {
    val expiryText = remember(premiumUntilMillis) {
        SimpleDateFormat("dd/MM/yyyy", Locale.ITALY).format(Date(premiumUntilMillis))
    }
    val daysRemaining = remember(premiumUntilMillis) {
        ceil((premiumUntilMillis - System.currentTimeMillis()) / DAY_MILLIS.toDouble())
            .toLong()
            .coerceAtLeast(0)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✓ Piano Premium attivo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Scade il $expiryText",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (daysRemaining > 0) "$daysRemaining giorni rimanenti" else "In scadenza oggi",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
