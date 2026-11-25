package com.example.android.notepad

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun PreferencesScreen(
    model: NotesModel,
    onBack: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    val current = model.preferences

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        PageHeader("设置", onBack)

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingsCard {
                Options("主题", ThemeMode.entries, current.theme, { it.label }) {
                    model.updatePreferences(current.copy(theme = it))
                }
                Options("编辑器背景", Paper.entries, current.paper, { it.label }) {
                    model.updatePreferences(current.copy(paper = it))
                }
                Options("正文字号", listOf(16, 18, 22), current.fontSize, { "$it sp" }) {
                    model.updatePreferences(current.copy(fontSize = it))
                }
                Options("列表排序", NoteOrder.entries, current.order, { it.label }) {
                    model.updatePreferences(current.copy(order = it))
                }
            }

            SettingsCard {
                Text("备份与恢复", style = MaterialTheme.typography.titleMedium)
                Text(
                    "将全部笔记和待办保存成一个文件，可在系统选择器中选择设备或已登录的云盘。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedButton(onClick = onBackup, enabled = !model.busy, modifier = Modifier.fillMaxWidth()) {
                    Text("保存备份")
                }
                OutlinedButton(onClick = onRestore, enabled = !model.busy, modifier = Modifier.fillMaxWidth()) {
                    Text("从备份恢复")
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> Options(title: String, values: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (value in values) {
            FilterChip(
                selected = value == selected,
                onClick = { onSelect(value) },
                label = { Text(label(value)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}
