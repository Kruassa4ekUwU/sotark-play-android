package com.sotark.play.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sotark.play.R
import com.sotark.play.ui.components.*
import com.sotark.play.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAppClick: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sotark Play") }) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh    = viewModel::load,
            modifier     = Modifier.padding(padding).fillMaxSize()
        ) {
            when {
                state.isLoading && state.topApps.isEmpty() && state.newApps.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                state.error != null -> Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(R.string.connection_error),
                        style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = viewModel::load) { Text(stringResource(R.string.retry)) }
                }
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (state.topApps.isNotEmpty()) {
                        item { SectionHeader(stringResource(R.string.top_apps)) }
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.topApps.take(6), key = { it.id }) { app ->
                                    FeaturedAppCard(app = app, onClick = { onAppClick(app.id) })
                                }
                            }
                        }
                    }
                    if (state.categories.isNotEmpty()) {
                        item { SectionHeader(stringResource(R.string.categories)) }
                        item {
                            Row(
                                Modifier.horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.categories.take(8).forEach { cat ->
                                    AssistChip(onClick = {},
                                        label = { Text("${cat.category}  ${cat.count}") })
                                }
                            }
                        }
                    }
                    if (state.newApps.isNotEmpty()) {
                        item { SectionHeader(stringResource(R.string.new_apps)) }
                        items(state.newApps, key = { it.id }) { app ->
                            Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                AppCard(app = app, onClick = { onAppClick(app.id) })
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}
