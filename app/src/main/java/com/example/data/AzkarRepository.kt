package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppColorTheme
import com.example.data.model.AppThemeMode
import com.example.data.model.Badge
import com.example.data.model.CustomWird
import com.example.data.model.StreakInfo
import com.example.data.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AzkarRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("azkar_app_prefs", Context.MODE_PRIVATE)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Flow states
    private val _userPreferences = MutableStateFlow(loadPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    private val _streakInfo = MutableStateFlow(loadStreakInfo())
    val streakInfo: StateFlow<StreakInfo> = _streakInfo.asStateFlow()

    private val _customWirds = MutableStateFlow(loadCustomWirds())
    val customWirds: StateFlow<List<CustomWird>> = _customWirds.asStateFlow()

    private val _dailyCounts = MutableStateFlow(loadDailyCounts())
    val dailyCounts: StateFlow<Map<Int, Int>> = _dailyCounts.asStateFlow()

    private val _totalCompletedTasbeeh = MutableStateFlow(prefs.getInt("total_tasbeeh", 0))
    val totalCompletedTasbeeh: StateFlow<Int> = _totalCompletedTasbeeh.asStateFlow()

    init {
        checkDailyStreakMaintenance()
    }

    fun getTodayDateString(): String = dateFormat.format(Date())

    private fun getYesterdayDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(cal.time)
    }

    private fun loadPreferences(): UserPreferences {
        val modeStr = prefs.getString("pref_theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val colorStr = prefs.getString("pref_color_theme", AppColorTheme.EMERALD.name) ?: AppColorTheme.EMERALD.name
        val fontScale = prefs.getFloat("pref_font_scale", 1.0f)
        val vibCount = prefs.getBoolean("pref_vib_count", true)
        val vibComplete = prefs.getBoolean("pref_vib_complete", true)
        val sound = prefs.getBoolean("pref_sound", false)

        return UserPreferences(
            themeMode = try { AppThemeMode.valueOf(modeStr) } catch (_: Exception) { AppThemeMode.SYSTEM },
            colorTheme = try { AppColorTheme.valueOf(colorStr) } catch (_: Exception) { AppColorTheme.EMERALD },
            fontSizeScale = fontScale,
            vibrateOnCount = vibCount,
            vibrateOnComplete = vibComplete,
            soundEnabled = sound
        )
    }

    fun savePreferences(preferences: UserPreferences) {
        prefs.edit()
            .putString("pref_theme_mode", preferences.themeMode.name)
            .putString("pref_color_theme", preferences.colorTheme.name)
            .putFloat("pref_font_scale", preferences.fontSizeScale)
            .putBoolean("pref_vib_count", preferences.vibrateOnCount)
            .putBoolean("pref_vib_complete", preferences.vibrateOnComplete)
            .putBoolean("pref_sound", preferences.soundEnabled)
            .apply()
        _userPreferences.value = preferences
    }

    private fun loadStreakInfo(): StreakInfo {
        val current = prefs.getInt("streak_current", 0)
        val longest = prefs.getInt("streak_longest", 0)
        val freeze = prefs.getInt("streak_freeze", 2)
        val lastDate = prefs.getString("streak_last_date", "") ?: ""
        val completedSet = prefs.getStringSet("streak_completed_dates", emptySet()) ?: emptySet()
        val freezeSet = prefs.getStringSet("streak_freeze_dates", emptySet()) ?: emptySet()

        return StreakInfo(
            currentStreak = current,
            longestStreak = longest,
            freezeBalance = freeze,
            lastCompletedDate = lastDate,
            completedDates = completedSet,
            freezeUsedDates = freezeSet
        )
    }

    /**
     * Checks if days were skipped and handles automatic streak freeze protection.
     */
    fun checkDailyStreakMaintenance() {
        val currentStreak = prefs.getInt("streak_current", 0)
        if (currentStreak <= 0) return

        val lastDate = prefs.getString("streak_last_date", "") ?: ""
        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()

        if (lastDate == today || lastDate == yesterday) {
            // All good, streak is healthy!
            return
        }

        // More than 1 day missed! Let's check freeze
        val freeze = prefs.getInt("streak_freeze", 0)
        if (freeze > 0) {
            // Protect streak using 1 freeze day!
            val newFreeze = freeze - 1
            val freezeSet = (prefs.getStringSet("streak_freeze_dates", emptySet()) ?: emptySet()).toMutableSet()
            freezeSet.add(yesterday)

            prefs.edit()
                .putInt("streak_freeze", newFreeze)
                .putString("streak_last_date", yesterday) // extend grace
                .putStringSet("streak_freeze_dates", freezeSet)
                .apply()

            _streakInfo.value = loadStreakInfo()
        } else {
            // Streak broken, reset to 0
            prefs.edit().putInt("streak_current", 0).apply()
            _streakInfo.value = loadStreakInfo()
        }
    }

    private fun loadDailyCounts(): Map<Int, Int> {
        val today = getTodayDateString()
        val savedDate = prefs.getString("counts_date", "") ?: ""
        if (savedDate != today) {
            // Reset daily counts for a new day
            prefs.edit().putString("counts_date", today).remove("counts_json").apply()
            return emptyMap()
        }
        val json = prefs.getString("counts_json", "{}") ?: "{}"
        val map = mutableMapOf<Int, Int>()
        try {
            val obj = JSONObject(json)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key.toInt()] = obj.getInt(key)
            }
        } catch (_: Exception) {}
        return map
    }

    fun incrementDhikrCount(dhikrId: Int): Int {
        val today = getTodayDateString()
        val currentCounts = _dailyCounts.value.toMutableMap()
        val newCount = (currentCounts[dhikrId] ?: 0) + 1
        currentCounts[dhikrId] = newCount
        _dailyCounts.value = currentCounts

        val obj = JSONObject()
        currentCounts.forEach { (k, v) -> obj.put(k.toString(), v) }
        prefs.edit()
            .putString("counts_date", today)
            .putString("counts_json", obj.toString())
            .apply()

        // Increment total tasbeeh
        val newTotal = _totalCompletedTasbeeh.value + 1
        _totalCompletedTasbeeh.value = newTotal
        prefs.edit().putInt("total_tasbeeh", newTotal).apply()

        // Mark today completed in streak if reasonable activity is done (e.g., at least 3 tasbeeh)
        if (newCount >= 1) {
            recordDailyActivity()
        }

        return newCount
    }

    fun resetDhikrCount(dhikrId: Int) {
        val currentCounts = _dailyCounts.value.toMutableMap()
        currentCounts.remove(dhikrId)
        _dailyCounts.value = currentCounts

        val obj = JSONObject()
        currentCounts.forEach { (k, v) -> obj.put(k.toString(), v) }
        prefs.edit().putString("counts_json", obj.toString()).apply()
    }

    private fun recordDailyActivity() {
        val today = getTodayDateString()
        val info = _streakInfo.value
        if (info.lastCompletedDate == today) {
            // Already counted today
            return
        }

        val yesterday = getYesterdayDateString()
        var newStreak = info.currentStreak
        if (info.lastCompletedDate == yesterday || info.currentStreak == 0) {
            newStreak += 1
        } else {
            newStreak = 1
        }

        val longest = maxOf(newStreak, info.longestStreak)
        val completedSet = info.completedDates.toMutableSet()
        completedSet.add(today)

        // Award extra freeze reward when hitting milestones (every 7 days streak up to max 3)
        var newFreeze = info.freezeBalance
        if (newStreak % 7 == 0 && newFreeze < 3) {
            newFreeze = minOf(3, newFreeze + 1)
        }

        prefs.edit()
            .putInt("streak_current", newStreak)
            .putInt("streak_longest", longest)
            .putInt("streak_freeze", newFreeze)
            .putString("streak_last_date", today)
            .putStringSet("streak_completed_dates", completedSet)
            .apply()

        _streakInfo.value = StreakInfo(
            currentStreak = newStreak,
            longestStreak = longest,
            freezeBalance = newFreeze,
            lastCompletedDate = today,
            completedDates = completedSet,
            freezeUsedDates = info.freezeUsedDates
        )
    }

    private fun loadCustomWirds(): List<CustomWird> {
        val json = prefs.getString("custom_wirds_json", "[]") ?: "[]"
        val list = mutableListOf<CustomWird>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val itemsArray = obj.getJSONArray("items")
                val items = mutableListOf<Pair<Int, Int>>()
                for (j in 0 until itemsArray.length()) {
                    val itemObj = itemsArray.getJSONObject(j)
                    items.add(Pair(itemObj.getInt("id"), itemObj.getInt("count")))
                }
                list.add(CustomWird(id, name, items, obj.optLong("createdAt", System.currentTimeMillis())))
            }
        } catch (_: Exception) {}

        if (list.isEmpty()) {
            // Provide an initial sample custom wird: ورد الاستغفار والحمد
            val defaultWird = CustomWird(
                id = "default_wird_1",
                name = "وردي اليومي المبارك",
                itemIdsWithCounts = listOf(
                    Pair(1, 33), // سبحان الله وبحمده سبحان الله العظيم
                    Pair(2, 10), // سبحان الله والحمد لله ولا اله الا الله
                    Pair(5, 1),  // سيد الاستغفار
                    Pair(9, 10)  // الصلاة على النبي
                )
            )
            list.add(defaultWird)
            saveCustomWirds(list)
        }

        return list
    }

    fun saveCustomWirds(wirds: List<CustomWird>) {
        try {
            val array = JSONArray()
            wirds.forEach { wird ->
                val obj = JSONObject()
                obj.put("id", wird.id)
                obj.put("name", wird.name)
                obj.put("createdAt", wird.createdAt)
                val itemsArr = JSONArray()
                wird.itemIdsWithCounts.forEach { (dhikrId, count) ->
                    val itemObj = JSONObject()
                    itemObj.put("id", dhikrId)
                    itemObj.put("count", count)
                    itemsArr.put(itemObj)
                }
                obj.put("items", itemsArr)
                array.put(obj)
            }
            prefs.edit().putString("custom_wirds_json", array.toString()).apply()
            _customWirds.value = wirds
        } catch (_: Exception) {}
    }

    fun addCustomWird(name: String, items: List<Pair<Int, Int>>) {
        val newWird = CustomWird(
            id = "wird_" + System.currentTimeMillis(),
            name = name,
            itemIdsWithCounts = items
        )
        val current = _customWirds.value.toMutableList()
        current.add(newWird)
        saveCustomWirds(current)
    }

    fun updateCustomWird(id: String, name: String, items: List<Pair<Int, Int>>) {
        val current = _customWirds.value.map {
            if (it.id == id) it.copy(name = name, itemIdsWithCounts = items) else it
        }
        saveCustomWirds(current)
    }

    fun deleteCustomWird(id: String) {
        val current = _customWirds.value.filter { it.id != id }
        saveCustomWirds(current)
    }
}
