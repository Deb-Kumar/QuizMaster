package com.example.onlinequizapp.ui.teacher.create

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.onlinequizapp.domain.model.AssignmentType
import com.example.onlinequizapp.domain.model.Batch
import com.example.onlinequizapp.domain.model.EnrolledStudent
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PublishTargetDialog(
    targetType: AssignmentType,
    onTargetTypeChanged: (AssignmentType) -> Unit,
    batches: List<Batch>,
    selectedBatchIds: Set<String>,
    onToggleBatch: (String) -> Unit,
    students: List<EnrolledStudent>,
    selectedStudentIds: Set<String>,
    onToggleStudent: (String) -> Unit,
    hasScheduleWindow: Boolean = false,
    onToggleScheduleWindow: (Boolean) -> Unit = {},
    availabilityStartMillis: Long? = null,
    onAvailabilityStartMillisChanged: (Long?) -> Unit = {},
    availabilityEndMillis: Long? = null,
    onAvailabilityEndMillisChanged: (Long?) -> Unit = {},
    scheduleSummary: String? = null,
    shuffleQuestionsAndOptions: Boolean = false,
    onToggleShuffleQuestionsAndOptions: (Boolean) -> Unit = {},
    onDismiss: () -> Unit,
    onConfirmPublish: () -> Unit,
    isLoading: Boolean,
    errorMessage: String? = null
) {
    val isDark = isTeacherInDarkTheme()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(if (isDark) Color(0xFF0F172A) else Color.White)
                .border(1.dp, if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Publish Quiz",
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Choose audience & schedule availability",
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                    }
                }

                // Target Type Selector Tabs (Batch vs Individual Student vs Public)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Option 1: Publish to Batch
                    val isBatch = targetType == AssignmentType.BATCH
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isBatch) (if (isDark) Color(0xFF6366F1) else Color(0xFF4F46E5)) else Color.Transparent)
                            .clickable { onTargetTypeChanged(AssignmentType.BATCH) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = if (isBatch) Color.White else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Batch",
                                color = if (isBatch) Color.White else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    // Option 2: Publish to Individual Student
                    val isStudent = targetType == AssignmentType.INDIVIDUAL_STUDENT
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isStudent) (if (isDark) Color(0xFF6366F1) else Color(0xFF4F46E5)) else Color.Transparent)
                            .clickable { onTargetTypeChanged(AssignmentType.INDIVIDUAL_STUDENT) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isStudent) Color.White else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Individual",
                                color = if (isStudent) Color.White else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    // Option 3: Public (6-Character Code Access)
                    val isPublic = targetType == AssignmentType.PUBLIC
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isPublic) (if (isDark) Color(0xFF6366F1) else Color(0xFF4F46E5)) else Color.Transparent)
                            .clickable { onTargetTypeChanged(AssignmentType.PUBLIC) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = if (isPublic) Color.White else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Public",
                                color = if (isPublic) Color.White else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }

                // Error message banner if any
                errorMessage?.let { err ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF450A0A))
                            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(text = err, color = Color(0xFFFCA5A5), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // List of Batches, Students, or Public Access Card
                when (targetType) {
                    AssignmentType.BATCH -> {
                        if (batches.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No active batches found. Please create a batch in Students tab first.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(batches) { batch ->
                                    val isSelected = selectedBatchIds.contains(batch.id)
                                    val itemBg = if (isSelected) {
                                        if (isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF)
                                    } else {
                                        if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
                                    }
                                    val itemBorder = if (isSelected) {
                                        if (isDark) Color(0xFF818CF8) else Color(0xFF818CF8)
                                    } else {
                                        if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(itemBg)
                                            .border(1.dp, itemBorder, RoundedCornerShape(12.dp))
                                            .clickable { onToggleBatch(batch.id) }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) (if (isDark) Color(0xFF6366F1) else Color(0xFF4F46E5)) else (if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = batch.name, color = if (isDark) Color.White else Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(text = "Code: ${batch.batchCode} • ${batch.studentCount} students", color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AssignmentType.INDIVIDUAL_STUDENT -> {
                        if (students.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No enrolled students found in your batches yet.",
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(students) { student ->
                                    val isSelected = selectedStudentIds.contains(student.studentId)
                                    val itemBg = if (isSelected) {
                                        if (isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF)
                                    } else {
                                        if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
                                    }
                                    val itemBorder = if (isSelected) {
                                        if (isDark) Color(0xFF818CF8) else Color(0xFF818CF8)
                                    } else {
                                        if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(itemBg)
                                            .border(1.dp, itemBorder, RoundedCornerShape(12.dp))
                                            .clickable { onToggleStudent(student.studentId) }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) (if (isDark) Color(0xFF6366F1) else Color(0xFF4F46E5)) else (if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = student.name, color = if (isDark) Color.White else Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(text = student.email, color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AssignmentType.PUBLIC -> {
                        // Public Quiz Informational & Feature Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isDark) {
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF0C4A6E).copy(alpha = 0.35f), Color(0xFF1E293B).copy(alpha = 0.6f))
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFE0F2FE), Color(0xFFF0FDF4))
                                        )
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isDark) Color(0xFF0284C7).copy(alpha = 0.4f) else Color(0xFFBAE6FD),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) Color(0xFF0369A1) else Color(0xFF0284C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Public,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Public Quiz • Open Access",
                                            color = if (isDark) Color.White else Color(0xFF0F172A),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Generates a 6-character access code",
                                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Text(
                                    text = "Anyone can take this quiz by entering the generated 6-character code in the \"Join Quiz\" screen. No batch or student restriction is required.",
                                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isDark) Color(0xFF0F172A).copy(alpha = 0.7f) else Color.White)
                                        .border(
                                            1.dp,
                                            if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFFA5B4FC) else Color(0xFF6366F1),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "6-Digit/Character code provided after publishing",
                                        color = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // -----------------------------------------------------------------
                // Schedule Availability Window Configuration Card
                // -----------------------------------------------------------------
                val schedCardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
                val schedCardBorder = if (hasScheduleWindow) {
                    if (isDark) Color(0xFF6366F1) else Color(0xFF818CF8)
                } else {
                    if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(schedCardBg)
                        .border(1.dp, schedCardBorder, RoundedCornerShape(16.dp))
                        .padding(13.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Header Row with Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF6366F1)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "Schedule Availability Window",
                                        color = if (isDark) Color.White else Color(0xFF0F172A),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (hasScheduleWindow) "Quiz open only within specific dates/times" else "Always available to students",
                                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Switch(
                                checked = hasScheduleWindow,
                                onCheckedChange = onToggleScheduleWindow,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF7C3AED),
                                    uncheckedThumbColor = if (isDark) Color(0xFF94A3B8) else Color(0xFFCBD5E1),
                                    uncheckedTrackColor = if (isDark) Color(0xFF0F172A) else Color(0xFFE2E8F0)
                                )
                            )
                        }

                        if (hasScheduleWindow) {
                            val context = LocalContext.current
                            val now = System.currentTimeMillis()
                            val effectiveStart = availabilityStartMillis ?: now
                            val effectiveEnd = availabilityEndMillis

                            val pickerBoxBg = if (isDark) Color(0xFF0F172A) else Color.White
                            val pickerBoxBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                            val pickerSubBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                            val pickerSubBorder = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)

                            // 1. Start Time Picker (Opens At)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(pickerBoxBg)
                                    .border(1.dp, pickerBoxBorder, RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Start Time (Opens At)",
                                            color = if (isDark) Color(0xFF93C5FD) else Color(0xFF0369A1),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Text(
                                        text = "Reset to Now",
                                        color = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { onAvailabilityStartMillisChanged(System.currentTimeMillis()) }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(pickerSubBg)
                                        .border(1.dp, pickerSubBorder, RoundedCornerShape(9.dp))
                                        .clickable {
                                            showScheduleDateTimePicker(context, effectiveStart) { selected ->
                                                onAvailabilityStartMillisChanged(selected)
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formatScheduleDateTime(availabilityStartMillis),
                                        color = if (isDark) Color.White else Color(0xFF0F172A),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Change 📅",
                                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // 2. Deadline Picker (Closes At)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(pickerBoxBg)
                                    .border(1.dp, pickerBoxBorder, RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Deadline (Closes At)",
                                            color = if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    if (availabilityEndMillis != null) {
                                        Text(
                                            text = "Remove Deadline",
                                            color = Color(0xFFEF4444),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { onAvailabilityEndMillisChanged(null) }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(pickerSubBg)
                                        .border(1.dp, pickerSubBorder, RoundedCornerShape(9.dp))
                                        .clickable {
                                            val base = effectiveEnd ?: (effectiveStart + 3600000L)
                                            showScheduleDateTimePicker(context, base) { selected ->
                                                onAvailabilityEndMillisChanged(selected)
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (availabilityEndMillis != null) formatScheduleDateTime(availabilityEndMillis) else "No Deadline set",
                                        color = if (availabilityEndMillis != null) (if (isDark) Color.White else Color(0xFF0F172A)) else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Set Deadline ⏱️",
                                        color = if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Quick Shortcut Chips for Deadline (+1h, +2h, +1d, +7d)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        "+1 Hour" to 3600000L,
                                        "+2 Hours" to 7200000L,
                                        "+1 Day" to 86400000L,
                                        "+7 Days" to 604800000L
                                    ).forEach { (label, offset) ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(7.dp))
                                                .background(pickerSubBg)
                                                .border(1.dp, pickerSubBorder, RoundedCornerShape(7.dp))
                                                .clickable {
                                                    val base = effectiveStart
                                                    onAvailabilityEndMillisChanged(base + offset)
                                                }
                                                .padding(vertical = 5.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isDark) Color(0xFF93C5FD) else Color(0xFF2563EB),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                // Validation warning if end <= start
                                if (availabilityStartMillis != null && availabilityEndMillis != null && availabilityEndMillis <= availabilityStartMillis) {
                                    Text(
                                        text = "⚠️ Deadline must be set after the start time.",
                                        color = Color(0xFFEF4444),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Shuffle Questions & Options Card ─────────────────────────
                val shuffleCardBg = if (shuffleQuestionsAndOptions) {
                    if (isDark) Color(0xFF1E1B4B) else Color(0xFFF5F3FF)
                } else {
                    if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
                }
                val shuffleCardBorder = if (shuffleQuestionsAndOptions) {
                    if (isDark) Color(0xFF8B5CF6) else Color(0xFFA78BFA)
                } else {
                    if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(shuffleCardBg)
                        .border(1.dp, shuffleCardBorder, RoundedCornerShape(16.dp))
                        .padding(13.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "Shuffle Questions & Options",
                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (shuffleQuestionsAndOptions)
                                        "Randomized questions & choices for each student"
                                    else
                                        "Fixed sequence for all students",
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = shuffleQuestionsAndOptions,
                            onCheckedChange = onToggleShuffleQuestionsAndOptions,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF8B5CF6),
                                uncheckedThumbColor = if (isDark) Color(0xFF94A3B8) else Color(0xFFCBD5E1),
                                uncheckedTrackColor = if (isDark) Color(0xFF0F172A) else Color(0xFFE2E8F0)
                            )
                        )
                    }
                }

                // Action Buttons
                val isScheduleValid = !hasScheduleWindow || (availabilityStartMillis == null || availabilityEndMillis == null || availabilityEndMillis > availabilityStartMillis)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isDark) Color.Transparent else Color.White,
                            contentColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569))
                    }

                    Button(
                        onClick = onConfirmPublish,
                        enabled = !isLoading && isScheduleValid && (
                            (targetType == AssignmentType.BATCH && selectedBatchIds.isNotEmpty()) ||
                            (targetType == AssignmentType.INDIVIDUAL_STUDENT && selectedStudentIds.isNotEmpty()) ||
                            (targetType == AssignmentType.PUBLIC)
                        ),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Confirm & Publish", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun showScheduleDateTimePicker(
    context: android.content.Context,
    initialMillis: Long,
    onDateTimeSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initialMillis }
    val datePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val timePicker = TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    onDateTimeSelected(calendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            )
            timePicker.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePicker.show()
}

private fun formatScheduleDateTime(millis: Long?): String {
    if (millis == null) return "Immediately on publish"
    val sdf = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}
