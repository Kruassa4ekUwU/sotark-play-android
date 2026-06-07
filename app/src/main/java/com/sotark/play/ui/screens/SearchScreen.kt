package com.sotark.play.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sotark.play.R
import com.sotark.play.data.model.AgeRating
import com.sotark.play.ui.components.AppCard
import com.sotark.play.viewmodel.SearchViewModel
import com.sotark.play.viewmodel.SortOption

@Composable
fun SearchScreen(
    onAppClick: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val filter = state.filter

    Column(Modifier.fillMaxSize()) {

        // ── Search bar ────────────────────────────────────────────────────
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value         = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder   = { Text(stringResource(R.string.search_hint)) },
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
                modifier   = Modifier.weight(1f)
            )
            val hasActiveFilters = filter.category != null || filter.minRating > 0f ||
                filter.maxAge != null || filter.sort != SortOption.DOWNLOADS
            BadgedBox(badge = { if (hasActiveFilters) Badge() }) {
                IconButton(
                    onClick = viewModel::toggleFilters,
                    colors  = IconButtonDefaults.iconButtonColors(
                        containerColor = if (filter.showFilters)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(Icons.Filled.FilterList, null,
                        tint = if (filter.showFilters) Color.White
                               else MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // ── Filters panel ─────────────────────────────────────────────────
        AnimatedVisibility(
            visible = filter.showFilters,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            Column(
                Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Сортировка", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SortOption.values().forEach { opt ->
                        FilterChip(
                            selected = filter.sort == opt,
                            onClick  = { viewModel.setSort(opt) },
                            label    = { Text(opt.label) }
                        )
                    }
                }

                Text(
                    "Минимальный рейтинг: ${if (filter.minRating == 0f) "Любой" else "%.1f+".format(filter.minRating)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value         = filter.minRating,
                    onValueChange = viewModel::setMinRating,
                    valueRange    = 0f..5f,
                    steps         = 9
                )

                Text("Макс. возрастной рейтинг",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(selected = filter.maxAge == null,
                        onClick = { viewModel.setMaxAge(null) }, label = { Text("Все") })
                    AgeRating.values().forEach { age ->
                        FilterChip(selected = filter.maxAge == age,
                            onClick = { viewModel.setMaxAge(age) }, label = { Text(age.label) })
                    }
                }

                if (filter.category != null || filter.minRating > 0f ||
                    filter.maxAge != null || filter.sort != SortOption.DOWNLOADS) {
                    TextButton(
                        onClick = viewModel::clearFilters,
                        colors  = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Filled.FilterListOff, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Сбросить фильтры")
                    }
                }
                HorizontalDivider()
            }
        }

        // ── Suggestions ───────────────────────────────────────────────────
        if (state.suggestions.isNotEmpty()) {
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                state.suggestions.forEach { s ->
                    ListItem(
                        headlineContent = { Text(s) },
                        leadingContent  = { Icon(Icons.Filled.Search, null) },
                        modifier        = Modifier.clickable {
                            viewModel.onQueryChange(s); viewModel.search(s)
                        }
                    )
                    HorizontalDivider()
                }
            }
            return
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        if (state.results.isNotEmpty()) {
            Text("Найдено: ${state.results.size}",
                style    = MaterialTheme.typography.labelMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }

        if (state.query.isNotEmpty() && state.results.isEmpty() && !state.isLoading) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.SearchOff, null, modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.nothing_found), fontWeight = FontWeight.Medium)
                    Text("Попробуй изменить фильтры",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return
        }

        LazyColumn(
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.results.isEmpty() && state.query.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 48.dp),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Search, null, modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.enter_app_name),
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            items(state.results) { app ->
                AppCard(app = app, onClick = { onAppClick(app.id) })
            }
        }
    }
}
