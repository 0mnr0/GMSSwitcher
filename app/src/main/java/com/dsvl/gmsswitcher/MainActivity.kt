package com.dsvl.gmsswitcher

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.shape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityOptionsCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import com.dsvl.gmsswitcher.ui.theme.GMSSwitcherTheme

var context: MainActivity? = null
var readyAppList: MutableList<AppInfo>? = null
var ConstantlyAllApps: MutableList<AppInfo>? = null
val settingsKeys = listOf("ShowSystemApps", "IgnoreDamagedApps")
val settingsTitles = listOf("Показывать системные приложения", "Игнорировать приложения с неполными данными")
var SettingsChanged = false
object DrawableHolder {
    var icon: Drawable? = null
}




fun SaveBool(key: String, value: Boolean) {
    val prefs = context?.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    prefs?.edit()?.putBoolean(key, value)?.apply()
}

fun GetBool(key: String): Boolean {
    val prefs = context?.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    return prefs?.getBoolean(key, false) ?: false
}



class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        context = this



        setContent {
            GMSSwitcherTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
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
                getInstalledAppsWithRoot(context)
                SearchScreen()
                //DisplayAppList(appList)
            }
            if (selectedIndex == 1) {
                SettingsScreen()
            }

        }
    }
}
data class NavigationItem(val title: String, val icon: ImageVector)




data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable
)

fun getInstalledAppsWithRoot(context: Context): List<AppInfo> {
    val apps = mutableListOf<AppInfo>()
    if (readyAppList != null || SettingsChanged) {
        if (SettingsChanged) {
            SettingsChanged = false
        } else {
            return readyAppList!!
        }
    }
    val ShowSystemApps = GetBool("ShowSystemApps")

    try {
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        val userApps = packages.filter {
            (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0
        }

        userApps.forEach {
            val packageName = it.packageName
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val appIcon = packageManager.getApplicationIcon(appInfo)
            apps.add(AppInfo(appName, packageName, appIcon))
        }
        if (!ShowSystemApps) {
            ConstantlyAllApps = apps
            return apps.sortedBy { it.name }
        }

        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "pm list packages -f"))
        val reader = process.inputStream.bufferedReader()

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
    ConstantlyAllApps = apps
    readyAppList = apps
    return apps.sortedBy { it.name }
}


fun filterApps(apps: MutableList<AppInfo>?, filteredString: String): List<AppInfo> {
    return apps?.filter { it.name.contains(filteredString, ignoreCase = true) } ?: emptyList()
}

@Composable
fun SearchScreen() {
    var searchText by remember { mutableStateOf("") }

    // Пересчёт списка при изменении searchText
    val filteredApps by remember(searchText) {
        mutableStateOf(filterApps(ConstantlyAllApps, searchText))
    }

    Column {
        OutlinedTextField(
            modifier = Modifier.padding(6.dp).clip(RoundedCornerShape(12.dp)).fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Поиск", modifier = Modifier.padding(0.dp))
            },
            value = searchText,
            onValueChange = { newText ->
                searchText = newText
            },
            label = { Text("Поиск приложений") }
        )
        DisplayAppList(filteredApps)
    }
}









@Composable
fun DisplayAppList(apps: List<AppInfo>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = apps, key = { it.packageName }) { app ->
            var expanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .clickable {
                        expanded = !expanded
                    },
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(40.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(app.icon)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(app.name, style = MaterialTheme.typography.bodyLarge)
                    }
                    val context = LocalContext.current
                    AnimatedVisibility(visible = expanded) {
                        Column {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Пакет: ${app.packageName}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val intent = Intent(context, PreferensesEditor::class.java)
                                    intent.putExtra("AppName", app.name)
                                    intent.putExtra("AppPackage", app.packageName)
                                    DrawableHolder.icon = app.icon

                                    val options = ActivityOptionsCompat.makeCustomAnimation(
                                        context, R.anim.slide_in_right, R.anim.slide_out_left
                                    )
                                    context.startActivity(intent, options.toBundle())
                                },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(2.dp, 16.dp),
                                shape = RoundedCornerShape(26.dp, 26.dp, 12.dp, 12.dp)
                            ) {
                                Text("Редактировать")
                            }
                            Spacer(Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    launchAppByPackageName(context, app.packageName)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(2.dp, 16.dp),
                                shape = RoundedCornerShape(12.dp, 12.dp, 26.dp, 26.dp)
                            ) {
                                Text("Запустить")
                            }
                        }
                    }
                }
            }
        }
    }
}



fun launchAppByPackageName(context: Context, packageName: String) {
    val pm: PackageManager = context.packageManager
    try {
        val intent: Intent? = pm.getLaunchIntentForPackage(packageName)
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}



@Composable
fun AppIcon(drawable: Drawable) {
    val bitmap = remember(drawable) { drawable.toBitmap() }
    Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.size(48.dp))
}

// Функция для конвертации Drawable в Bitmap
fun Drawable.toBitmap(): Bitmap {
    val bitmap = createBitmap(intrinsicWidth, intrinsicHeight)
    val canvas = android.graphics.Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}


@Composable
fun CreateSettingLine(key: String, title: String, settingValue: Boolean) {
    var switchState by remember { mutableStateOf(settingValue) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.secondaryContainer) // Задаем нужный фон (например, светло-серый)
    ) {
        Text(
            text = title,
            modifier = Modifier
                .weight(1f) // Ширина и цвет текста
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(start = 8.dp, end = 8.dp) // отступы внутри текстового элемента
        )
        Switch(
            checked = switchState,
            onCheckedChange = { newValue ->
                SettingsChanged = true
                switchState = newValue
                SaveBool(key, newValue)
            },
            modifier = Modifier.padding(8.dp) // отступы для переключателя
        )
    }

}




@Composable
fun SettingsScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp), // Отступ сверху
        verticalArrangement = Arrangement.Top // Размещает элементы вверху
    ) {
        Text(
            text = "Настройки",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp, bottom = 12.dp), // Отступ сверху
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 36.sp,
            fontWeight = FontWeight.W600,
        )

        settingsKeys.forEachIndexed { index, key ->
            CreateSettingLine(key, settingsTitles[index], GetBool(key))
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GMSSwitcherTheme {
        BottomBar()
    }
}
