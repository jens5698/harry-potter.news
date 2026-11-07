package de.hpnews.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.hpnews.app.ui.ArticleScreen
import de.hpnews.app.ui.HPTheme
import de.hpnews.app.ui.NewsListScreen
import de.hpnews.app.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    private val vm: NewsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HPTheme {
                val screen by vm.screen.collectAsStateWithLifecycle()
                val list by vm.list.collectAsStateWithLifecycle()
                BackHandler(enabled = screen != Screen.List) { vm.back() }
                when (val s = screen) {
                    Screen.List -> NewsListScreen(
                        state = list,
                        onRefresh = vm::refresh,
                        onFilter = vm::setFilter,
                        onOpen = vm::open,
                        onSettings = vm::openSettings,
                    )
                    Screen.Settings -> SettingsScreen(
                        initialKey = vm.settings.apiKey,
                        initialModel = vm.settings.model,
                        onSave = vm::saveSettings,
                        onBack = vm::back,
                    )
                    is Screen.Article -> ArticleScreen(
                        state = s.state,
                        onBack = vm::back,
                        onRetry = vm::retrySimplify,
                    )
                }
            }
        }
    }
}
