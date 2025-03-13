package com.dsvl.gmsswitcher

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsvl.gmsswitcher.ui.theme.GMSSwitcherTheme
import java.io.File


var AppName = ""
var AppPackage = ""
var AppIcon: Drawable? = null
var filesList = mutableListOf<String>()
var filesData = mutableListOf<String>()

class PreferensesEditor : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        enableEdgeToEdge();

        AppName = intent.getStringExtra("AppName").toString()
        AppPackage = intent.getStringExtra("AppPackage").toString()
        AppIcon = DrawableHolder.icon

        setContent {
            GMSSwitcherTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyScreen()
                    Greeting2(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )


                    Log.d("PreferencesList", getAllSharedPreferencesWithRoot(AppPackage).toString())
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.secondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.secondary
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
            CreateAppInfo(AppIcon, AppName, AppPackage)
        }
    }
}


@Composable
fun CreateAppInfo(icon: Drawable?, name: String, packageName: String) { // Код для отображения информации о приложении
    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(10.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        if (icon != null) {
            AppIcon(icon)
        }
        Column (
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(text = name, fontWeight = FontWeight.W600, fontSize = 20.sp)
            Text(text = packageName)
        }
    }
}


fun getAllSharedPreferencesWithRoot(packageName: String) {
    try {
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "ls /data/data/$packageName/shared_prefs/"))
        val reader = process.inputStream.bufferedReader().readText()
        val files = reader.split("\n")


        files.forEach { fileName ->
            if (fileName.endsWith(".xml")) {
                filesList.add(fileName)
                val filePath = "/data/data/$packageName/shared_prefs/$fileName"
                val catProcess = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat $filePath"))
                val content = catProcess.inputStream.bufferedReader().readText()
                filesData.add(content)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }



}




@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    GMSSwitcherTheme {
        Greeting2("Android")
    }
}