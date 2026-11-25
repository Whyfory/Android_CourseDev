package com.example.lab03

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecyclerActivity : LabActivity() {
    private val addedAnimals = arrayListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLayout(R.layout.activity_recycler)
        addedAnimals.addAll(savedInstanceState?.getIntegerArrayList("addedAnimals").orEmpty())

        val list = findViewById<RecyclerView>(R.id.recycler_list)
        val rows = animals.toMutableList()
        for ((index, animalIndex) in addedAnimals.withIndex()) {
            val original = animals[animalIndex]
            rows.add(0, original.copy(name = "${original.name}（新增 ${index + 1}）"))
        }
        val adapter = AnimalAdapter(rows) { name ->
            Toast.makeText(this, name, Toast.LENGTH_SHORT).show()
        }
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        findViewById<Button>(R.id.add_animal).setOnClickListener {
            val names = animals.map { it.name }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("选择要添加的动物")
                .setItems(names) { _, index ->
                    addedAnimals.add(index)
                    val original = animals[index]
                    val newAnimal = original.copy(name = "${original.name}（新增 ${addedAnimals.size}）")

                    // 先插入数据，再告诉 RecyclerView 哪一行发生了变化。
                    rows.add(0, newAnimal)
                    adapter.notifyItemInserted(0)
                    list.scrollToPosition(0)
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putIntegerArrayList("addedAnimals", addedAnimals)
        super.onSaveInstanceState(outState)
    }
}

private class AnimalAdapter(
    private val data: List<Animal>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<AnimalAdapter.AnimalHolder>() {

    // ViewHolder 保留一行的控件引用，滚动复用时可以直接更新内容。
    class AnimalHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.animal_name)
        val image: ImageView = view.findViewById(R.id.animal_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalHolder {
        val row = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_animal, parent, false)
        return AnimalHolder(row)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: AnimalHolder, position: Int) {
        val animal = data[position]
        holder.name.text = animal.name
        holder.image.setImageResource(animal.imageId)
        // 点击时使用这一轮绑定的动物，避免插入前的位置与现在的位置不同。
        holder.itemView.setOnClickListener { onClick(animal.name) }
    }
}
