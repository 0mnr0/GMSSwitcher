package com.dsvl.gmsswitcher

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.dsvl.gmsswitcher.ui.theme.GMSSwitcherTheme


var AppName = ""
var AppPackage = ""
var AppIcon: Bitmap? = null

class PreferensesEditor : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        enableEdgeToEdge();

        AppName = intent.getStringExtra("AppName").toString()
        AppPackage = intent.getStringExtra("AppPackage").toString()

        try { AppIcon = intent.getStringExtra("AppIcon") as Bitmap? } catch (e: Exception) {Log.w("PreferencesEditor", "Не удалось получить иконку приложения")}

        Log.d("PreferencesEditor", "Получено: name = $AppName, age = $AppPackage")

        setContent {
            GMSSwitcherTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyScreen()
                    Greeting2(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

//

@Composable
fun Greeting2(name: String, modifier: Modifier = Modifier) {}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScreen() {
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppName, fontWeight = FontWeight.W600) },
                navigationIcon = {
                    IconButton(onClick = { backPressedDispatcher?.onBackPressed() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer, // Светло-серый цвет
                    titleContentColor = MaterialTheme.colorScheme.secondary, // Цвет текста
                    navigationIconContentColor = MaterialTheme.colorScheme.secondary // Цвет иконки "Назад"
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Основной контент экрана")
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    GMSSwitcherTheme {
        Greeting2("Android")
    }
}