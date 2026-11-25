package com.example.lab03

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : LabActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLayout(R.layout.activity_main)

        findViewById<Button>(R.id.open_list).setOnClickListener {
            startActivity(Intent(this, AnimalListActivity::class.java))
        }
        findViewById<Button>(R.id.open_dialog).setOnClickListener { showLoginDialog() }
        findViewById<Button>(R.id.open_menu).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
        }
        findViewById<Button>(R.id.open_selection).setOnClickListener {
            startActivity(Intent(this, SelectionActivity::class.java))
        }
        findViewById<Button>(R.id.open_recycler).setOnClickListener {
            startActivity(Intent(this, RecyclerActivity::class.java))
        }
    }

    private fun showLoginDialog() {
        // 先加载自定义布局，再通过 setView 放进对话框。
        val form = layoutInflater.inflate(R.layout.dialog_sign_in, null)
        val username = form.findViewById<EditText>(R.id.username)
        val password = form.findViewById<EditText>(R.id.password)
        val dialog = AlertDialog.Builder(this)
            .setView(form)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Sign in", null)
            .create()

        dialog.show()
        // 单独设置点击事件，输入为空时保留窗口供用户修改。
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            when {
                username.text.isBlank() -> username.error = "请输入用户名"
                password.text.isBlank() -> password.error = "请输入密码"
                else -> {
                    Toast.makeText(this, "已提交演示表单", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
    }
}

open class LabActivity : ComponentActivity() {
    protected fun showLayout(layoutId: Int) {
        enableEdgeToEdge()
        setContentView(layoutId)

        // 把系统栏占用的空间留在内容容器外侧。
        val content = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(content)
    }
}
