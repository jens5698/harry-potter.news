package de.hpnews.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.hpnews.app.ArticleState
import de.hpnews.core.VocabEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(state: ArticleState, onBack: () -> Unit, onRetry: () -> Unit) {
    val item = state.item
    val uri = LocalUriHandler.current
    // Tab 0 = einfaches Englisch, Tab 1 = Original
    var tab by remember(item.id) { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.publisher, maxLines = 1) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            // Quellenangabe
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(12.dp).fillMaxWidth()) {
                    Text("Quelle: ${item.publisher}", fontWeight = FontWeight.SemiBold)
                    Text(formatDate(item.published), style = MaterialTheme.typography.bodySmall)
                    Text(item.link, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    if (item.foundVia != item.publisher) {
                        Text("Gefunden über: ${item.foundVia}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { uri.openUri(item.link) }) { Text("Original-Artikel öffnen") }
                }
            }
            Spacer(Modifier.height(12.dp))

            if (state.needsTranslation) {
                TabRow(selectedTabIndex = tab) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Einfaches Englisch") })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Original") })
                }
                Spacer(Modifier.height(12.dp))
            }

            if (!state.needsTranslation || tab == 1) {
                Text(item.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OriginalBody(state)
            } else {
                SimplifiedBody(state, onRetry)
            }
        }
    }
}

@Composable
private fun Loading(text: String) {
    Row(Modifier.padding(vertical = 24.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(Modifier.width(24.dp).height(24.dp), strokeWidth = 2.dp)
        Spacer(Modifier.width(12.dp))
        Text(text)
    }
}

@Composable
private fun OriginalBody(state: ArticleState) {
    val text = state.original
    if (state.loadingText || text == null) { Loading("Artikel wird geladen …"); return }
    text.paragraphs.forEach { p ->
        Text(p, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 10.dp))
    }
    if (!text.isComplete) {
        Text(
            "Nur ein Auszug verfügbar (z. B. Bezahlschranke). Den ganzen Text gibt es über „Original-Artikel öffnen“.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SimplifiedBody(state: ArticleState, onRetry: () -> Unit) {
    val s = state.simplified
    when {
        state.loadingText -> Loading("Artikel wird geladen …")
        state.simplifying -> Loading("Wird in einfaches Englisch übersetzt …")
        state.simplifyError != null && s == null -> {
            Text(state.simplifyError, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onRetry) { Text("Nochmal versuchen") }
        }
        s != null -> {
            Text(s.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            s.paragraphs.forEach { p ->
                Text(highlight(p, s.vocabulary), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 10.dp))
            }
            if (state.original?.isComplete == false) {
                Text(
                    "Hinweis: Es war nur ein Auszug des Artikels verfügbar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (s.vocabulary.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                VocabList(s.vocabulary)
            }
        }
        else -> Box {}
    }
}

@Composable
private fun VocabList(vocab: List<VocabEntry>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(14.dp).fillMaxWidth()) {
            Text("Vokabeln (Englisch – Deutsch)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            vocab.forEachIndexed { i, v ->
                if (i > 0) HorizontalDivider(Modifier.padding(vertical = 6.dp))
                Row {
                    Text(v.english, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(12.dp))
                    Text(v.german, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/** Hebt Vokabeln aus der Liste im Text fett und golden hervor. */
@Composable
private fun highlight(text: String, vocab: List<VocabEntry>): AnnotatedString {
    val color = MaterialTheme.colorScheme.primary
    val ranges = vocab.mapNotNull { v ->
        val word = v.english.trim()
        if (word.length < 2) null else Regex("\\b" + Regex.escape(word) + "\\b", RegexOption.IGNORE_CASE).find(text)?.range
    }.sortedBy { it.first }
    return buildAnnotatedString {
        var pos = 0
        for (r in ranges) {
            if (r.first < pos) continue
            append(text.substring(pos, r.first))
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = color))
            append(text.substring(r.first, r.last + 1))
            pop()
            pos = r.last + 1
        }
        append(text.substring(pos))
    }
}
