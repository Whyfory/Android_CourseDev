package com.example.android_dev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.android_dev.ui.theme.Android_DevTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Activity 创建时，用 setContent 设置 Compose 界面。
        setContent {
            Android_DevTheme {
                Scaffold { innerPadding ->
                    // 使用 Scaffold 提供的内边距，避开状态栏和导航栏。
                    Column(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                    ) {
                        Text(stringResource(R.string.welcome), style = MaterialTheme.typography.headlineMedium)
                        Text(stringResource(R.string.description), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
