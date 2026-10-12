package com.example.onlinequizapp.ui.teacher.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.collectAsState
import com.example.onlinequizapp.domain.model.BatchAccessMode
import com.example.onlinequizapp.domain.model.BatchJoinRequest
import com.example.onlinequizapp.domain.model.JoinRequestStatus
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onlinequizapp.data.repository.BatchRepositoryImpl
import com.example.onlinequizapp.domain.model.Batch
import com.example.onlinequizapp.domain.model.EnrolledStudent
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BrandPurple = Color(0xFF8B5CF6)
private val BrandIndigo = Color(0xFF6366F1)
private val AccentGreen = Color(0xFF10B981)
private val AccentAmber = Color(0xFFF59E0B)
private val AccentRose = Color(0xFFEF4444)

/**
 * Modern Card presenting a single Teacher Batch with quick code copy,
 * statistics badge, and student management triggers.
 */
@Composable
fun TeacherBatchCard(
    batch: Batch,
    onViewStudents: (Batch) -> Unit,
    onEditBatch: (Batch) -> Unit,
    onDeleteBatch: (Batch) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isTeacherInDarkTheme()
    var showMenu by remember { mutableStateOf(false) }
    var copiedCodeRecently by remember { mutableStateOf(false) }

    LaunchedEffect(copiedCodeRecently) {
        if (copiedCodeRecently) {
            kotlinx.coroutines.delay(2000)
            copiedCodeRecently = false
        }
    }

    // Determine theme gradient for the batch: Java / Data Science / MCA
    val isJava = batch.name.contains("Java", ignoreCase = true) || batch.name.contains("Kotlin", ignoreCase = true)
    val isData = batch.name.contains("Data", ignoreCase = true) || batch.name.contains("Python", ignoreCase = true)

    val iconGradient = when {
        isData -> listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
        else -> listOf(Color(0xFFA855F7), Color(0xFF7C3AED))
    }

    val cardBorderBrush = if (isDark) {
        val borderGradient = when {
            isData -> listOf(
                Color(0xFF38BDF8),
                Color(0xFF0284C7).copy(alpha = 0.5f),
                Color(0xFF1E2958)
            )
            else -> listOf(
                Color(0xFFC084FC),
                Color(0xFF7C3AED).copy(alpha = 0.5f),
                Color(0xFF1E2958)
            )
        }
        Brush.horizontalGradient(borderGradient)
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
    }

    val cardBg = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF091032).copy(alpha = 0.95f),
                Color(0xFF070B24).copy(alpha = 0.95f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White,
                Color(0xFFF8FAFC)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(cardBg)
            .border(
                if (isDark) 1.5.dp else 1.dp,
                cardBorderBrush,
                RoundedCornerShape(22.dp)
            )
            .clickable { onViewStudents(batch) }
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Squircle Graduation Cap + Batch Name/Desc + 3-dot Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Squircle Graduation Icon Box
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(iconGradient)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = batch.name,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Visibility Mode Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (batch.isPublic) {
                                            if (isDark) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFFDCFCE7)
                                        } else {
                                            if (isDark) Color(0xFF451A03).copy(alpha = 0.5f) else Color(0xFFFEF3C7)
                                        }
                                    )
                                    .border(
                                        0.8.dp,
                                        if (batch.isPublic) {
                                            if (isDark) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0xFF86EFAC)
                                        } else {
                                            if (isDark) Color(0xFFF59E0B).copy(alpha = 0.6f) else Color(0xFFFDE68A)
                                        },
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = if (batch.isPublic) Icons.Default.Public else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (batch.isPublic) AccentGreen else AccentAmber,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = if (batch.isPublic) "Public" else "Private",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (batch.isPublic) AccentGreen else AccentAmber
                                    )
                                }
                            }
                        }
                        if (batch.description.isNotBlank()) {
                            Text(
                                text = batch.description,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(if (isDark) Color(0xFF131D45) else Color.White)
                            .border(BorderStroke(1.dp, if (isDark) Color(0xFF1E2B60) else Color(0xFFE2E8F0)), RoundedCornerShape(10.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Batch", color = if (isDark) Color.White else Color(0xFF0F172A)) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFA78BFA)) },
                            onClick = {
                                showMenu = false
                                onEditBatch(batch)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Batch", color = AccentRose) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = AccentRose) },
                            onClick = {
                                showMenu = false
                                onDeleteBatch(batch)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Code & Copy Code Highlight Capsule Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark) Color(0xFF04081E) else Color(0xFFF1F5F9))
                    .border(
                        1.2.dp,
                        if (isDark) Color(0xFF1A2655) else Color(0xFFE2E8F0),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Join Code Block
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Text(
                            text = "JOIN CODE",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF818CF8) else Color(0xFF6366F1),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = batch.batchCode,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            letterSpacing = 1.2.sp
                        )
                    }

                    // Copy Code Button: Rounded Gradient Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (copiedCodeRecently) {
                                    Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                                } else {
                                    Brush.horizontalGradient(listOf(Color(0xFF9333EA), Color(0xFF6366F1), Color(0xFF3B82F6)))
                                }
                            )
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Batch Join Code", batch.batchCode)
                                clipboard.setPrimaryClip(clip)
                                copiedCodeRecently = true
                                Toast.makeText(context, "Join Code ${batch.batchCode} copied!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 13.dp, vertical = 6.5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (copiedCodeRecently) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (copiedCodeRecently) "Copied!" else "Copy Code",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Footer Row: Students Enrolled Count & "View Students >" Pill Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = "${batch.studentCount} Students Enrolled",
                        fontSize = 12.5.sp,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }

                // "View Students >" Pill Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isDark) Color(0xFF0F183C) else Color(0xFFEDE9FE))
                        .border(
                            1.2.dp,
                            if (isDark) Color(0xFF334175) else Color(0xFFDDD6FE),
                            RoundedCornerShape(18.dp)
                        )
                        .clickable { onViewStudents(batch) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "View Students",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF6D28D9)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (isDark) Color.White else Color(0xFF6D28D9),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog enabling teachers to create a new batch with immediate code generation.
 */
@Composable
fun CreateBatchDialog(
    teacherId: String,
    onDismiss: () -> Unit,
    onBatchCreated: (Batch) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = isTeacherInDarkTheme()

    var batchName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var accessMode by remember { mutableStateOf(BatchAccessMode.PUBLIC) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var createdBatchResult by remember { mutableStateOf<Batch?>(null) }

    val bg = if (isDark) Color(0xFF0F1738) else Color(0xFFFFFFFF)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f),
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF1E2D64) else Color(0xFFE2E8F0)),
            color = bg
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                if (createdBatchResult == null) {
                    // Header Row: Top-left icon + Title + Subtitle + Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Left top icon in gradient circle
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(BrandPurple, BrandIndigo)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Create New Batch",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Text(
                                    text = "Group students & assign quizzes",
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = if (isDark) Color(0xFF1E2D64) else Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Batch Name Input (Reduced corner, leading icon, matching personal info modal)
                    OutlinedTextField(
                        value = batchName,
                        onValueChange = {
                            batchName = it
                            errorMessage = null
                        },
                        label = { Text("Batch Name (e.g. MCA Semester 1)", color = textSecondary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = BrandPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPurple,
                            unfocusedBorderColor = if (isDark) Color(0xFF1E2D64) else Color(0xFFCBD5E1),
                            focusedContainerColor = if (isDark) Color(0xFF0A102E) else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isDark) Color(0xFF0A102E) else Color(0xFFF8FAFC),
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description Input (Optional) (Reduced corner, leading icon)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Subject / Description (Optional)", color = textSecondary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = BrandPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPurple,
                            unfocusedBorderColor = if (isDark) Color(0xFF1E2D64) else Color(0xFFCBD5E1),
                            focusedContainerColor = if (isDark) Color(0xFF0A102E) else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isDark) Color(0xFF0A102E) else Color(0xFFF8FAFC),
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Batch Access Mode Selector
                    Text(
                        text = "Access Mode",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Public Card
                        val isPublicSelected = accessMode == BatchAccessMode.PUBLIC
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isPublicSelected) {
                                        if (isDark) Color(0xFF0F253E) else Color(0xFFEFF6FF)
                                    } else {
                                        if (isDark) Color(0xFF070B1F) else Color(0xFFF8FAFC)
                                    }
                                )
                                .border(
                                    BorderStroke(
                                        if (isPublicSelected) 1.5.dp else 1.dp,
                                        if (isPublicSelected) BrandPurple else if (isDark) Color(0xFF1E2B60) else Color(0xFFE2E8F0)
                                    ),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { accessMode = BatchAccessMode.PUBLIC }
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = if (isPublicSelected) BrandPurple else textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Public",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPublicSelected) BrandPurple else textPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Students join directly with code",
                                    fontSize = 10.5.sp,
                                    color = textSecondary,
                                    lineHeight = 13.sp
                                )
                            }
                        }

                        // Private Card
                        val isPrivateSelected = accessMode == BatchAccessMode.PRIVATE
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isPrivateSelected) {
                                        if (isDark) Color(0xFF28183E) else Color(0xFFFAF5FF)
                                    } else {
                                        if (isDark) Color(0xFF070B1F) else Color(0xFFF8FAFC)
                                    }
                                )
                                .border(
                                    BorderStroke(
                                        if (isPrivateSelected) 1.5.dp else 1.dp,
                                        if (isPrivateSelected) BrandPurple else if (isDark) Color(0xFF1E2B60) else Color(0xFFE2E8F0)
                                    ),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { accessMode = BatchAccessMode.PRIVATE }
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (isPrivateSelected) BrandPurple else textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Private",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPrivateSelected) BrandPurple else textPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Requires teacher approval",
                                    fontSize = 10.5.sp,
                                    color = textSecondary,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = AccentRose,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel", color = textSecondary)
                        }

                        Button(
                            onClick = {
                                if (batchName.trim().isBlank()) {
                                    errorMessage = "Please enter a batch name."
                                    return@Button
                                }
                                isSubmitting = true
                                errorMessage = null
                                coroutineScope.launch {
                                    val result = BatchRepositoryImpl.instance.createBatch(
                                        teacherId = teacherId.ifBlank { "teacher_default" },
                                        name = batchName.trim(),
                                        description = description.trim(),
                                        accessMode = accessMode
                                    )
                                    isSubmitting = false
                                    result.fold(
                                        onSuccess = { createdBatch ->
                                            createdBatchResult = createdBatch
                                            onBatchCreated(createdBatch)
                                        },
                                        onFailure = { ex ->
                                            errorMessage = ex.message ?: "Failed to create batch."
                                        }
                                    )
                                }
                            },
                            enabled = !isSubmitting,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text("Create Batch", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                } else {
                    // Success View: Display newly generated code prominently!
                    val batch = createdBatchResult!!
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(AccentGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Batch Created Successfully!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = batch.name,
                                fontSize = 14.sp,
                                color = textSecondary
                            )
                            Text(
                                text = if (batch.isPublic) "• 🌐 Public" else "• 🔒 Private",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (batch.isPublic) AccentGreen else AccentAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Large Code Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDark) Color(0xFF070B1F) else Color(0xFFF8FAFC))
                                .border(
                                    BorderStroke(1.5.dp, BrandPurple),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "STUDENT JOIN CODE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandPurple,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = batch.batchCode,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = textPrimary,
                                    letterSpacing = 4.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Share this 6-character code with your students. They can enter it on their dashboard to join this batch.",
                            fontSize = 12.sp,
                            color = textSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Actions: Copy & Done
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Batch Join Code", batch.batchCode)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Join code ${batch.batchCode} copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BrandPurple)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Code", color = BrandPurple, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                            ) {
                                Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Detailed Dialog displaying the roster of enrolled students in a batch
 * with the ability for the teacher to remove students.
 */
@Composable
fun BatchStudentsDialog(
    batch: Batch,
    teacherId: String,
    onDismiss: () -> Unit,
    onStudentRemoved: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = isTeacherInDarkTheme()

    var students by remember { mutableStateOf<List<EnrolledStudent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var studentPendingRemoval by remember { mutableStateOf<EnrolledStudent?>(null) }
    var isRemoving by remember { mutableStateOf(false) }

    val bg = if (isDark) Color(0xFF0F1738) else Color(0xFFFFFFFF)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val itemBg = if (isDark) Color(0xFF070B1F) else Color(0xFFF8FAFC)
    val itemBorder = if (isDark) Color(0xFF162354) else Color(0xFFE2E8F0)

    fun loadRoster() {
        isLoading = true
        coroutineScope.launch {
            val result = BatchRepositoryImpl.instance.getStudentsInBatch(
                batchId = batch.id,
                teacherId = teacherId.ifBlank { "teacher_default" }
            )
            isLoading = false
            students = result.getOrDefault(emptyList())
        }
    }

    LaunchedEffect(batch.id) {
        loadRoster()
    }

    // Confirmation dialog before removing student
    studentPendingRemoval?.let { student ->
        AlertDialog(
            onDismissRequest = { studentPendingRemoval = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = bg,
            title = {
                Text(
                    text = "Remove Student?",
                    color = textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove \"${student.name}\" from ${batch.name}? They will no longer have access to this batch's quizzes.",
                    color = textSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isRemoving = true
                        coroutineScope.launch {
                            val res = BatchRepositoryImpl.instance.removeStudentFromBatch(
                                batchId = batch.id,
                                studentId = student.studentId,
                                teacherId = teacherId.ifBlank { "teacher_default" }
                            )
                            isRemoving = false
                            studentPendingRemoval = null
                            if (res.isSuccess) {
                                Toast.makeText(context, "${student.name} removed from batch.", Toast.LENGTH_SHORT).show()
                                loadRoster()
                                onStudentRemoved()
                            } else {
                                Toast.makeText(context, "Failed to remove student.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRose)
                ) {
                    if (isRemoving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Remove", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { studentPendingRemoval = null }) {
                    Text("Cancel", color = textSecondary)
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(560.dp),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF1E2B60) else Color(0xFFE2E8F0)),
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF8B5CF6), Color(0xFFD946EF))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            Text(
                                text = batch.name,
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                lineHeight = 20.sp
                            )
                            if (batch.description.isNotBlank()) {
                                Text(
                                    text = batch.description,
                                    fontSize = 12.sp,
                                    color = textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 15.sp
                                )
                            }
                            Text(
                                text = "Code: ${batch.batchCode} • ${students.size} Students",
                                fontSize = 11.sp,
                                color = BrandPurple,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandPurple)
                    }
                } else if (students.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
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
                                text = "No students have joined yet.",
                                color = textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Share the join code \"${batch.batchCode}\" with students to invite them.",
                                color = textSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(students, key = { it.studentId }) { student ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(itemBg)
                                    .border(BorderStroke(1.dp, itemBorder), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(BrandPurple.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!student.avatarUrl.isNullOrBlank()) {
                                            SubcomposeAsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(student.avatarUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = student.name,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop,
                                                error = {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = BrandPurple,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = BrandPurple,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(0.dp)
                                    ) {
                                        Text(
                                            text = student.name,
                                            color = textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            lineHeight = 16.sp
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            modifier = Modifier.padding(top = 0.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Email,
                                                contentDescription = null,
                                                tint = textSecondary,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Text(
                                                text = student.email,
                                                color = textSecondary,
                                                fontSize = 11.5.sp,
                                                lineHeight = 14.sp
                                            )
                                        }

                                        if (student.degree.isNotBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFEEF2FF))
                                                    .border(
                                                        1.dp,
                                                        if (isDark) Color(0xFF334155) else Color(0xFFC7D2FE),
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = student.degree,
                                                    color = if (isDark) Color(0xFF93C5FD) else Color(0xFF4F46E5),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { studentPendingRemoval = student },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonRemove,
                                        contentDescription = "Remove",
                                        tint = AccentRose,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherSectionTabs(
    selectedSection: Int,
    onSectionSelected: (Int) -> Unit,
    batchesCount: Int,
    studentsCount: Int,
    modifier: Modifier = Modifier
) {
    val isDark = isTeacherInDarkTheme()
    val tab0Selected = selectedSection == 0
    val tab1Selected = selectedSection == 1

    val containerBg = if (isDark) Color(0xFF0C1438).copy(alpha = 0.9f) else Color(0xFFE2E8F0)
    val containerBorder = if (isDark) Color(0xFF1E2B60) else Color(0xFFCBD5E1)
    val unselectedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(containerBg)
            .border(
                1.2.dp,
                containerBorder,
                RoundedCornerShape(26.dp)
            )
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 0: Batches
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        if (tab0Selected) {
                            Brush.horizontalGradient(
                                listOf(Color(0xFF6366F1), Color(0xFF2563EB), Color(0xFF38BDF8))
                            )
                        } else {
                            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                    )
                    .clickable { onSectionSelected(0) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = if (tab0Selected) Color.White else unselectedColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Batches ($batchesCount)",
                        color = if (tab0Selected) Color.White else unselectedColor,
                        fontWeight = if (tab0Selected) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    )
                }
            }

            // Tab 1: Directory
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        if (tab1Selected) {
                            Brush.horizontalGradient(
                                listOf(Color(0xFF6366F1), Color(0xFF2563EB), Color(0xFF38BDF8))
                            )
                        } else {
                            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                    )
                    .clickable { onSectionSelected(1) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = if (tab1Selected) Color.White else unselectedColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Directory ($studentsCount)",
                        color = if (tab1Selected) Color.White else unselectedColor,
                        fontWeight = if (tab1Selected) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    )
                }
            }
        }
    }
}

/**
 * Dialog displaying join requests submitted by students for the teacher's private batches.
 * Allows the teacher to Accept or Reject each request in real time.
 */
@Composable
fun TeacherJoinRequestsDialog(
    teacherId: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = isTeacherInDarkTheme()

    val requestsFlow = remember(teacherId) {
        BatchRepositoryImpl.instance.getJoinRequestsForTeacher(teacherId)
    }
    val allRequests by requestsFlow.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(0) } // 0: Pending, 1: History (All)
    val pendingRequests = remember(allRequests) {
        allRequests.filter { it.isPending }
    }
    val displayRequests = remember(allRequests, selectedTab) {
        if (selectedTab == 0) pendingRequests else allRequests
    }

    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(teacherId) {
        isRefreshing = true
        BatchRepositoryImpl.instance.refreshJoinRequests(teacherId)
        isRefreshing = false
    }

    var processingRequestId by remember { mutableStateOf<String?>(null) }
    var isBatchProcessing by remember { mutableStateOf(false) }
    var showConfirmAcceptAllDialog by remember { mutableStateOf(false) }
    var showConfirmRejectAllDialog by remember { mutableStateOf(false) }

    val bg = if (isDark) Color(0xFF0F1738) else Color(0xFFFFFFFF)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val itemBg = if (isDark) Color(0xFF070B1F) else Color(0xFFF8FAFC)
    val itemBorder = if (isDark) Color(0xFF162354) else Color(0xFFE2E8F0)

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }

    if (showConfirmAcceptAllDialog) {
        AlertDialog(
            onDismissRequest = { if (!isBatchProcessing) showConfirmAcceptAllDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = if (isDark) Color(0xFF0F1738) else Color(0xFFFFFFFF),
            title = {
                Text(
                    text = "Accept All Requests?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to approve all ${pendingRequests.size} pending student requests? All students will be immediately enrolled into their respective batches.",
                    fontSize = 13.5.sp,
                    color = textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isBatchProcessing = true
                        coroutineScope.launch {
                            val res = BatchRepositoryImpl.instance.acceptAllJoinRequests(
                                teacherId = teacherId,
                                requestIds = pendingRequests.map { it.id }
                            )
                            isBatchProcessing = false
                            showConfirmAcceptAllDialog = false
                            if (res.isSuccess) {
                                val count = res.getOrDefault(0)
                                Toast.makeText(context, "Successfully accepted $count request${if (count != 1) "s" else ""}!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, res.exceptionOrNull()?.message ?: "Failed to accept requests.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isBatchProcessing
                ) {
                    if (isBatchProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Accept All", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmAcceptAllDialog = false },
                    enabled = !isBatchProcessing
                ) {
                    Text("Cancel", color = textSecondary)
                }
            }
        )
    }

    if (showConfirmRejectAllDialog) {
        AlertDialog(
            onDismissRequest = { if (!isBatchProcessing) showConfirmRejectAllDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = if (isDark) Color(0xFF0F1738) else Color(0xFFFFFFFF),
            title = {
                Text(
                    text = "Reject All Requests?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to decline all ${pendingRequests.size} pending student requests? These students will not be granted access to the batches.",
                    fontSize = 13.5.sp,
                    color = textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isBatchProcessing = true
                        coroutineScope.launch {
                            val res = BatchRepositoryImpl.instance.rejectAllJoinRequests(
                                teacherId = teacherId,
                                requestIds = pendingRequests.map { it.id }
                            )
                            isBatchProcessing = false
                            showConfirmRejectAllDialog = false
                            if (res.isSuccess) {
                                val count = res.getOrDefault(0)
                                Toast.makeText(context, "Declined $count request${if (count != 1) "s" else ""}.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, res.exceptionOrNull()?.message ?: "Failed to reject requests.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRose),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isBatchProcessing
                ) {
                    if (isBatchProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Reject All", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmRejectAllDialog = false },
                    enabled = !isBatchProcessing
                ) {
                    Text("Cancel", color = textSecondary)
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp)),
            color = bg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Row
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
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PendingActions,
                                contentDescription = null,
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Join Requests",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                if (pendingRequests.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(AccentAmber)
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${pendingRequests.size}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Review student access to your private batches",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    isRefreshing = true
                                    BatchRepositoryImpl.instance.refreshJoinRequests(teacherId)
                                    isRefreshing = false
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = BrandPurple
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = textSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Switcher: Pending vs All
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
                            .background(if (selectedTab == 0) BrandPurple else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pending (${pendingRequests.size})",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) Color.White else textSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 1) BrandPurple else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All History (${allRequests.size})",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) Color.White else textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bulk Action Buttons: Reject All & Accept All
                if (selectedTab == 0 && pendingRequests.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showConfirmRejectAllDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isDark) AccentRose.copy(alpha = 0.08f) else Color(0xFFFEF2F2),
                                contentColor = AccentRose
                            ),
                            enabled = !isBatchProcessing && processingRequestId == null,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = AccentRose
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Reject All (${pendingRequests.size})",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentRose
                            )
                        }

                        Button(
                            onClick = { showConfirmAcceptAllDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentGreen,
                                disabledContainerColor = AccentGreen.copy(alpha = 0.5f)
                            ),
                            enabled = !isBatchProcessing && processingRequestId == null,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            if (isBatchProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Accept All (${pendingRequests.size})",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Request List
                if (displayRequests.isEmpty()) {
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
                                text = if (selectedTab == 0) "No Pending Join Requests" else "No Join Requests Yet",
                                color = textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (selectedTab == 0)
                                    "When students enter codes for your private batches, requests will appear here."
                                else
                                    "No access requests have been submitted yet.",
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
                        items(displayRequests, key = { it.id }) { req ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(itemBg)
                                    .border(BorderStroke(1.dp, itemBorder), RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Row 1: Student info & Status Chip
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(BrandPurple.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = req.studentName.take(1).uppercase(),
                                                    color = BrandPurple,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = req.studentName,
                                                    fontSize = 14.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (req.studentEmail.isNotBlank()) {
                                                    Text(
                                                        text = req.studentEmail,
                                                        fontSize = 11.5.sp,
                                                        color = textSecondary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        // Status Chip
                                        val statusColor = when (req.status) {
                                            JoinRequestStatus.PENDING -> AccentAmber
                                            JoinRequestStatus.ACCEPTED -> AccentGreen
                                            JoinRequestStatus.REJECTED -> AccentRose
                                            JoinRequestStatus.CANCELLED -> textSecondary
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(statusColor.copy(alpha = 0.15f))
                                                .border(0.8.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = req.status.name,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = statusColor
                                            )
                                        }
                                    }

                                    // Row 2: Batch target & Timestamp
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.School,
                                                contentDescription = null,
                                                tint = BrandPurple,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = req.batchName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = BrandPurple
                                            )
                                        }

                                        Text(
                                            text = dateFormat.format(Date(req.requestedAt)),
                                            fontSize = 11.sp,
                                            color = textSecondary
                                        )
                                    }

                                    // Action Buttons: Accept / Reject (only shown when PENDING)
                                    if (req.isPending) {
                                        val isProcessing = processingRequestId == req.id
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    processingRequestId = req.id
                                                    coroutineScope.launch {
                                                        val res = BatchRepositoryImpl.instance.rejectJoinRequest(
                                                            requestId = req.id,
                                                            teacherId = teacherId
                                                        )
                                                        processingRequestId = null
                                                        if (res.isSuccess) {
                                                            Toast.makeText(context, "Request rejected.", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, res.exceptionOrNull()?.message ?: "Failed", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                enabled = !isProcessing,
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.7f)),
                                                contentPadding = PaddingValues(vertical = 6.dp)
                                            ) {
                                                Text("Reject", color = AccentRose, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    processingRequestId = req.id
                                                    coroutineScope.launch {
                                                        val res = BatchRepositoryImpl.instance.acceptJoinRequest(
                                                            requestId = req.id,
                                                            teacherId = teacherId
                                                        )
                                                        processingRequestId = null
                                                        if (res.isSuccess) {
                                                            Toast.makeText(context, "${req.studentName} joined ${req.batchName}!", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, res.exceptionOrNull()?.message ?: "Failed", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                enabled = !isProcessing,
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                                contentPadding = PaddingValues(vertical = 6.dp)
                                            ) {
                                                if (isProcessing) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(14.dp),
                                                        strokeWidth = 2.dp,
                                                        color = Color.White
                                                    )
                                                } else {
                                                    Text("Accept", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
