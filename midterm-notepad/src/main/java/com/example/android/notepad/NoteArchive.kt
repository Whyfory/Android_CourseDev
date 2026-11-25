package com.example.android.notepad

import android.content.ContentResolver
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID

internal object NoteArchive {
    private const val FORMAT = "course-notepad"
    private const val VERSION = 1
    private const val MAX_BYTES = 16 * 1024 * 1024

    fun encodeTasks(tasks: List<Task>): JSONArray = JSONArray().apply {
        for (task in tasks) {
            put(JSONObject().put("text", task.text).put("done", task.done))
        }
    }

    fun readTasks(json: String): List<Task> = readTasks(JSONArray(json))

    private fun readTasks(array: JSONArray): List<Task> = List(array.length()) { index ->
        val task = array.getJSONObject(index)
        val text = task.get("text") as String
        require(text.isNotBlank())
        Task(text, task.get("done") as Boolean)
    }

    fun exportText(resolver: ContentResolver, uri: Uri, note: Note) {
        val content = buildString {
            appendLine(note.displayTitle)
            appendLine()
            appendLine(note.body)
            if (note.tasks.isNotEmpty()) {
                appendLine()
                appendLine("待办")
                for (task in note.tasks) {
                    val box = if (task.done) "[x]" else "[ ]"
                    appendLine("- $box " + task.text)
                }
            }
        }
        write(resolver, uri, content.toByteArray(Charsets.UTF_8))
    }

    fun backup(resolver: ContentResolver, uri: Uri, notes: List<Note>) {
        val records = JSONArray()
        for (note in notes) {
            records.put(JSONObject()
                .put("id", note.id)
                .put("title", note.title)
                .put("body", note.body)
                .put("created", note.created)
                .put("modified", note.modified)
                .put("tasks", encodeTasks(note.tasks)))
        }
        val archive = JSONObject().put("format", FORMAT).put("version", VERSION).put("notes", records)
        val bytes = archive.toString().toByteArray(Charsets.UTF_8)
        if (bytes.size > MAX_BYTES) throw IOException("备份文件超过 16 MB")
        write(resolver, uri, bytes)
    }

    fun readBackup(resolver: ContentResolver, uri: Uri): List<Note> {
        val input = resolver.openInputStream(uri) ?: throw IOException("无法读取文件")
        val bytes = input.use {
            val buffer = ByteArray(8192)
            val output = ByteArrayOutputStream()
            while (true) {
                val count = it.read(buffer)
                if (count == -1) break
                if (output.size() + count > MAX_BYTES) throw IOException("备份文件超过 16 MB")
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }

        // 先检查整个文件和每一条记录。全部有效后才会交给数据库。
        val archive = JSONObject(bytes.toString(Charsets.UTF_8).removePrefix("\uFEFF"))
        require(archive.get("format") == FORMAT && archive.get("version") == VERSION)
        val array = archive.getJSONArray("notes")
        val ids = mutableSetOf<String>()
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            val id = item.get("id") as String
            require(UUID.fromString(id).toString() == id && ids.add(id))
            Note(
                id = id,
                title = item.get("title") as String,
                body = item.get("body") as String,
                created = timestamp(item, "created"),
                modified = timestamp(item, "modified"),
                tasks = readTasks(item.getJSONArray("tasks"))
            )
        }
    }

    private fun timestamp(note: JSONObject, key: String): Long {
        val value = note.get(key)
        require(value is Int || value is Long)
        return (value as Number).toLong().also { require(it >= 0) }
    }

    private fun write(resolver: ContentResolver, uri: Uri, bytes: ByteArray) {
        val output = resolver.openOutputStream(uri, "wt") ?: throw IOException("无法写入文件")
        output.use { it.write(bytes) }
    }
}
