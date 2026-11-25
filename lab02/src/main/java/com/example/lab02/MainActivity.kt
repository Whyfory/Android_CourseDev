package com.example.lab02

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLayout(R.layout.activity_main)

        openLayout(R.id.open_linear, R.layout.example_linear)
        openLayout(R.id.open_table, R.layout.example_table)
        openLayout(R.id.open_calculator, R.layout.example_calculator)
        openLayout(R.id.open_flight, R.layout.example_flight)
        findViewById<Button>(R.id.open_tasks).setOnClickListener {
            startActivity(Intent(this, TasksActivity::class.java))
        }
    }

    private fun openLayout(buttonId: Int, layoutId: Int) {
        findViewById<Button>(buttonId).setOnClickListener {
            // Intent 携带要展示的 XML 布局编号。
            startActivity(Intent(this, LayoutActivity::class.java).putExtra("layout", layoutId))
        }
    }
}

internal fun ComponentActivity.showLayout(layoutId: Int) {
    enableEdgeToEdge()
    setContentView(layoutId)

    // 给内容容器预留系统栏和屏幕开孔的空间。
    val content = findViewById<android.view.View>(android.R.id.content)
    ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
        insets
    }
    ViewCompat.requestApplyInsets(content)
}
