package com.example.android.notepad

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun NotesListScreen(model: NotesModel) {
    val focus = LocalFocusManager.current
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "拾页 NotePad",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = {
                focus.clearFocus()
                model.showSettings()
            }) { Text("设置") }
        }

        OutlinedTextField(
            value = model.query,
            onValueChange = model::search,
            modifier = Modifier.fillMaxWidth().testTag("note-search"),
            label = { Text("搜索标题或正文") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
            trailingIcon = if (model.query.isNotEmpty()) {
                { TextButton(onClick = { model.search("") }) { Text("清空") } }
            } else {
                null
            },
            shape = MaterialTheme.shapes.large
        )

        Spacer(Modifier.height(16.dp))
        if (model.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        } else {
            val label = if (model.query.isBlank()) "共 ${model.notes.size} 篇笔记" else "找到 ${model.notes.size} 篇笔记"
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
        }

        if (!model.loading && model.notes.isEmpty()) {
            Column(Modifier.padding(vertical = 40.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    if (model.query.isBlank()) "还没有笔记" else "没有找到笔记",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    if (model.query.isBlank()) "点「新建笔记」写下第一篇。" else "试试标题或正文中的其他关键词。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(top = 12.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(model.notes, key = { it.id }) { note ->
                    NoteCard(note, model.query) {
                        focus.clearFocus()
                        model.openNote(note)
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteCard(note: Note, query: String, onOpen: () -> Unit) {
    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                note.displayTitle,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (note.body.isNotBlank()) {
                Text(
                    searchExcerpt(note.body, query),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                "更新于 " + DateFormat.format("yyyy-MM-dd HH:mm:ss", note.modified),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (note.tasks.isNotEmpty()) {
                Text(
                    "待办 ${note.tasks.count { it.done }} / ${note.tasks.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun searchExcerpt(body: String, query: String): String {
    val text = body.trim()
    val term = query.trim()
    val match = if (term.isEmpty()) -1 else text.indexOf(term, ignoreCase = true)
    val start = (match - 22).coerceAtLeast(0)
    return (if (start > 0) "…" else "") + text.substring(start)
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F4ED)
@Composable
private fun NoteCardPreview() {
    NotePadTheme(false) {
        NoteCard(
            Note(title = "期中实验", body = "复习笔记查询和文件备份。", modified = 1795587205000, tasks = listOf(Task("检查时间戳", true))),
            query = "",
            onOpen = {}
        )
    }
}
