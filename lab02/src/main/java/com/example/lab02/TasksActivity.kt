package com.example.lab02

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

class TasksActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                primary = Color(0xFFAB1822),
                background = Color(0xFFF4F8FA)
            )) {
                Scaffold { padding ->
                    TasksScreen(Modifier.fillMaxSize().padding(padding).padding(16.dp))
                }
            }
        }
    }
}

private data class StudyTask(val title: String, val done: Boolean = false)

// 把任务保存成 Android 可以恢复的字符串和布尔值，旋转后仍保留进度。
private val tasksSaver = listSaver<List<StudyTask>, Any>(
    save = { tasks -> tasks.flatMap { listOf(it.title, it.done) } },
    restore = { values -> values.chunked(2).map { StudyTask(it[0] as String, it[1] as Boolean) } }
)

@Composable
private fun TasksScreen(modifier: Modifier) {
    var input by rememberSaveable { mutableStateOf("") }
    var tasks by rememberSaveable(stateSaver = tasksSaver) {
        mutableStateOf(listOf(
            StudyTask("学习 Column 和 Row", true),
            StudyTask("学习状态管理"),
            StudyTask("完成 Compose 实验")
        ))
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("课程学习任务", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("请输入学习任务") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    tasks = tasks + StudyTask(input.trim())
                    input = ""
                },
                enabled = input.isNotBlank()
            ) {
                Text("添加")
            }
        }

        // 完成数和删除线共用任务状态，每次更新时一起刷新。
        Text("已完成：${tasks.count { it.done }} / ${tasks.size}")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(tasks) { index, task ->
                Card {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = task.done, onCheckedChange = { checked ->
                            tasks = tasks.mapIndexed { position, item ->
                                if (position == index) item.copy(done = checked) else item
                            }
                        })
                        Text(
                            task.title,
                            modifier = Modifier.weight(1f),
                            textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None
                        )
                        TextButton(onClick = { tasks = tasks.filterIndexed { position, _ -> position != index } }) {
                            Text("删除")
                        }
                    }
                }
            }
        }
    }
}
