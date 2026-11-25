package com.example.android.notepad

import android.content.ContentResolver
import android.content.ContentValues
import com.example.android.notepad.NotePad.Notes
import java.util.UUID

internal data class Task(val text: String, val done: Boolean = false)

internal data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val body: String = "",
    val created: Long = 0,
    val modified: Long = 0,
    val tasks: List<Task> = emptyList()
) {
    val displayTitle: String get() = title.ifBlank { "无标题" }
    val hasContent: Boolean get() = title.isNotBlank() || body.isNotBlank() || tasks.isNotEmpty()
}

internal enum class NoteOrder(val label: String, val sql: String) {
    MODIFIED("最近修改", "modified DESC, _id DESC"),
    CREATED("最近创建", "created DESC, _id DESC"),
    TITLE("标题顺序", "title COLLATE NOCASE ASC, _id DESC")
}

internal class NoteRepository(private val resolver: ContentResolver) {
    private val uri = Notes.CONTENT_URI

    fun list(query: String = "", order: NoteOrder = NoteOrder.MODIFIED): List<Note> {
        val text = query.trim()
        // 用参数绑定检索词，并转义 LIKE 的 %、_ 与反斜杠。
        val pattern = "%" + text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%"
        val where = if (text.isEmpty()) null else "(title LIKE ? ESCAPE '\\' OR note LIKE ? ESCAPE '\\')"
        val args = if (where == null) null else arrayOf(pattern, pattern)

        val projection = arrayOf(
            Notes.COLUMN_NAME_BACKUP_ID, Notes.COLUMN_NAME_TITLE, Notes.COLUMN_NAME_NOTE,
            Notes.COLUMN_NAME_CREATE_DATE, Notes.COLUMN_NAME_MODIFICATION_DATE, Notes.COLUMN_NAME_CHECKLIST
        )
        return requireNotNull(resolver.query(uri, projection, where, args, order.sql))
            .use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(Note(
                            id = cursor.getString(0),
                            title = cursor.getString(1).orEmpty(),
                            body = cursor.getString(2).orEmpty(),
                            created = cursor.getLong(3),
                            modified = cursor.getLong(4),
                            tasks = NoteArchive.readTasks(cursor.getString(5))
                        ))
                    }
                }
            }
    }

    fun save(draft: Note, isNew: Boolean) {
        val now = System.currentTimeMillis()
        val title = draft.title.trim().ifBlank {
            draft.body.lineSequence().firstOrNull { it.isNotBlank() }?.trim()?.take(40).orEmpty()
        }
        val note = draft.copy(
            title = title,
            created = if (isNew) now else draft.created,
            modified = now
        )
        if (isNew) {
            checkNotNull(resolver.insert(uri, note.toValues()))
        } else {
            check(resolver.update(uri, note.toValues(), "backup_id = ?", arrayOf(note.id)) == 1)
        }
    }

    fun delete(note: Note) {
        resolver.delete(uri, "backup_id = ?", arrayOf(note.id))
    }

    fun importNotes(notes: List<Note>): Int {
        return resolver.bulkInsert(uri, notes.map { it.toValues() }.toTypedArray())
    }

    private fun Note.toValues() = ContentValues().apply {
        put(Notes.COLUMN_NAME_BACKUP_ID, id)
        put(Notes.COLUMN_NAME_TITLE, title)
        put(Notes.COLUMN_NAME_NOTE, body)
        put(Notes.COLUMN_NAME_CREATE_DATE, created)
        put(Notes.COLUMN_NAME_MODIFICATION_DATE, modified)
        put(Notes.COLUMN_NAME_CHECKLIST, NoteArchive.encodeTasks(tasks).toString())
    }
}
