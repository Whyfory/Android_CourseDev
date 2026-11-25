package com.example.lab03

// 两种列表共用同一份动物名称与图片资源。
internal data class Animal(val name: String, val imageId: Int)

internal val animals = listOf(
    Animal("Lion", R.drawable.lion),
    Animal("Tiger", R.drawable.tiger),
    Animal("Monkey", R.drawable.monkey),
    Animal("Dog", R.drawable.dog),
    Animal("Cat", R.drawable.cat),
    Animal("Elephant", R.drawable.elephant)
)
