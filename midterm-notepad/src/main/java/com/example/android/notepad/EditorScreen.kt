package com.example.android.notepad

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun EditorScreen(
    note: Note,
    model: NotesModel,
    dark: Boolean,
    onBack: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    val focus = LocalFocusManager.current
    var taskText by rememberSaveable(note.id) { mutableStateOf("") }
    val saveEnabled = !model.busy && note.hasContent &&
        (!model.hasSavedNote || model.hasUnsavedChanges)

    val addTask = {
        val text = taskText.trim()
        if (text.isNotEmpty() && !model.busy) {
            model.edit { it.copy(tasks = it.tasks + Task(text)) }
            taskText = ""
            focus.clearFocus()
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        PageHeader(
            title = if (model.hasSavedNote) "编辑笔记" else "新建笔记",
            onBack = onBack
        ) {
            Button(
                enabled = saveEnabled,
                onClick = {
                    focus.clearFocus()
                    model.save()
                }
            ) { Text("保存") }
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = note.title,
                onValueChange = { text -> model.edit { it.copy(title = text) } },
                modifier = Modifier.fillMaxWidth().testTag("note-title"),
                label = { Text("标题") },
                singleLine = true,
                enabled = !model.busy,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            val paper = model.preferences.paper.editorColor(dark)
            OutlinedTextField(
                value = note.body,
                onValueChange = { text -> model.edit { it.copy(body = text) } },
                modifier = Modifier.fillMaxWidth().testTag("note-body"),
                label = { Text("正文") },
                minLines = 6,
                enabled = !model.busy,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = model.preferences.fontSize.sp,
                    lineHeight = (model.preferences.fontSize + 8).sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = paper,
                    unfocusedContainerColor = paper,
                    disabledContainerColor = paper
                )
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("待办", style = MaterialTheme.typography.titleLarge)
                Text(
                    "${note.tasks.count { it.done }} / ${note.tasks.size} 已完成",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = taskText,
                    onValueChange = { taskText = it },
                    label = { Text("添加一项待办") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !model.busy,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { addTask() })
                )
                Button(onClick = addTask, enabled = taskText.isNotBlank() && !model.busy) { Text("添加") }
            }

            // 每次勾选都生成新的清单，完成数和列表预览从同一份状态计算。
            note.tasks.forEachIndexed { index, task ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().padding(end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.done,
                            onCheckedChange = { checked ->
                                model.edit { current ->
                                    current.copy(tasks = current.tasks.mapIndexed { position, item ->
                                        if (position == index) item.copy(done = checked) else item
                                    })
                                }
                            },
                            enabled = !model.busy,
                            modifier = Modifier.semantics { contentDescription = task.text + "完成状态" }
                        )
                        Text(
                            task.text,
                            modifier = Modifier.weight(1f),
                            textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None
                        )
                        TextButton(
                            onClick = {
                                model.edit { current ->
                                    current.copy(tasks = current.tasks.filterIndexed { position, _ -> position != index })
                                }
                            },
                            enabled = !model.busy,
                            modifier = Modifier.semantics { contentDescription = "移除待办 " + task.text }
                        ) { Text("移除") }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(
                    onClick = {
                        focus.clearFocus()
                        onExport()
                    },
                    enabled = !model.busy && note.hasContent
                ) { Text("导出 TXT") }
                if (model.hasSavedNote) {
                    TextButton(onClick = onDelete, enabled = !model.busy) {
                        Text("删除笔记", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
internal fun PageHeader(title: String, onBack: () -> Unit, action: @Composable () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TextButton(onClick = onBack) { Text("返回") }
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
        action()
    }
}
