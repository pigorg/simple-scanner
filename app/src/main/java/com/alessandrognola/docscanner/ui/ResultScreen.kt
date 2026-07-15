package com.alessandrognola.docscanner.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alessandrognola.docscanner.files.FileFormat

@Composable
fun ResultScreen(
    pageUri: Uri,
    onSave: (Uri, FileFormat) -> Unit,
    onShare: (Uri, FileFormat) -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember(pageUri) {
        context.contentResolver.openInputStream(pageUri)?.use { BitmapFactory.decodeStream(it) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Pagina scansionata", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Salva", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onSave(pageUri, FileFormat.PDF) }, modifier = Modifier.fillMaxWidth()) {
            Text("Salva PDF")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onSave(pageUri, FileFormat.JPG) }, modifier = Modifier.fillMaxWidth()) {
            Text("Salva JPG (immagine)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Condividi", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = { onShare(pageUri, FileFormat.PDF) }, modifier = Modifier.fillMaxWidth()) {
            Text("Condividi PDF")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = { onShare(pageUri, FileFormat.JPG) }, modifier = Modifier.fillMaxWidth()) {
            Text("Condividi JPG (immagine)")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onDone) {
                Text("Fatto")
            }
        }
    }
}
