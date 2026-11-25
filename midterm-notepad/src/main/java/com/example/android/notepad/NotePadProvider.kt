/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.notepad

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import com.example.android.notepad.NotePad.Notes
import java.util.UUID

/**
 * 保留原 NotePad 的 notes 表、时间字段和 URI 约定。
 * UI 通过 ContentResolver 访问数据，Provider 统一负责 SQLite 读写。
 */
class NotePadProvider : ContentProvider() {
    private lateinit var helper: NotesDatabase
    private val resolver get() = requireNotNull(context).contentResolver

    private val matcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(NotePad.AUTHORITY, "notes", 1)
        addURI(NotePad.AUTHORITY, "notes/#", 2)
    }

    override fun onCreate(): Boolean {
        helper = NotesDatabase(requireNotNull(context))
        return true
    }

    override fun getType(uri: Uri): String = when (matcher.match(uri)) {
        1 -> Notes.CONTENT_TYPE
        2 -> Notes.CONTENT_ITEM_TYPE
        else -> throw IllegalArgumentException("未知的笔记 URI")
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val (where, args) = whereFor(uri, selection, selectionArgs)
        return helper.readableDatabase.query(
            Notes.TABLE_NAME, projection, where, args,
            null, null, sortOrder ?: Notes.DEFAULT_SORT_ORDER
        ).apply { setNotificationUri(resolver, Notes.CONTENT_URI) }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        requireCollection(uri)
        val id = helper.writableDatabase.insertOrThrow(
            Notes.TABLE_NAME, null, requireNotNull(values)
        )
        notifyNotesChanged()
        return ContentUris.withAppendedId(Notes.CONTENT_URI, id)
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, args: Array<out String>?): Int {
        val (where, allArgs) = whereFor(uri, selection, args)
        val changed = helper.writableDatabase.update(Notes.TABLE_NAME, values, where, allArgs)
        if (changed > 0) notifyNotesChanged()
        return changed
    }

    override fun delete(uri: Uri, selection: String?, args: Array<out String>?): Int {
        val (where, allArgs) = whereFor(uri, selection, args)
        val changed = helper.writableDatabase.delete(Notes.TABLE_NAME, where, allArgs)
        if (changed > 0) notifyNotesChanged()
        return changed
    }

    override fun bulkInsert(uri: Uri, values: Array<out ContentValues>): Int {
        requireCollection(uri)
        val db = helper.writableDatabase
        var added = 0
        db.beginTransaction()
        try {
            for (note in values) {
                // 同一份备份可以重复导入，唯一标识用于保留已有笔记。
                val id = db.insertWithOnConflict(Notes.TABLE_NAME, null, note, SQLiteDatabase.CONFLICT_IGNORE)
                if (id != -1L) added++
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        if (added > 0) notifyNotesChanged()
        return added
    }

    private fun requireCollection(uri: Uri) {
        require(matcher.match(uri) == 1) { "请使用笔记集合 URI" }
    }

    private fun notifyNotesChanged() {
        resolver.notifyChange(Notes.CONTENT_URI, null)
    }

    private fun whereFor(
        uri: Uri, selection: String?, args: Array<out String>?
    ): Pair<String?, Array<out String>?> = when (matcher.match(uri)) {
        1 -> selection to args
        2 -> {
            val idClause = "${Notes._ID} = ?"
            val where = if (selection.isNullOrBlank()) idClause else "$idClause AND ($selection)"
            where to (listOf(ContentUris.parseId(uri).toString()) + args.orEmpty()).toTypedArray()
        }
        else -> throw IllegalArgumentException("未知的笔记 URI")
    }

    private class NotesDatabase(context: Context) : SQLiteOpenHelper(context, "note_pad.db", null, 3) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE notes (
                    _id INTEGER PRIMARY KEY,
                    title TEXT,
                    note TEXT,
                    created INTEGER,
                    modified INTEGER,
                    backup_id TEXT NOT NULL,
                    checklist TEXT NOT NULL DEFAULT '[]'
                )
                """.trimIndent()
            )
            createBackupIndex(db)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion < 3) {
                // 升级原版表时补默认值和备份 ID，保留原标题、正文及时间戳。
                db.execSQL("ALTER TABLE notes ADD COLUMN backup_id TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE notes ADD COLUMN checklist TEXT NOT NULL DEFAULT '[]'")
                db.query("notes", arrayOf("_id"), null, null, null, null, null).use { cursor ->
                    while (cursor.moveToNext()) {
                        db.update(
                            "notes",
                            ContentValues().apply { put("backup_id", UUID.randomUUID().toString()) },
                            "_id = ?",
                            arrayOf(cursor.getLong(0).toString())
                        )
                    }
                }
                createBackupIndex(db)
            }
        }

        private fun createBackupIndex(db: SQLiteDatabase) {
            db.execSQL("CREATE UNIQUE INDEX notes_backup_id ON notes(backup_id)")
        }
    }
}
