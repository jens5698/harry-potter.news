package de.hpnews.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import de.hpnews.core.Simplifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(initialKey: String, initialModel: String, onSave: (String, String) -> Unit, onBack: () -> Unit) {
    var key by remember { mutableStateOf(initialKey) }
    var model by remember { mutableStateOf(initialModel) }
    val models = listOf(
        Simplifier.DEFAULT_MODEL to "Claude Opus 5.5 – beste Qualität (ca. 5 Cent pro Artikel)",
        Simplifier.CHEAP_MODEL to "Claude Haiku 5.5 – sehr günstig (ca. 0,2 Cent pro Artikel)",
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück") } },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("Claude API-Schlüssel", style = MaterialTheme.typography.titleMedium)
            Text(
                "Wird nur für die Übersetzung englischer Artikel gebraucht und bleibt auf diesem Gerät. " +
                    "Einen Schlüssel gibt es unter console.anthropic.com.",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = key,
                onValueChange = { key = it },
                label = { Text("sk-ant-…") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
            Text("KI-Modell", style = MaterialTheme.typography.titleMedium)
            models.forEach { (id, label) ->
                Row(
                    Modifier.fillMaxWidth().selectable(selected = model == id, onClick = { model = id }).padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = model == id, onClick = { model = id })
                    Text(label)
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = { onSave(key, model) }, modifier = Modifier.fillMaxWidth()) { Text("Speichern") }
        }
    }
}
