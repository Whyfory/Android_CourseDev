package com.example.lab03

import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.ListView

class SelectionActivity : LabActivity() {
    private lateinit var items: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLayout(R.layout.activity_selection)
        val list = findViewById<ListView>(R.id.selection_list)
        items = savedInstanceState?.getStringArrayList("items")
            ?: arrayListOf("One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight")
        val adapter = ArrayAdapter(this, R.layout.item_selection, items)
        list.adapter = adapter
        list.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL

        // 长按一行进入系统 ActionMode，随后点按其他行可增减选中项。
        list.setMultiChoiceModeListener(object : AbsListView.MultiChoiceModeListener {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                mode.menuInflater.inflate(R.menu.selection_actions, menu)
                return true
            }

            override fun onItemCheckedStateChanged(mode: ActionMode, position: Int, id: Long, checked: Boolean) {
                mode.title = "已选中 ${list.checkedItemCount} 项"
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                if (item.itemId != R.id.delete_selected) return false

                val checked = list.checkedItemPositions
                // 从后往前删除，前面记录的位置就不会因删除而变化。
                for (position in items.indices.reversed()) {
                    if (checked[position]) items.removeAt(position)
                }
                list.clearChoices()
                adapter.notifyDataSetChanged()
                mode.finish()
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

            override fun onDestroyActionMode(mode: ActionMode) = Unit
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArrayList("items", items)
        super.onSaveInstanceState(outState)
    }
}
