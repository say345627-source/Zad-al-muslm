package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AzkarDataSource
import com.example.data.model.CustomWird
import com.example.data.model.DhikrItem
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.AzkarViewModel

@Composable
fun CustomWirdScreen(
    viewModel: AzkarViewModel,
    modifier: Modifier = Modifier
) {
    val customWirds by viewModel.customWirds.collectAsState()
    val dailyCounts by viewModel.dailyCounts.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingWird by remember { mutableStateOf<CustomWird?>(null) }
    var deletingWird by remember { mutableStateOf<CustomWird?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("custom_wirds_screen")
    ) {
        if (customWirds.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "لم تقم بإنشاء أي وِرد بعد",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "اصنع وِردك الخاص الآن باختيار أذكارك المفضلة من بنود المكتبة المعتمدة وتحديد عدد تكرار كل ذكر.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إنشاء وِرد جديد")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "أورادي المخصصة",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "اختر الأذكار ورتّبها بحرية ضمن أورادك الخاصة مع تتبع مستقل للإنجاز",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(customWirds, key = { it.id }) { wird ->
                    WirdCard(
                        wird = wird,
                        dailyCounts = dailyCounts,
                        onStart = {
                            val itemsToRead = wird.itemIdsWithCounts.mapNotNull { (id, count) ->
                                AzkarDataSource.getById(id)?.let { Pair(it, count) }
                            }
                            viewModel.startReader(wird.name, itemsToRead)
                        },
                        onEdit = { editingWird = wird },
                        onDelete = { deletingWird = wird }
                    )
                }
            }
        }

        // FAB to add new Wird
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("create_wird_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "إنشاء وِرد")
        }
    }

    // Create / Edit Dialog
    if (showCreateDialog || editingWird != null) {
        WirdEditorDialog(
            existingWird = editingWird,
            onDismiss = {
                showCreateDialog = false
                editingWird = null
            },
            onSave = { name, selectedItems ->
                if (editingWird != null) {
                    viewModel.updateCustomWird(editingWird!!.id, name, selectedItems)
                } else {
                    viewModel.createCustomWird(name, selectedItems)
                }
                showCreateDialog = false
                editingWird = null
            }
        )
    }

    // Delete Confirmation Dialog
    deletingWird?.let { wird ->
        AlertDialog(
            onDismissRequest = { deletingWird = null },
            title = { Text("حذف الوِرد") },
            text = { Text("هل أنت متأكد من رغبتك في حذف وِرد «${wird.name}»؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomWird(wird.id)
                        deletingWird = null
                    }
                ) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingWird = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun WirdCard(
    wird: CustomWird,
    dailyCounts: Map<Int, Int>,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Calculate progress
    val totalItems = wird.itemIdsWithCounts.size
    var completedItems = 0
    wird.itemIdsWithCounts.forEach { (id, target) ->
        val current = dailyCounts[id] ?: 0
        if (current >= target) completedItems++
    }
    val isCompleted = completedItems >= totalItems && totalItems > 0
    val progressFloat = if (totalItems > 0) completedItems.toFloat() / totalItems.toFloat() else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wird_card_${wird.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = wird.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$totalItems أذكار معتمدة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الإنجاز اليومي: $completedItems من $totalItems",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isCompleted) {
                    Text(
                        text = "مكتمل بحمد الله ✓",
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progressFloat },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isCompleted) SuccessGreen else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onStart,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) SuccessGreen else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCompleted) "مراجعة الوِرد" else "بدء قراءة الوِرد",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WirdEditorDialog(
    existingWird: CustomWird?,
    onDismiss: () -> Unit,
    onSave: (name: String, items: List<Pair<Int, Int>>) -> Unit
) {
    var name by remember { mutableStateOf(existingWird?.name ?: "") }
    // Map of Dhikr ID to Target Count
    val selectedMap = remember {
        mutableStateMapOf<Int, Int>().apply {
            existingWird?.itemIdsWithCounts?.forEach { (id, count) ->
                put(id, count)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingWird != null) "تعديل الوِرد" else "إنشاء وِرد جديد",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الوِرد (مثال: وردي اليومي)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "اختر الأذكار وحدد عدد التكرار:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AzkarDataSource.items, key = { it.id }) { item ->
                        val isChecked = selectedMap.containsKey(item.id)
                        val currentTarget = selectedMap[item.id] ?: item.defaultCount

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) {
                                        selectedMap.remove(item.id)
                                    } else {
                                        selectedMap[item.id] = item.defaultCount
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            selectedMap[item.id] = item.defaultCount
                                        } else {
                                            selectedMap.remove(item.id)
                                        }
                                    }
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "#${item.id}: " + item.text.take(65) + if (item.text.length > 65) "..." else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }

                                if (isChecked) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // Stepper for counts
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                val next = when {
                                                    currentTarget > 33 -> 33
                                                    currentTarget > 10 -> 10
                                                    currentTarget > 3 -> 3
                                                    currentTarget > 1 -> 1
                                                    else -> 1
                                                }
                                                selectedMap[item.id] = next
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Text("-", fontWeight = FontWeight.Bold)
                                        }

                                        Text(
                                            text = "$currentTarget",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        IconButton(
                                            onClick = {
                                                val next = when {
                                                    currentTarget < 3 -> 3
                                                    currentTarget < 10 -> 10
                                                    currentTarget < 33 -> 33
                                                    currentTarget < 100 -> 100
                                                    else -> currentTarget + 10
                                                }
                                                selectedMap[item.id] = next
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Text("+", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && selectedMap.isNotEmpty()) {
                        val list = selectedMap.map { Pair(it.key, it.value) }
                        onSave(name.trim(), list)
                    }
                },
                enabled = name.isNotBlank() && selectedMap.isNotEmpty()
            ) {
                Text("حفظ الوِرد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
