package com.example.lab03

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class AnimalListActivity : LabActivity() {
    private var pendingName: String? = null

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val name = pendingName
            pendingName = null
            if (granted && name != null) {
                showNotification(name)
            } else if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showNotificationSettings()
            } else {
                Toast.makeText(this, "通知未开启，仍可点击查看动物名称", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLayout(R.layout.activity_animal_list)
        pendingName = savedInstanceState?.getString("pendingName")
        val list = findViewById<ListView>(R.id.animal_list)

        // SimpleAdapter 用键名从每一条数据中取值，再放入对应的控件。
        val rows = animals.map { mapOf("name" to it.name, "image" to it.imageId) }
        list.adapter = SimpleAdapter(
            this,
            rows,
            R.layout.item_animal,
            arrayOf("name", "image"),
            intArrayOf(R.id.animal_name, R.id.animal_image)
        )
        list.setOnItemClickListener { _, _, position, _ ->
            val name = animals[position].name
            Toast.makeText(this, name, Toast.LENGTH_SHORT).show()
            notifyAfterPermission(name)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("pendingName", pendingName)
        super.onSaveInstanceState(outState)
    }

    private fun notifyAfterPermission(name: String) {
        val permission = Manifest.permission.POST_NOTIFICATIONS

        // Android 13 起，先确认系统通知权限。权限结果返回后再发送当前条目的通知。
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                showNotification(name)
            }
            shouldShowRequestPermissionRationale(permission) -> {
                AlertDialog.Builder(this)
                    .setMessage("开启通知后，点击的动物名称会出现在系统通知栏。")
                    .setPositiveButton("继续") { _, _ -> requestNotificationPermission(name) }
                    .setNegativeButton("取消", null)
                    .show()
            }
            else -> requestNotificationPermission(name)
        }
    }

    private fun requestNotificationPermission(name: String) {
        pendingName = name
        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun showNotificationSettings() {
        AlertDialog.Builder(this)
            .setMessage("系统通知已关闭。你可以在应用设置中开启通知。")
            .setPositiveButton("打开设置") { _, _ ->
                startActivity(Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                ))
            }
            .setNegativeButton("关闭", null)
            .show()
    }

    private fun showNotification(name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager = NotificationManagerCompat.from(this)
        if (!manager.areNotificationsEnabled()) {
            showNotificationSettings()
            return
        }

        val channelId = "animal_selection"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(channelId, "动物列表", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }

        val openList = PendingIntent.getActivity(
            this, 0, Intent(this, AnimalListActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(applicationInfo.icon)
            .setContentTitle(name)
            .setContentText("你在动物列表中选择了 $name。")
            .setContentIntent(openList)
            .setAutoCancel(true)
            .build()
        try {
            manager.notify(name, 1, notification)
        } catch (_: SecurityException) {
            Toast.makeText(this, "通知权限已关闭", Toast.LENGTH_SHORT).show()
        }
    }
}
