package com.alessandrognola.docscanner.quota

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.YearMonth

private val Context.scanQuotaDataStore by preferencesDataStore(name = "scan_quota")

data class QuotaState(
    val scansUsed: Int,
    val scansRemaining: Int,
    val isPremium: Boolean,
    val premiumUntilMillis: Long
)

class ScanQuotaRepository(private val context: Context) {

    private object Keys {
        val scanCount = intPreferencesKey("scan_count")
        val scanMonth = stringPreferencesKey("scan_month")
        val premiumUntil = longPreferencesKey("premium_until")
    }

    companion object {
        const val FREE_SCANS_PER_MONTH = 5
    }

    val state: Flow<QuotaState> = context.scanQuotaDataStore.data.map { prefs ->
        val currentMonth = YearMonth.now().toString()
        val storedMonth = prefs[Keys.scanMonth]
        val count = if (storedMonth == currentMonth) prefs[Keys.scanCount] ?: 0 else 0
        val premiumUntil = prefs[Keys.premiumUntil] ?: 0L
        QuotaState(
            scansUsed = count,
            scansRemaining = (FREE_SCANS_PER_MONTH - count).coerceAtLeast(0),
            isPremium = premiumUntil > System.currentTimeMillis(),
            premiumUntilMillis = premiumUntil
        )
    }

    suspend fun canScan(): Boolean {
        val current = state.first()
        return current.isPremium || current.scansRemaining > 0
    }

    suspend fun recordScan() {
        val currentMonth = YearMonth.now().toString()
        context.scanQuotaDataStore.edit { prefs ->
            val storedMonth = prefs[Keys.scanMonth]
            val count = if (storedMonth == currentMonth) prefs[Keys.scanCount] ?: 0 else 0
            prefs[Keys.scanMonth] = currentMonth
            prefs[Keys.scanCount] = count + 1
        }
    }

    /** Estende il periodo premium: se ne resta già attivo, somma alla scadenza esistente. */
    suspend fun extendPremium(durationMillis: Long) {
        context.scanQuotaDataStore.edit { prefs ->
            val now = System.currentTimeMillis()
            val current = prefs[Keys.premiumUntil] ?: 0L
            val base = if (current > now) current else now
            prefs[Keys.premiumUntil] = base + durationMillis
        }
    }
}
