package com.sotark.play.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sotark.play.ui.components.AppCard
import com.sotark.play.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    onAppClick: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(Modifier.fillMaxSize()) {

        // ── Search field ──────────────────────────────────────────────────
        OutlinedTextField(
            value         = state.query,
            onValueChange = viewModel::onQueryChange,
            placeholder   = { Text("Поиск приложений…") },
            leadingIcon   = { Icon(Icons.Filled.Search, null) },
            trailingIcon  = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Filled.Close, null)
                    }
                }
            },
            singleLine = true,
            shape      = RoundedCornerShape(12.dp),
            modifier   = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )

        // ── Suggestions dropdown ──────────────────────────────────────────
        if (state.suggestions.isNotEmpty()) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                state.suggestions.forEach { s ->
                    ListItem(
                        headlineContent = { Text(s) },
                        leadingContent  = { Icon(Icons.Filled.Search, null) },
                        modifier        = Modifier.clickable {
                            viewModel.onQueryChange(s)
                            viewModel.search(s)
                        }
                    )
                    HorizontalDivider()
                }
            }
            return
        }

        // ── Loading ───────────────────────────────────────────────────────
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        // ── Empty result ──────────────────────────────────────────────────
        if (state.query.isNotEmpty() && state.results.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Ничего не найдено по «\${state.query}»",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return
        }

        // ── Results ───────────────────────────────────────────────────────
        LazyColumn(
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.results.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Введите название приложения",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            items(state.results) { app ->
                AppCard(app = app, onClick = { onAppClick(app.id) })
            }
        }
    }
}
