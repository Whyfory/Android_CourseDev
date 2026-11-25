package com.example.android.notepad

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class NotesFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<NotesActivity>()

    @Test
    fun saveNoteThenFindItByTitleAndBodyWithTimestamp() {
        val title = "Title-" + UUID.randomUUID()
        val body = "Body-" + UUID.randomUUID()
        val repository = NoteRepository(compose.activity.contentResolver)
        try {
            compose.onNodeWithContentDescription("新建笔记").performClick()
            compose.onNodeWithTag("note-title").performTextReplacement(title)
            compose.onNodeWithTag("note-body").performTextReplacement(body)
            compose.onNodeWithText("保存").performClick()

            // 分别用只出现在标题、只出现在正文的词搜索，两次都应定位到同一篇。
            val search = compose.onNodeWithTag("note-search")
            compose.waitUntil(8000) {
                compose.onAllNodesWithText(title).fetchSemanticsNodes().size == 1
            }
            search.performTextReplacement(title.take(18))
            waitForMatch(title)
            search.performTextReplacement(body)
            waitForMatch(title)
        } finally {
            repository.list(title).filter { it.title == title }.forEach(repository::delete)
        }
    }

    private fun waitForMatch(title: String) {
        compose.waitUntil(8000) {
            compose.onAllNodesWithText("找到 1 篇笔记").fetchSemanticsNodes().size == 1
        }
        compose.onNodeWithText(title).assertIsDisplayed()
        compose.onNodeWithText("更新于", substring = true).assertIsDisplayed()
    }

    @Test
    fun backupRetainsChecklistAndTimestampsAndRestoreSkipsRepeatedRecords() {
        val activity = compose.activity
        val resolver = activity.contentResolver
        val repository = NoteRepository(resolver)
        val archive = File.createTempFile("note-test-", ".json", activity.cacheDir)
        val uri = Uri.fromFile(archive)
        val note = Note(
            title = "Archive-" + UUID.randomUUID(),
            body = "测试正文",
            tasks = listOf(Task("完成备份", true), Task("检查恢复"))
        )
        try {
            repository.save(note, isNew = true)
            val before = repository.list(note.title).single()
            NoteArchive.backup(resolver, uri, listOf(before))
            val restored = NoteArchive.readBackup(resolver, uri)
            assertEquals(listOf(before), restored)
            repository.delete(before)
            assertEquals(1, repository.importNotes(restored))
            assertEquals(0, repository.importNotes(restored))
            assertEquals(listOf(before), repository.list(note.title))
        } finally {
            repository.delete(note)
            archive.delete()
        }
    }
}
