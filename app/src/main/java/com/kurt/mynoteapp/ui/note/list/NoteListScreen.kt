package com.kurt.mynoteapp.ui.note.list

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kurt.mynoteapp.data.local.Note
import com.kurt.mynoteapp.util.DateFormatUtils

// Sabit palette'ler - performans için
private val LIGHT_PALETTE = listOf(
    Color(0xFF7C4DFF),
    Color(0xFFFF7043),
    Color(0xFF26A69A),
    Color(0xFFF9A825),
    Color(0xFFD81B60)
)

private val DARK_PALETTE = listOf(
    Color(0xFFB39DDB),
    Color(0xFFFFAB91),
    Color(0xFFA5D6A7),
    Color(0xFFFFE082),
    Color(0xFFF48FB1)
)

@Composable
fun NoteListRoute(
    onNoteClick: (Long) -> Unit,
    onAddNew: () -> Unit,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    val onClearFilters = remember { { viewModel.onIntent(NoteListIntent.ClearFilters) } }
    
    NoteListScreen(
        state = state,
        onDelete = { viewModel.onIntent(NoteListIntent.Delete(it)) },
        onNoteClick = onNoteClick,
        onAddNew = onAddNew,
        onQueryChange = { viewModel.onIntent(NoteListIntent.ChangeQuery(it)) },
        onToggleTag = { viewModel.onIntent(NoteListIntent.ToggleTag(it)) },
        onClearFilters = onClearFilters
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    state: NoteListUiState,
    onDelete: (Note) -> Unit,
    onNoteClick: (Long) -> Unit,
    onAddNew: () -> Unit,
    onQueryChange: (String) -> Unit,
    onToggleTag: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    val listState = rememberLazyListState()
    
    // Yeni not eklendiğinde otomatik olarak en üste scroll
    LaunchedEffect(state.filteredNotes.firstOrNull()?.id, state.filteredNotes.size) {
        val shouldScrollTop =
            listState.firstVisibleItemIndex > 0 &&
                    state.filteredNotes.isNotEmpty()
        if (shouldScrollTop) {
            listState.animateScrollToItem(0)
        }
    }
    
    Scaffold(
        topBar = { TopAppBar(title = { Text("Notlar") }) },
        floatingActionButton = { FloatingActionButton(onClick = onAddNew) { Icon(Icons.Filled.Add, "Yeni not") } }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SearchAndFilters(
                query = state.query,
                onQueryChange = onQueryChange,
                allTags = remember(state.allTags) { state.allTags.toList().sorted() },
                selected = state.tagFilters,
                onToggle = onToggleTag
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.filteredNotes.isEmpty()) {
                val hasTagFilters = remember(state.tagFilters) { state.tagFilters.isNotEmpty() }
                val onClearFiltersCallback = remember(state.tagFilters, onClearFilters) {
                    if (state.tagFilters.isNotEmpty()) onClearFilters else null
                }
                
                EmptyState(
                    onAddNew = onAddNew, 
                    hasTagFilters = hasTagFilters, 
                    onClearFilters = onClearFiltersCallback
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)
                ) {
                    items(
                        items = state.filteredNotes, 
                        key = { it.id },
                        contentType = { "note" }
                    ) { note ->
                        SwipeToDismissItem(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            onDismiss = { onDelete(note) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissItem(
    note: Note,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { value ->
        if (value == SwipeToDismissBoxValue.EndToStart) {
            onDismiss(); true
        } else false
    })

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = { DismissBackground(dismissState) },
        content = { NoteCard(note = note, onClick = onClick) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissBackground(state: SwipeToDismissBoxState) {
    val show = state.targetValue == SwipeToDismissBoxValue.EndToStart ||
        state.currentValue == SwipeToDismissBoxValue.EndToStart
    if (!show) return
    val bg = Color(0xFFFFE6E6)
    Surface(color = bg) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Sil", tint = Color(0xFFD32F2F))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoteCard(note: Note, onClick: () -> Unit) {
    val dateText = remember(note.createdAt) {
        DateFormatUtils.format(note.createdAt)
    }
    
    val dark = isSystemInDarkTheme()
    
    val accentColor = remember(note.id, note.createdAt, dark) {
        val key = if (note.id != 0L) note.id else note.createdAt
        paletteFor(key, dark)
    }
    
    ElevatedCard(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (dark) 0.25f else 0.15f)), 
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (dark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f) else Color(0xFFF7F8FA)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            RowHeader(
                title = note.title.ifBlank { "Başlıksız" },
                dateText = dateText,
                accent = accentColor,
                textColor = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.content.ifBlank { "İçerik yok" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                val chipContainer = if (dark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF2F2F2)
                val chipLabel = MaterialTheme.colorScheme.onSurface
                FlowRow(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    note.tags.take(3).forEach { tag ->
                        AssistChip(
                            onClick = {},
                            label = { Text(tag, color = chipLabel) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = chipContainer
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowHeader(title: String, dateText: String, accent: Color, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .height(36.dp)
                .width(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(accent.copy(alpha = 0.6f))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.height(14.dp), tint = accent.copy(alpha = 0.9f))
                Spacer(modifier = Modifier.width(4.dp))
                Text(dateText, style = MaterialTheme.typography.labelMedium, color = textColor.copy(alpha = 0.7f))
            }
        }
    }
}

private fun paletteFor(key: Long, dark: Boolean): Color {
    val palettes = if (dark) DARK_PALETTE else LIGHT_PALETTE
    val idx = (key % palettes.size).toInt()
    return palettes[idx]
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchAndFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    allTags: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    val dark = isSystemInDarkTheme()
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = if (dark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF2F2F2),
        labelColor = MaterialTheme.colorScheme.onSurface,
        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
        selectedLabelColor = MaterialTheme.colorScheme.onSurface
    )
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Ara...") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Ara") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        if (allTags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                allTags.forEach { tag ->
                    val isSelected = tag in selected
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToggle(tag) },
                        label = { Text(tag) },
                        colors = chipColors
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    onAddNew: () -> Unit,
    hasTagFilters: Boolean = false,
    onClearFilters: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (hasTagFilters) {
            Text("Bu tag'de not bulunamadı", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Farklı tag'ler seçin veya filtreleri temizleyin", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            if (onClearFilters != null) {
                Button(onClick = onClearFilters, colors = ButtonDefaults.buttonColors()) { 
                    Text("Filtreleri Temizle") 
                }
            }
        } else {
            Text("Hiç not yok", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Yeni bir not oluşturarak başlayın", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAddNew, colors = ButtonDefaults.buttonColors()) { 
                Text("Yeni Not Oluştur") 
            }
        }
    }
}


