package com.example.data.model

enum class DhikrCategory(val title: String) {
    MORNING("أذكار الصباح"),
    EVENING("أذكار المساء"),
    SLEEP("أذكار النوم"),
    GENERAL("فضائل وأعمال عامة")
}

data class DhikrItem(
    val id: Int,
    val text: String,
    val fadl: String? = null,
    val defaultCount: Int = 1,
    val categories: List<DhikrCategory> = listOf(DhikrCategory.GENERAL)
)

data class CustomWird(
    val id: String,
    val name: String,
    val itemIdsWithCounts: List<Pair<Int, Int>>, // Pair(dhikrId, targetCount)
    val createdAt: Long = System.currentTimeMillis()
)

data class StreakInfo(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val freezeBalance: Int = 2,
    val lastCompletedDate: String = "",
    val completedDates: Set<String> = emptySet(),
    val freezeUsedDates: Set<String> = emptySet()
)

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val progress: Float = 0f,
    val progressLabel: String = ""
)

enum class AppColorTheme(val displayName: String) {
    EMERALD("الزمردي الإسلامي"),
    SAPPHIRE("الياقوتي الأزرق"),
    AMBER("العنبري الذهبي"),
    PURPLE("الأرجواني الملكي")
}

enum class AppThemeMode(val displayName: String) {
    SYSTEM("تلقائي (حسب النظام)"),
    LIGHT("الوضع الفاتح"),
    DARK("الوضع الداكن")
}

data class UserPreferences(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val colorTheme: AppColorTheme = AppColorTheme.EMERALD,
    val fontSizeScale: Float = 1.0f, // 0.85f (صغير), 1.0f (متوسط), 1.2f (كبير), 1.4f (كبير جداً)
    val vibrateOnCount: Boolean = true,
    val vibrateOnComplete: Boolean = true,
    val soundEnabled: Boolean = false
)

data class SmartInsight(
    val title: String,
    val message: String,
    val recommendation: String,
    val completionPercentage: Int,
    val recommendedTime: String
)
