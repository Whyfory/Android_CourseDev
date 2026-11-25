package com.example.lab03

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast

class MenuActivity : LabActivity() {
    private var fontSize = 16f
    private var fontColor = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLayout(R.layout.activity_menu)
        fontSize = savedInstanceState?.getFloat("size") ?: fontSize
        fontColor = savedInstanceState?.getInt("color") ?: fontColor
        val sample = findViewById<TextView>(R.id.sample_text)
        sample.textSize = fontSize
        sample.setTextColor(fontColor)

        val menuButton = findViewById<Button>(R.id.menu_button)
        menuButton.setOnClickListener {
            // 菜单的层级和选项由 XML 声明，Kotlin 负责处理点击。
            val menu = PopupMenu(this, menuButton)
            menu.inflate(R.menu.text_options)
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.size_small -> fontSize = 10f
                    R.id.size_medium -> fontSize = 16f
                    R.id.size_large -> fontSize = 20f
                    R.id.color_red -> fontColor = Color.RED
                    R.id.color_black -> fontColor = Color.BLACK
                    R.id.normal_item -> Toast.makeText(this, "点击了普通菜单项", Toast.LENGTH_SHORT).show()
                    else -> return@setOnMenuItemClickListener false
                }
                sample.textSize = fontSize
                sample.setTextColor(fontColor)
                true
            }
            menu.show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat("size", fontSize)
        outState.putInt("color", fontColor)
        super.onSaveInstanceState(outState)
    }
}
