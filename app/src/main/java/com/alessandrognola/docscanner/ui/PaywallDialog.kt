package com.alessandrognola.docscanner.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PaywallDialog(
    onDismiss: () -> Unit,
    onBuyMonth: () -> Unit,
    onBuyYear: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sblocca scansioni illimitate") },
        text = {
            Column {
                Text("Hai raggiunto il limite gratuito di 5 scansioni al mese. Scegli un piano per continuare senza limiti:")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBuyMonth, modifier = Modifier.fillMaxWidth()) {
                    Text("1 mese — 1€")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBuyYear, modifier = Modifier.fillMaxWidth()) {
                    Text("1 anno — 10€ (risparmi 2€)")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )
}
