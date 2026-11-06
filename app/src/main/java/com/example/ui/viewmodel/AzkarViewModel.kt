package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AzkarDataSource
import com.example.data.AzkarRepository
import com.example.data.model.AppColorTheme
import com.example.data.model.AppThemeMode
import com.example.data.model.Badge
import com.example.data.model.CustomWird
import com.example.data.model.DhikrCategory
import com.example.data.model.DhikrItem
import com.example.data.model.SmartInsight
import com.example.data.model.StreakInfo
import com.example.data.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class ScreenDestination {
    object Home : ScreenDestination()
    data class DhikrList(val initialCategory: DhikrCategory? = null) : ScreenDestination()
    object CustomWirds : ScreenDestination()
    object Achievements : ScreenDestination()
    object Settings : ScreenDestination()
    data class Reader(
        val title: String,
        val items: List<Pair<DhikrItem, Int>>, // item and target count
        val initialIndex: Int = 0
    ) : ScreenDestination()
}

class AzkarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AzkarRepository(application.applicationContext)
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // UI Navigation State
    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Home)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Navigation history for BackHandler
    private val screenStack = mutableListOf<ScreenDestination>(ScreenDestination.Home)

    // Data flows from repository
    val userPreferences = repository.userPreferences
    val streakInfo = repository.streakInfo
    val customWirds = repository.customWirds
    val dailyCounts = repository.dailyCounts
    val totalCompletedTasbeeh = repository.totalCompletedTasbeeh

    // All approved 28 items
    val allItems: List<DhikrItem> = AzkarDataSource.items

    // Current Reader State (when in Reader destination)
    private val _readerIndex = MutableStateFlow(0)
    val readerIndex: StateFlow<Int> = _readerIndex.asStateFlow()

    private val _readerItems = MutableStateFlow<List<Pair<DhikrItem, Int>>>(emptyList())
    val readerItems: StateFlow<List<Pair<DhikrItem, Int>>> = _readerItems.asStateFlow()

    private val _readerTitle = MutableStateFlow("")
    val readerTitle: StateFlow<String> = _readerTitle.asStateFlow()

    // Smart section based on time of day
    val currentRecommendedCategory: StateFlow<DhikrCategory> = MutableStateFlow(detectCurrentCategory())

    // Badges computation
    val badges: StateFlow<List<Badge>> = combine(
        streakInfo,
        totalCompletedTasbeeh,
        customWirds
    ) { streak, totalTasbeeh, wirds ->
        computeBadges(streak, totalTasbeeh, wirds)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Section Progress: Pair of (completed, total)
    val morningProgress: StateFlow<Pair<Int, Int>> = dailyCounts.combine(MutableStateFlow(Unit)) { counts, _ ->
        computeCategoryProgress(DhikrCategory.MORNING, counts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0, 1))

    val eveningProgress: StateFlow<Pair<Int, Int>> = dailyCounts.combine(MutableStateFlow(Unit)) { counts, _ ->
        computeCategoryProgress(DhikrCategory.EVENING, counts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0, 1))

    val sleepProgress: StateFlow<Pair<Int, Int>> = dailyCounts.combine(MutableStateFlow(Unit)) { counts, _ ->
        computeCategoryProgress(DhikrCategory.SLEEP, counts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0, 1))

    fun navigateTo(destination: ScreenDestination) {
        if (_currentScreen.value != destination) {
            screenStack.add(destination)
            _currentScreen.value = destination
        }
    }

    fun navigateBack(): Boolean {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.size - 1)
            _currentScreen.value = screenStack.last()
            return true
        } else if (_currentScreen.value !is ScreenDestination.Home) {
            _currentScreen.value = ScreenDestination.Home
            screenStack.clear()
            screenStack.add(ScreenDestination.Home)
            return true
        }
        return false
    }

    private fun detectCurrentCategory(): DhikrCategory {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 4..11 -> DhikrCategory.MORNING
            hour in 12..19 -> DhikrCategory.EVENING
            else -> DhikrCategory.SLEEP
        }
    }

    private fun computeCategoryProgress(category: DhikrCategory, counts: Map<Int, Int>): Pair<Int, Int> {
        val categoryItems = AzkarDataSource.getByCategory(category)
        if (categoryItems.isEmpty()) return Pair(0, 1)
        var completed = 0
        categoryItems.forEach { item ->
            val count = counts[item.id] ?: 0
            if (count >= item.defaultCount) {
                completed++
            }
        }
        return Pair(completed, categoryItems.size)
    }

    private fun computeBadges(
        streak: StreakInfo,
        totalTasbeeh: Int,
        wirds: List<CustomWird>
    ): List<Badge> {
        return listOf(
            Badge(
                id = "b1",
                title = "بداية النور",
                description = "إتمام أول ذكر وتسبيحة في التطبيق",
                iconName = "sparkles",
                isUnlocked = totalTasbeeh >= 1,
                progress = if (totalTasbeeh >= 1) 1f else 0f,
                progressLabel = if (totalTasbeeh >= 1) "مكتمل" else "0/1"
            ),
            Badge(
                id = "b2",
                title = "حماس البداية",
                description = "الحفاظ على 3 أيام متتالية من الأذكار",
                iconName = "fire",
                isUnlocked = streak.currentStreak >= 3 || streak.longestStreak >= 3,
                progress = minOf(1f, maxOf(streak.currentStreak, streak.longestStreak) / 3f),
                progressLabel = "${minOf(3, maxOf(streak.currentStreak, streak.longestStreak))}/3 أيام"
            ),
            Badge(
                id = "b3",
                title = "أسبوع البركة",
                description = "سلسلة التزام متواصلة لمدة 7 أيام",
                iconName = "stars",
                isUnlocked = streak.currentStreak >= 7 || streak.longestStreak >= 7,
                progress = minOf(1f, maxOf(streak.currentStreak, streak.longestStreak) / 7f),
                progressLabel = "${minOf(7, maxOf(streak.currentStreak, streak.longestStreak))}/7 أيام"
            ),
            Badge(
                id = "b4",
                title = "شهر الاستقامة",
                description = "سلسلة ذهبية متواصلة لمدة 30 يومًا",
                iconName = "trophy",
                isUnlocked = streak.currentStreak >= 30 || streak.longestStreak >= 30,
                progress = minOf(1f, maxOf(streak.currentStreak, streak.longestStreak) / 30f),
                progressLabel = "${minOf(30, maxOf(streak.currentStreak, streak.longestStreak))}/30 يوم"
            ),
            Badge(
                id = "b5",
                title = "صانع الأوراد",
                description = "إنشاء وتخصيص ورد يومي خاص بك",
                iconName = "playlist",
                isUnlocked = wirds.isNotEmpty(),
                progress = if (wirds.isNotEmpty()) 1f else 0f,
                progressLabel = if (wirds.isNotEmpty()) "مكتمل" else "0/1"
            ),
            Badge(
                id = "b6",
                title = "سيد التسبيح (100)",
                description = "إتمام 100 تسبيحة إجمالاً في رصيدك",
                iconName = "beads",
                isUnlocked = totalTasbeeh >= 100,
                progress = minOf(1f, totalTasbeeh / 100f),
                progressLabel = "${minOf(100, totalTasbeeh)}/100"
            ),
            Badge(
                id = "b7",
                title = "ألفية الذكر (1000)",
                description = "بلوغ 1000 تسبيحة وذكر في رصيدك الكلي",
                iconName = "crown",
                isUnlocked = totalTasbeeh >= 1000,
                progress = minOf(1f, totalTasbeeh / 1000f),
                progressLabel = "${minOf(1000, totalTasbeeh)}/1000"
            ),
            Badge(
                id = "b8",
                title = "حارس السلسلة",
                description = "امتلاك رصيد تجميد أو استخدامه لحماية السلسلة",
                iconName = "shield",
                isUnlocked = streak.freezeUsedDates.isNotEmpty() || streak.freezeBalance > 0,
                progress = 1f,
                progressLabel = "رصيد التجميد: ${streak.freezeBalance}"
            )
        )
    }

    fun startReader(title: String, items: List<Pair<DhikrItem, Int>>, startIndex: Int = 0) {
        _readerTitle.value = title
        _readerItems.value = items
        _readerIndex.value = startIndex
        navigateTo(ScreenDestination.Reader(title, items, startIndex))
    }

    fun incrementDhikr(dhikrId: Int, targetCount: Int) {
        val newCount = repository.incrementDhikrCount(dhikrId)
        val prefs = userPreferences.value

        if (newCount == targetCount && prefs.vibrateOnComplete) {
            triggerCompletionHaptic()
        } else if (prefs.vibrateOnCount) {
            triggerTapHaptic()
        }
    }

    fun resetDhikr(dhikrId: Int) {
        repository.resetDhikrCount(dhikrId)
    }

    fun nextReaderItem() {
        val current = _readerIndex.value
        val items = _readerItems.value
        if (current < items.size - 1) {
            _readerIndex.value = current + 1
        }
    }

    fun prevReaderItem() {
        val current = _readerIndex.value
        if (current > 0) {
            _readerIndex.value = current - 1
        }
    }

    private fun triggerTapHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25)
            }
        } catch (_: Exception) {}
    }

    private fun triggerCompletionHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 50, 70, 70)
                val amplitudes = intArrayOf(0, 255, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (_: Exception) {}
    }

    fun saveUserPreferences(prefs: UserPreferences) {
        repository.savePreferences(prefs)
    }

    fun updateThemeMode(mode: AppThemeMode) {
        saveUserPreferences(userPreferences.value.copy(themeMode = mode))
    }

    fun updateColorTheme(color: AppColorTheme) {
        saveUserPreferences(userPreferences.value.copy(colorTheme = color))
    }

    fun updateFontSize(scale: Float) {
        saveUserPreferences(userPreferences.value.copy(fontSizeScale = scale))
    }

    fun toggleVibrateCount(enabled: Boolean) {
        saveUserPreferences(userPreferences.value.copy(vibrateOnCount = enabled))
    }

    fun toggleVibrateComplete(enabled: Boolean) {
        saveUserPreferences(userPreferences.value.copy(vibrateOnComplete = enabled))
    }

    fun createCustomWird(name: String, items: List<Pair<Int, Int>>) {
        repository.addCustomWird(name, items)
    }

    fun updateCustomWird(id: String, name: String, items: List<Pair<Int, Int>>) {
        repository.updateCustomWird(id, name, items)
    }

    fun deleteCustomWird(id: String) {
        repository.deleteCustomWird(id)
    }

    /**
     * الذكاء الاصطناعي التنظيمي والمحفز (Section 4):
     * تحليل تنظيمي للعادات والأوقات ونقاط القوة والتحفيز الذكي دون توليد نصوص أذكار.
     */
    fun getSmartInsight(): SmartInsight {
        val streak = streakInfo.value
        val (mComp, mTot) = morningProgress.value
        val (eComp, eTot) = eveningProgress.value
        val (sComp, sTot) = sleepProgress.value

        val totalSections = mTot + eTot + sTot
        val completedSections = mComp + eComp + sComp
        val pct = if (totalSections > 0) ((completedSections.toFloat() / totalSections) * 100).toInt() else 0

        val currentCat = detectCurrentCategory()
        val recommendedTime = when (currentCat) {
            DhikrCategory.MORNING -> "بين صلاة الفجر وطلوع الشمس (6:00 ص - 7:30 ص)"
            DhikrCategory.EVENING -> "بين صلاة العصر والمغرب (4:30 م - 6:00 م)"
            DhikrCategory.SLEEP -> "قبل النوم مباشرة (10:00 م - 11:30 م)"
            else -> "في أي وقت من اليوم"
        }

        val title: String
        val msg: String
        val recommendation: String

        when {
            streak.currentStreak >= 7 -> {
                title = "أداء استثنائي ومداومة ممتازة!"
                msg = "أنت تحافظ على وتيرة مباركة بسلسلة قدرها ${streak.currentStreak} أيام. استمرارك يمنحك رصيد تجميد لحماية سلسلتك."
                recommendation = "ننصحك بمتابعة الورد في وقتك المفضل $recommendedTime لتثبيت العادة في نفس الساعة."
            }
            streak.currentStreak >= 3 -> {
                title = "خطوات واثقة نحو الثبات"
                msg = "لديك سلسلة نشطة من ${streak.currentStreak} أيام متتالية! بقي قليل للوصول لشارة أسبوع البركة."
                recommendation = "خصّص 5 دقائق هادئة بعد الفجر أو العصر لتلاوة أذكار القسم الحالي."
            }
            pct > 0 -> {
                title = "بداية موفقة لليوم"
                msg = "لقد أنجزت $pct% من أذكار اليوم. المداومة على قليل مستمر خير وأعظم أثراً."
                recommendation = "استكمل أذكار ${currentCat.title} للحفاظ على نشاط سلسلتك اليومية."
            }
            else -> {
                title = "جدّد همتك وابدأ وردك"
                msg = "لم تسجل أذكار اليوم بعد. رصيد التجميد المتبقي لديك: ${streak.freezeBalance} يوم لحماية السلسلة عند الطوارئ."
                recommendation = "الأنسب لك الآن البدء بقسم «${currentCat.title}» - لن يستغرق منك سوى بضع دقائق."
            }
        }

        return SmartInsight(
            title = title,
            message = msg,
            recommendation = recommendation,
            completionPercentage = pct,
            recommendedTime = recommendedTime
        )
    }
}
