package com.dsvl.gmsswitcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.graphics.drawable.toBitmap
import com.dsvl.gmsswitcher.ui.theme.GMSSwitcherTheme

var context: MainActivity? = null;
var readyAppList: MutableList<AppInfo>? = null;

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        context = this



        setContent {
            GMSSwitcherTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                    BottomBar()
                }
            }
        }
    }
}

@Composable
fun BottomBar() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val items = listOf(
        NavigationItem("Список приложений", Icons.Default.Home),
        NavigationItem("Настройки", Icons.Default.Settings)
    )
    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (selectedIndex == 0) {
                val context = LocalContext.current
                val appList by remember { mutableStateOf(getInstalledAppsWithRoot(context)) }
                DisplayAppList(appList)
            }
        }
    }
}
data class NavigationItem(val title: String, val icon: ImageVector)


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable
)

fun getInstalledAppsWithRoot(context: Context, showSystem: Boolean = false): List<AppInfo> {
    var ReadSystemAppKey = ""
    if (showSystem) {ReadSystemAppKey = "-f"}
    val apps = mutableListOf<AppInfo>()
    if (readyAppList != null) {
        return readyAppList!!
    }

    try {
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "pm list packages -f"))
        val reader = process.inputStream.bufferedReader()
        val packageManager = context.packageManager

        reader.forEachLine { line ->
            val parts = line.split("=")
            if (parts.size == 2) {
                val packageName = parts[1]
                try {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    val appIcon = packageManager.getApplicationIcon(appInfo)
                    apps.add(AppInfo(appName, packageName, appIcon))
                } catch (e: PackageManager.NameNotFoundException) {
                    // Приложение не найдено, игнорируем
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    readyAppList = apps
    return apps
}

@Composable
fun DisplayAppList(apps: List<AppInfo>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(apps) { app ->
            ExpandableCard(app)
        }
    }
}

@Composable
fun ExpandableCard(app: AppInfo) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                AppIcon(app.icon)
                Spacer(modifier = Modifier.width(8.dp))
                Text(app.name, style = MaterialTheme.typography.bodyLarge)
            }
            val context = LocalContext.current
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Text("Пакет: ${app.packageName}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val intent = Intent(context, PreferensesEditor::class.java)
                        intent.putExtra("KEY_NAME", "значение")
                        intent.putExtra("KEY_AGE", 25);
                        val options = ActivityOptionsCompat.makeCustomAnimation(
                            context, R.anim.slide_in_right, R.anim.slide_out_left
                        )
                        context.startActivity(intent, options.toBundle())
                    }) {
                        Text("Редактировать")
                    }
                }
            }
        }
    }
}


@Composable
fun AppIcon(drawable: Drawable) {
    val bitmap = remember(drawable) { drawable.toBitmap() }
    Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.size(48.dp))
}

// Функция для конвертации Drawable в Bitmap
fun Drawable.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GMSSwitcherTheme {
        BottomBar()
    }
}
