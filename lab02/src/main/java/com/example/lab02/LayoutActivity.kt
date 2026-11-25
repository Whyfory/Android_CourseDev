package com.example.lab02

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class LayoutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = intent.getIntExtra("layout", R.layout.example_linear)
        showLayout(layout)

        when (layout) {
            R.layout.example_calculator -> setupCalculator()
            R.layout.example_flight -> setupFlight()
        }
    }

    private fun setupCalculator() {
        val input = findViewById<TextView>(R.id.input)
        val result = findViewById<TextView>(R.id.result)
        var number = ""
        var left = 0.0
        var operator = ""
        val numberKeys = listOf(
            R.id.key_0, R.id.key_1, R.id.key_2, R.id.key_3, R.id.key_4,
            R.id.key_5, R.id.key_6, R.id.key_7, R.id.key_8, R.id.key_9, R.id.key_dot
        )

        for (id in numberKeys) {
            val button = findViewById<Button>(id)
            button.setOnClickListener {
                val key = button.text.toString()
                if (key != "." || "." !in number) {
                    number += key
                    input.text = if (operator.isEmpty()) number else "$left $operator $number"
                }
            }
        }

        // 先保存左操作数与运算符，按等号时再读取右操作数。
        val operators = listOf(R.id.key_add, R.id.key_sub, R.id.key_mul, R.id.key_div)
        for (id in operators) {
            val button = findViewById<Button>(id)
            button.setOnClickListener {
                val value = number.toDoubleOrNull()
                if (value != null) {
                    left = value
                    operator = button.text.toString()
                    number = ""
                    input.text = "$left $operator"
                }
            }
        }
        findViewById<Button>(R.id.key_equals).setOnClickListener {
            val right = number.toDoubleOrNull()
            if (operator.isEmpty() || right == null) return@setOnClickListener
            val answer = when (operator) {
                "+" -> left + right
                "−" -> left - right
                "×" -> left * right
                else -> left / right
            }
            result.text = if (answer.isFinite()) answer.toString() else "计算错误"
            number = ""
            operator = ""
        }
    }

    private fun setupFlight() {
        val origin = findViewById<TextView>(R.id.origin)
        val destination = findViewById<TextView>(R.id.destination)
        val oneWay = findViewById<Switch>(R.id.one_way)
        val swap = findViewById<ImageButton>(R.id.swap)

        swap.setOnClickListener {
            val previousOrigin = origin.text
            origin.text = destination.text
            destination.text = previousOrigin
        }

        oneWay.setOnCheckedChangeListener { _, checked ->
            oneWay.text = if (checked) "One Way" else "Round Trip"
            swap.setImageResource(if (checked) R.drawable.single_arrow else R.drawable.double_arrows)
        }
        findViewById<Button>(R.id.depart).setOnClickListener {
            Toast.makeText(this, "${origin.text} → ${destination.text} · ${oneWay.text}", Toast.LENGTH_SHORT).show()
        }
    }
}
