package com.example.onlinequizapp.ui.home.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.School
import com.example.onlinequizapp.domain.model.BatchJoinRequest
import com.example.onlinequizapp.domain.model.JoinRequestStatus
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onlinequizapp.data.repository.BatchRepositoryImpl
import com.example.onlinequizapp.domain.model.Batch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PurplePrimary = Color(0xFF8B5CF6)
private val ErrorRed = Color(0xFFEF4444)

/**
 * Dialog displaying all batches the student has joined.
 */
@Composable
fun MyBatchesDialog(
    studentId: String,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onJoinAnotherBatch: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val batchesFlow = remember(studentId) {
        BatchRepositoryImpl.instance.getBatchesForStudent(studentId)
    }
    val batches by batchesFlow.collectAsState(initial = emptyList())

    val requestsFlow = remember(studentId) {
        BatchRepositoryImpl.instance.getJoinRequestsForStudent(studentId)
    }
    val requests by requestsFlow.collectAsState(initial = emptyList())
    var selectedTab by remember { mutableStateOf(0) } // 0: Enrolled Batches, 1: Join Requests

    var batchPendingLeave by remember { mutableStateOf<Batch?>(null) }
    var isLeaving by remember { mutableStateOf(false) }

    val bg = if (isDark) Color(0xFF0F1738) else Color(0xFFFFFFFF)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val itemBg = if (isDark) Color(0xFF070B1F) else Color(0xFFF8FAFC)
    val itemBorder = if (isDark) Color(0xFF162354) else Color(0xFFE2E8F0)
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }

    // Leave confirmation dialog
    batchPendingLeave?.let { batch ->
        Dialog(
            onDismissRequest = { if (!isLeaving) batchPendingLeave = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(BorderStroke(1.dp, itemBorder), RoundedCornerShape(24.dp)),
                color = bg,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    // Header Row with Red Exit Badge & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ErrorRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Leave Batch?",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Text(
                                    text = "Confirm batch exit",
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = { if (!isLeaving) batchPendingLeave = null },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF162354) else Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = if (isDark) Color(0xFF162354) else Color(0xFFE2E8F0))

                    Spacer(modifier = Modifier.height(16.dp))

                    // Batch Details Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(itemBg)
                            .border(BorderStroke(1.dp, itemBorder), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PurplePrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = batch.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Code: ${batch.batchCode}",
                                    fontSize = 12.sp,
                                    color = textSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Are you sure you want to leave this batch? You will no longer receive quizzes assigned exclusively to this batch.",
                        fontSize = 13.5.sp,
                        color = textSecondary,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Action Buttons Row (Cancel & Leave)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { if (!isLeaving) batchPendingLeave = null },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, itemBorder)
                        ) {
                            Text(
                                text = "Cancel",
                                color = textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }

                        Button(
                            onClick = {
                                isLeaving = true
                                coroutineScope.launch {
                                    val res = BatchRepositoryImpl.instance.leaveBatch(
                                        batchId = batch.id,
                                        studentId = studentId
                                    )
                                    isLeaving = false
                                    batchPendingLeave = null
                                    if (res.isSuccess) {
                                        Toast.makeText(context, "Left ${batch.name}", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to leave batch.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isLeaving,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ErrorRed,
                                disabledContainerColor = ErrorRed.copy(alpha = 0.5f)
                            )
                        ) {
                            if (isLeaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Leave Batch",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(520.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = bg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PurplePrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = PurplePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "My Batches",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = "${batches.size} enrolled • ${requests.size} requests",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Switcher: Enrolled vs Requests
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF070B1F) else Color(0xFFF1F5F9))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 0) PurplePrimary else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Enrolled (${batches.size})",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) Color.White else textSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 1) PurplePrimary else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Requests (${requests.size})",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 1) Color.White else textSecondary
                            )
                            val pendingCount = requests.count { it.isPending }
                            if (pendingCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0xFFF59E0B))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "$pendingCount",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // ==========================================
                    // TAB 0: ENROLLED BATCHES
                    // ==========================================
                    if (batches.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = textSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "You haven't joined any batches yet.",
                                    color = textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ask your teacher for a 6-character joining code to enroll in a batch.",
                                    color = textSecondary,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                                    items(batches, key = { it.id }) { batch ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(itemBg)
                                        .border(BorderStroke(1.dp, itemBorder), RoundedCornerShape(14.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(1.dp)
                                    ) {
                                        Text(
                                            text = batch.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (batch.description.isNotBlank()) {
                                            Text(
                                                text = batch.description,
                                                fontSize = 12.sp,
                                                color = textSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.padding(top = 1.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "Code: ${batch.batchCode}",
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = PurplePrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "•",
                                                fontSize = 11.sp,
                                                color = textSecondary
                                            )
                                            Text(
                                                text = "${batch.studentCount} Students",
                                                fontSize = 11.sp,
                                                color = textSecondary
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { batchPendingLeave = batch },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                            contentDescription = "Leave Batch",
                                            tint = textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ==========================================
                    // TAB 1: JOIN REQUESTS
                    // ==========================================
                    if (requests.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.PendingActions,
                                    contentDescription = null,
                                    tint = textSecondary,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No Batch Requests",
                                    color = textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "When you enter a code for a private batch, your pending request and status will appear here.",
                                    color = textSecondary,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(requests, key = { it.id }) { req ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(itemBg)
                                        .border(BorderStroke(1.dp, itemBorder), RoundedCornerShape(14.dp))
                                        .padding(14.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    Icons.Default.School,
                                                    contentDescription = null,
                                                    tint = PurplePrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = req.batchName,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Status Badge
                                            val (badgeText, badgeColor, badgeBg) = when (req.status) {
                                                JoinRequestStatus.PENDING -> Triple("Pending", Color(0xFFF59E0B), Color(0xFFF59E0B).copy(alpha = 0.15f))
                                                JoinRequestStatus.ACCEPTED -> Triple("Accepted", Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.15f))
                                                JoinRequestStatus.REJECTED -> Triple("Rejected", Color(0xFFEF4444), Color(0xFFEF4444).copy(alpha = 0.15f))
                                                JoinRequestStatus.CANCELLED -> Triple("Cancelled", textSecondary, textSecondary.copy(alpha = 0.15f))
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(badgeBg)
                                                    .border(0.8.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = badgeText,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = badgeColor
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Requested: ${dateFormat.format(Date(req.requestedAt))}",
                                                fontSize = 11.5.sp,
                                                color = textSecondary
                                            )

                                            if (req.isRejected && !req.rejectionReason.isNullOrBlank()) {
                                                Text(
                                                    text = "Reason: ${req.rejectionReason}",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFFEF4444),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        if (req.isRejected) {
                                            Text(
                                                text = "Request was rejected. You can re-apply if appropriate.",
                                                fontSize = 11.sp,
                                                color = textSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action to join another batch
                Button(
                    onClick = {
                        onDismiss()
                        onJoinAnotherBatch()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Join Another Batch", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
