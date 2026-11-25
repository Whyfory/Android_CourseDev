package com.example.android.notepad

import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.core.view.WindowCompat

class NotesActivity : ComponentActivity() {
    private val model by viewModels<NotesModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val dark = when (model.preferences.theme) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            SideEffect {
                val bars = WindowCompat.getInsetsController(window, window.decorView)
                bars.isAppearanceLightStatusBars = !dark
                bars.isAppearanceLightNavigationBars = !dark
            }
            NotePadTheme(dark) {
                NotePadApp(model, dark)
            }
        }
    }
}

@Composable
private fun NotePadApp(model: NotesModel, dark: Boolean) {
    val snackbar = remember { SnackbarHostState() }
    var confirmation by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(model) {
        model.notices.collect { snackbar.showSnackbar(it) }
    }

    // 系统选择器既可以访问设备文件，也可以访问已登录并提供文档入口的云盘。
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) {
        if (it != null) model.export(it)
    }
    val backup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {
        if (it != null) model.backup(it)
    }
    val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) model.readBackup(it)
    }

    val back: () -> Unit = {
        if (!model.busy) {
            if (model.page == Page.EDITOR && model.hasUnsavedChanges) {
                confirmation = "discard"
            } else {
                model.showList()
            }
        }
    }
    BackHandler(model.page != Page.NOTES) { back() }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (model.page == Page.NOTES) {
                ExtendedFloatingActionButton(
                    onClick = { model.openNote() },
                    modifier = Modifier.semantics { contentDescription = "新建笔记" },
                    text = { Text("新建笔记") },
                    icon = { Text("＋") }
                )
            }
        }
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets)) {
            when (model.page) {
                Page.NOTES -> NotesListScreen(model)
                Page.SETTINGS -> PreferencesScreen(
                    model = model,
                    onBack = back,
                    onBackup = {
                        val stamp = DateFormat.format("yyyyMMdd-HHmmss", System.currentTimeMillis())
                        backup.launch("NotePad-$stamp.json")
                    },
                    onRestore = { restore.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }
                )
                Page.EDITOR -> model.draft?.let { note ->
                    EditorScreen(
                        note = note,
                        model = model,
                        dark = dark,
                        onBack = back,
                        onExport = {
                            val fileName = note.displayTitle.map { ch ->
                                if (ch.isISOControl() || ch in "\\/:*?\"<>|") '_' else ch
                            }.joinToString("").trim().take(60).ifBlank { "笔记" }
                            export.launch("$fileName.txt")
                        },
                        onDelete = { confirmation = "delete" }
                    )
                }
            }
        }
    }

    if (confirmation.isNotEmpty()) {
        val deleting = confirmation == "delete"
        AlertDialog(
            onDismissRequest = { confirmation = "" },
            title = { Text(if (deleting) "删除笔记？" else "放弃未保存的修改？") },
            text = { Text(if (deleting) "笔记及其待办将一起删除。" else "返回列表后，这次编辑的内容会丢失。") },
            confirmButton = {
                TextButton(onClick = {
                    confirmation = ""
                    if (deleting) model.delete() else model.showList()
                }) { Text(if (deleting) "删除" else "放弃修改") }
            },
            dismissButton = { TextButton(onClick = { confirmation = "" }) { Text("取消") } }
        )
    }

    model.pendingRestore?.let { incoming ->
        AlertDialog(
            onDismissRequest = model::dismissRestore,
            title = { Text("恢复备份？") },
            text = {
                Text("备份中有 ${incoming.size} 篇笔记。已有的笔记保留本机版本，缺少的笔记会补回。")
            },
            confirmButton = { TextButton(onClick = model::restore) { Text("合并恢复") } },
            dismissButton = { TextButton(onClick = model::dismissRestore) { Text("取消") } }
        )
    }
}
