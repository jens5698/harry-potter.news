package de.hpnews.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.hpnews.core.ArticleExtractor
import de.hpnews.core.ArticleText
import de.hpnews.core.Lang
import de.hpnews.core.NewsItem
import de.hpnews.core.NewsRepository
import de.hpnews.core.SimplifiedArticle
import de.hpnews.core.SimplifiedCache
import de.hpnews.core.Simplifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class Filter(val label: String) { ALL("Alle"), HBO("HBO-Serie"), GERMAN("Deutsch"), ENGLISH("Englisch"), EMMA("Emma Watson") }

data class ListState(
    val loading: Boolean = false,
    val items: List<NewsItem> = emptyList(),
    val failures: Map<String, String> = emptyMap(),
    val error: String? = null,
    val filter: Filter = Filter.ALL,
) {
    val visible: List<NewsItem>
        get() {
            val hp = items.filter { it.isHarryPotter }
            return when (filter) {
                Filter.ALL -> hp
                Filter.HBO -> hp.filter { it.isHboSeries }
                Filter.GERMAN -> hp.filter { it.language == Lang.DE }
                Filter.ENGLISH -> hp.filter { it.language != Lang.DE }
                Filter.EMMA -> items.filter { it.isEmmaWatson }
            }
        }
}

data class ArticleState(
    val item: NewsItem,
    val loadingText: Boolean = true,
    val original: ArticleText? = null,
    val simplifying: Boolean = false,
    val simplified: SimplifiedArticle? = null,
    val simplifyError: String? = null,
) {
    val needsTranslation get() = item.language != Lang.DE
}

sealed interface Screen {
    data object List : Screen
    data object Settings : Screen
    data class Article(val state: ArticleState) : Screen
}

class NewsViewModel(app: Application) : AndroidViewModel(app) {
    val settings = Settings(app)
    private val repository = NewsRepository()
    private val extractor = ArticleExtractor()
    private val cache = SimplifiedCache(File(app.filesDir, "simplified"))

    private val _list = MutableStateFlow(ListState())
    val list: StateFlow<ListState> = _list

    private val _screen = MutableStateFlow<Screen>(Screen.List)
    val screen: StateFlow<Screen> = _screen

    private var articleJob: Job? = null

    init { refresh() }

    fun refresh() {
        if (_list.value.loading) return
        _list.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { runCatching { repository.loadAll() } }
            result.onSuccess { r ->
                _list.update {
                    it.copy(
                        loading = false,
                        items = r.items,
                        failures = r.failures,
                        error = if (r.items.isEmpty()) "Keine Nachrichten gefunden. Internetverbindung prüfen?" else null,
                    )
                }
            }.onFailure { e ->
                _list.update { it.copy(loading = false, error = e.message ?: e.toString()) }
            }
        }
    }

    fun setFilter(f: Filter) = _list.update { it.copy(filter = f) }

    fun openSettings() { _screen.value = Screen.Settings }

    fun back() {
        articleJob?.cancel()
        _screen.value = Screen.List
    }

    fun saveSettings(apiKey: String, model: String) {
        settings.apiKey = apiKey
        settings.model = model
        _screen.value = Screen.List
    }

    private fun updateArticle(id: String, f: (ArticleState) -> ArticleState) {
        _screen.update { s -> if (s is Screen.Article && s.state.item.id == id) Screen.Article(f(s.state)) else s }
    }

    fun open(item: NewsItem) {
        articleJob?.cancel()
        _screen.value = Screen.Article(ArticleState(item))
        articleJob = viewModelScope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching { extractor.load(item) }.getOrElse { ArticleText(listOf(item.teaser), isComplete = false) }
            }
            updateArticle(item.id) { it.copy(loadingText = false, original = text) }
            if (item.language != Lang.DE) simplify(item, text)
        }
    }

    fun retrySimplify() {
        val s = (_screen.value as? Screen.Article)?.state ?: return
        val text = s.original ?: return
        articleJob = viewModelScope.launch { simplify(s.item, text) }
    }

    private suspend fun simplify(item: NewsItem, text: ArticleText) {
        val model = settings.model
        cache.get(item.id, model)?.let { cached ->
            updateArticle(item.id) { it.copy(simplified = cached, simplifyError = null) }
            return
        }
        val key = settings.apiKey
        if (key.isBlank()) {
            updateArticle(item.id) {
                it.copy(simplifyError = "Für die Übersetzung fehlt noch der API-Schlüssel (oben rechts unter Einstellungen).")
            }
            return
        }
        updateArticle(item.id) { it.copy(simplifying = true, simplifyError = null) }
        val result = withContext(Dispatchers.IO) {
            runCatching {
                Simplifier(key, model).simplify(item.title, text.paragraphs, item.publisher)
                    .also { cache.put(item.id, model, it) }
            }
        }
        updateArticle(item.id) {
            it.copy(
                simplifying = false,
                simplified = result.getOrNull(),
                simplifyError = result.exceptionOrNull()?.let { e -> "Übersetzung fehlgeschlagen: ${e.message ?: e}" },
            )
        }
    }
}
