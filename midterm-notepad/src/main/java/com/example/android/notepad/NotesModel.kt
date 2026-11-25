package com.example.android.notepad

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal enum class Page { NOTES, EDITOR, SETTINGS }

internal class NotesModel(application: Application) : AndroidViewModel(application) {
    private val resolver = application.contentResolver
    private val repository = NoteRepository(resolver)
    private val prefsStore = PreferencesStore(application)
    private val messages = Channel<String>(Channel.BUFFERED)
    private var searchJob: Job? = null
    private var original: Note? = null

    val notices = messages.receiveAsFlow()
    var page by mutableStateOf(Page.NOTES)
        private set
    var preferences by mutableStateOf(prefsStore.read())
        private set
    var query by mutableStateOf("")
        private set
    var notes by mutableStateOf(emptyList<Note>())
        private set
    var draft by mutableStateOf<Note?>(null)
        private set
    var loading by mutableStateOf(true)
        private set
    var busy by mutableStateOf(false)
        private set
    var pendingRestore by mutableStateOf<List<Note>?>(null)
        private set

    val hasSavedNote get() = original != null
    val hasUnsavedChanges get() = if (original == null) draft?.hasContent == true else draft != original

    init {
        refresh()
    }

    fun search(text: String) {
        query = text
        refresh()
    }

    private fun refresh() {
        searchJob?.cancel()
        val text = query
        val order = preferences.order
        loading = true
        searchJob = viewModelScope.launch {
            try {
                notes = withContext(Dispatchers.IO) { repository.list(text, order) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                tell("读取笔记失败，请重试")
            }
            loading = false
        }
    }

    fun openNote(note: Note? = null) {
        original = note
        draft = note ?: Note()
        page = Page.EDITOR
    }

    fun edit(change: (Note) -> Note) {
        if (!busy) draft = draft?.let(change)
    }

    fun showSettings() {
        page = Page.SETTINGS
    }

    fun showList() {
        draft = null
        original = null
        page = Page.NOTES
    }

    fun updatePreferences(value: NotePreferences) {
        val reorder = preferences.order != value.order
        preferences = value
        prefsStore.save(value)
        if (reorder) refresh()
    }

    fun save() {
        val note = draft ?: return
        if (!note.hasContent) return
        val isNew = original == null
        runOperation("保存失败，请重试", { repository.save(note, isNew) }) {
            if (isNew) query = ""
            showList()
            refresh()
            tell("笔记已保存")
        }
    }

    fun delete() {
        val note = original ?: return
        runOperation("删除失败，请重试", { repository.delete(note) }) {
            showList()
            refresh()
            tell("笔记已删除")
        }
    }

    fun export(uri: Uri) {
        val note = draft ?: return
        runOperation("导出失败，请检查保存位置", { NoteArchive.exportText(resolver, uri, note) }) {
            tell("已导出 TXT")
        }
    }

    fun backup(uri: Uri) {
        runOperation("备份失败，请检查空间或云盘连接", {
            val allNotes = repository.list()
            NoteArchive.backup(resolver, uri, allNotes)
            allNotes.size
        }) { count ->
            tell("已写入 $count 篇笔记的备份")
        }
    }

    fun readBackup(uri: Uri) {
        runOperation("无法读取备份，请选择有效的 NotePad 文件", {
            NoteArchive.readBackup(resolver, uri)
        }) { pendingRestore = it }
    }

    fun dismissRestore() {
        pendingRestore = null
    }

    fun restore() {
        val incoming = pendingRestore ?: return
        pendingRestore = null
        runOperation("恢复失败，请重试", { repository.importNotes(incoming) }) { added ->
            query = ""
            refresh()
            val skipped = incoming.size - added
            tell("已恢复 $added 篇，保留本机已有的 $skipped 篇")
        }
    }

    // 读写文件与 SQLite 放在 IO 线程，按钮在操作期间禁用。
    private fun <T> runOperation(error: String, work: () -> T, onSuccess: (T) -> Unit) {
        if (busy) return
        busy = true
        viewModelScope.launch {
            try {
                onSuccess(withContext(Dispatchers.IO) { work() })
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                tell(error)
            } finally {
                busy = false
            }
        }
    }

    private fun tell(message: String) {
        messages.trySend(message)
    }
}
