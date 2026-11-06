package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldenAccent
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.AzkarViewModel

@Composable
fun DhikrReaderScreen(
    viewModel: AzkarViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler {
        viewModel.navigateBack()
    }

    val title by viewModel.readerTitle.collectAsState()
    val items by viewModel.readerItems.collectAsState()
    val currentIndex by viewModel.readerIndex.collectAsState()
    val dailyCounts by viewModel.dailyCounts.collectAsState()
    val preferences by viewModel.userPreferences.collectAsState()

    var localFontScale by remember { mutableFloatStateOf(preferences.fontSizeScale) }

    if (items.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("لا توجد أذكار في هذه القائمة")
        }
        return
    }

    val currentPair = items.getOrElse(currentIndex) { items.first() }
    val currentDhikr = currentPair.first
    val targetCount = currentPair.second
    val currentCount = dailyCounts[currentDhikr.id] ?: 0
    val isCompleted = currentCount >= targetCount && targetCount > 0

    val progressAnim by animateFloatAsState(
        targetValue = if (targetCount > 0) (currentCount.toFloat() / targetCount.toFloat()).coerceIn(0f, 1f) else 1f,
        label = "tasbeeh_progress"
    )

    // Overall session completion check
    val allCompleted = items.all { pair ->
        (dailyCounts[pair.first.id] ?: 0) >= pair.second
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dhikr_reader_screen")
    ) {
        // Top Navigation Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("reader_close_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "الذكر ${currentIndex + 1} من ${items.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Font Zoom Control
                    Row {
                        IconButton(
                            onClick = {
                                if (localFontScale > 0.8f) localFontScale -= 0.15f
                            }
                        ) {
                            Text("A-", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }
                        IconButton(
                            onClick = {
                                if (localFontScale < 1.6f) localFontScale += 0.15f
                            }
                        ) {
                            Text("A+", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                // Overall progress bar of current section
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / items.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Completion Banner (when all items in session are finished)
            if (allCompleted) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SuccessGreen.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "هنيئاً لك! أتممت جميع أذكار هذه الجلسة بحمد الله.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
            }

            // Main Dhikr Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reader_dhikr_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Category & Item Number Tag
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "بند معتمد #${currentDhikr.id}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Copy & Share buttons
                        Row {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val text = buildString {
                                        append(currentDhikr.text)
                                        if (!currentDhikr.fadl.isNullOrBlank()) {
                                            append("\n\nالفضل: ")
                                            append(currentDhikr.fadl)
                                        }
                                    }
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Dhikr", text))
                                    Toast.makeText(context, "تم نسخ الذكر", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "نسخ",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    val shareText = buildString {
                                        append(currentDhikr.text)
                                        if (!currentDhikr.fadl.isNullOrBlank()) {
                                            append("\n\nالفضل: ")
                                            append(currentDhikr.fadl)
                                        }
                                        append("\n\n— من تطبيق الأذكار الذكي")
                                    }
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "مشاركة الذكر"))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "مشاركة",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main Text
                    Text(
                        text = currentDhikr.text,
                        fontSize = (22 * localFontScale).sp,
                        lineHeight = (22 * localFontScale * 1.55).sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    // Virtues Section: Shown ONLY when present!
                    if (!currentDhikr.fadl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "الفضل المعتمد:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldenAccent
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentDhikr.fadl,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Big Interactive Circular Tasbeeh Counter
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        viewModel.incrementDhikr(currentDhikr.id, targetCount)
                    }
                    .testTag("reader_tasbeeh_counter_ring")
            ) {
                // Background Track Ring
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 12.dp
                )
                // Animated Progress Ring
                CircularProgressIndicator(
                    progress = { progressAnim },
                    modifier = Modifier.fillMaxSize(),
                    color = if (isCompleted) SuccessGreen else MaterialTheme.colorScheme.primary,
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round
                )
                // Center Counter Info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$currentCount",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) SuccessGreen else MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "من $targetCount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isCompleted) "أتممت الذكر ✓" else "المس للعد",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCompleted) SuccessGreen else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reset Counter for this Dhikr
            if (currentCount > 0) {
                IconButton(
                    onClick = { viewModel.resetDhikr(currentDhikr.id) },
                    modifier = Modifier.testTag("reader_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "إعادة التعيين",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Bottom Controls: Previous, Increment button, Next
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Button
                OutlinedButton(
                    onClick = { viewModel.prevReaderItem() },
                    enabled = currentIndex > 0,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("reader_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "السابق",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("السابق")
                }

                // Quick Big Tap Button
                Button(
                    onClick = { viewModel.incrementDhikr(currentDhikr.id, targetCount) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) SuccessGreen else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .height(52.dp)
                        .padding(horizontal = 8.dp)
                        .testTag("reader_bottom_increment_button")
                ) {
                    Text(
                        text = if (isCompleted) "تم ($currentCount/$targetCount)" else "تسبيح (+1)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Next Button
                OutlinedButton(
                    onClick = { viewModel.nextReaderItem() },
                    enabled = currentIndex < items.size - 1,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("reader_next_button")
                ) {
                    Text("التالي")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "التالي",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
