package com.kurt.mynoteapp.ui.note.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun NoteDetailRoute(
    noteId: Long,
    onBack: () -> Unit,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(noteId) { viewModel.onIntent(NoteDetailIntent.Load(noteId)) }
    LaunchedEffect(state.closeRequested) {
        if (state.closeRequested) onBack()
    }
    NoteDetailScreen(
        state = state,
        onTitleChange = { viewModel.onIntent(NoteDetailIntent.ChangeTitle(it)) },
        onContentChange = { viewModel.onIntent(NoteDetailIntent.ChangeContent(it)) },
        onTagInputChange = { viewModel.onIntent(NoteDetailIntent.ChangeTagInput(it)) },
        onAddTag = { viewModel.onIntent(NoteDetailIntent.AddTag(it)) },
        onRemoveTag = { viewModel.onIntent(NoteDetailIntent.RemoveTag(it)) },
        onSave = { viewModel.onIntent(NoteDetailIntent.Save) },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    state: NoteDetailUiState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onTagInputChange: (String) -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Not Detayı") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri") }
                },
                actions = {
                    IconButton(onClick = onSave) { Icon(Icons.Filled.Done, contentDescription = "Kaydet") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                        value = state.note.title,
                        onValueChange = onTitleChange,
                        label = { Text("Başlık") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = state.note.content,
                        onValueChange = onContentChange,
                        label = { Text("İçerik") },
                modifier = Modifier.fillMaxWidth().height(220.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.tagInput,
                            onValueChange = onTagInputChange,
                            label = { Text("Etiket ekle") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { onAddTag(state.tagInput) }),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Button(onClick = { onAddTag(state.tagInput) }, enabled = state.tagInput.isNotBlank()) {
                            Text("Ekle")
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.note.tags.forEach { tag ->
                    AssistChip(
                        onClick = { onRemoveTag(tag) },
                        label = { Text(tag) },
                        colors = AssistChipDefaults.assistChipColors()
                    )
                }
            }

            Button(onClick = onSave, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text("Kaydet", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}


