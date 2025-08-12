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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberDismissState
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.DismissState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.Scaffold
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kurt.mynoteapp.data.local.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoteListRoute(
    onNoteClick: (Long) -> Unit,
    onAddNew: () -> Unit,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    NoteListScreen(
        state = state,
        onDelete = { viewModel.onIntent(NoteListIntent.Delete(it)) },
        onNoteClick = onNoteClick,
        onAddNew = onAddNew,
        onQueryChange = { viewModel.onIntent(NoteListIntent.ChangeQuery(it)) },
        onToggleTag = { viewModel.onIntent(NoteListIntent.ToggleTag(it)) }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NoteListScreen(
    state: NoteListUiState,
    onDelete: (Note) -> Unit,
    onNoteClick: (Long) -> Unit,
    onAddNew: () -> Unit,
    onQueryChange: (String) -> Unit,
    onToggleTag: (String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.snackbar) {
        val msg = state.snackbar
        if (msg != null) snackbarHostState.showSnackbar(message = msg)
    }

    val allTags = remember(state.notes) { state.notes.flatMap { it.tags }.toSet() }
    val filtered = remember(state.notes, state.query, state.tagFilters) {
        state.notes.filter { n ->
            (state.query.isBlank() || n.title.contains(state.query, true) || n.content.contains(state.query, true)) &&
            (state.tagFilters.isEmpty() || n.tags.any { it in state.tagFilters })
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Notlar") }) },
        floatingActionButton = { FloatingActionButton(onClick = onAddNew) { Icon(Icons.Filled.Add, null) } },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SearchAndFilters(
                query = state.query,
                onQueryChange = onQueryChange,
                allTags = allTags.toList(),
                selected = state.tagFilters,
                onToggle = onToggleTag
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filtered.isEmpty()) {
                EmptyState(onAddNew)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)
                ) {
                    items(items = filtered, key = { it.id }) { note ->
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwipeToDismissItem(
    note: Note,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val dismissState = rememberDismissState(confirmStateChange = { value ->
        if (value == DismissValue.DismissedToStart) {
            onDismiss(); true
        } else false
    })

    SwipeToDismiss(
        state = dismissState,
        background = { DismissBackground(dismissState) },
        dismissContent = {
            NoteCard(note = note, onClick = onClick)
        },
        directions = setOf(DismissDirection.EndToStart)
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DismissBackground(state: DismissState) {
    val bg = if (state.targetValue == DismissValue.DismissedToStart) Color(0xFFFFE6E6) else Color.Transparent
    Surface(color = bg) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(imageVector = Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFD32F2F))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoteCard(note: Note, onClick: () -> Unit) {
    val dateText = remember(note.createdAt) {
        SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault()).format(Date(note.createdAt))
    }
    
    val dark = isSystemInDarkTheme()
    
    val accentColor = remember(note.id, note.createdAt, dark) {
        val key = if (note.id != 0L) note.id else note.createdAt
        paletteFor(key, dark)
    }
    
    ElevatedCard(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
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
                FlowRow(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    note.tags.take(3).forEach { tag ->
                        AssistChip(
                            onClick = {},
                            label = { Text(tag, color = MaterialTheme.colorScheme.onSurface) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFFF2F2F2)
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
    val palettes = if (dark) {
        listOf(
            Color(0xFFB39DDB),
            Color(0xFFFFAB91),
            Color(0xFFA5D6A7),
            Color(0xFFFFE082),
            Color(0xFFF48FB1)
        )
    } else {
        listOf(
            Color(0xFF7C4DFF),
            Color(0xFFFF7043),
            Color(0xFF26A69A),
            Color(0xFFF9A825),
            Color(0xFFD81B60)
        )
    }
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
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Ara...") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
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
                        label = { Text(tag) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onAddNew: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hiç not yok", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Yeni bir not oluşturarak başlayın", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddNew, colors = ButtonDefaults.buttonColors()) { Text("Yeni Not Oluştur") }
    }
}


