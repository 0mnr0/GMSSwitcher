import android.content.Context
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class BooleanPreference(
    val context: Context,
    private val key: String,
    private val defaultValue: Boolean = false
) : ReadWriteProperty<Any?, Boolean> {

    private val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
}
