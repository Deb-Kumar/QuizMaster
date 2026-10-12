package com.example.onlinequizapp.ui.teacher

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import android.widget.Toast
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.onlinequizapp.data.repository.BatchRepositoryImpl
import com.example.onlinequizapp.domain.model.Batch
import com.example.onlinequizapp.domain.model.EnrolledStudent
import android.net.Uri
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import java.io.File
import com.example.onlinequizapp.domain.model.BatchAccessMode
import com.example.onlinequizapp.ui.teacher.components.BatchStudentsDialog
import com.example.onlinequizapp.ui.teacher.components.CreateBatchDialog
import com.example.onlinequizapp.ui.teacher.components.TeacherBatchCard
import com.example.onlinequizapp.ui.teacher.components.TeacherJoinRequestsDialog
import com.example.onlinequizapp.ui.teacher.components.TeacherSectionTabs
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import kotlinx.coroutines.launch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.R
import com.example.onlinequizapp.domain.model.QuizSessionState
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.ui.components.QuizMasterBrandTitle
import com.example.onlinequizapp.ui.teacher.components.TeacherNotificationButton

// -------------------------------------------------------------
// Cyberpunk & Dark Tech Palette matching uploaded design
// -------------------------------------------------------------
private val ScreenBg: Color @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF020617) else Color(0xFFF1F5F9)
private val CardBg: Color @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF080E2A) else Color(0xFFFFFFFF)
private val CardBorderColor: Color @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF141F48) else Color(0xFFE2E8F0)
private val StatCardBg: Color @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF091030) else Color(0xFFFFFFFF)
private val StatBorderColor: Color @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF162354) else Color(0xFFE2E8F0)

private val PurplePrimary = Color(0xFF8B5CF6)
private val PurpleGradient = Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFD946EF)))
private val BlueGradient = Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF06B6D4)))
private val BrandTextGradient = Brush.horizontalGradient(listOf(Color(0xFF818CF8), Color(0xFFC084FC)))

private val TextPrimary: Color @Composable get() = if (isTeacherInDarkTheme()) Color(0xFFFFFFFF) else Color(0xFF0F172A)
private val TextSecondary: Color @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF64748B)
private val TextMuted: Color @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF64748B) else Color(0xFF94A3B8)

// Sort Enum for Filter Menu
enum class StudentSortOption(val title: String) {
    DEFAULT("Default"),
    HIGHEST_SCORE("Highest Avg. Score"),
    LOWEST_SCORE("Lowest Avg. Score"),
    MOST_QUIZZES("Most Quizzes"),
    NAME_AZ("Name (A to Z)")
}

// Data Model for Student UI Item
data class StudentItem(
    val id: String,
    val name: String,
    val email: String,
    val studentCode: String,
    val avgScore: Int,
    val quizzesCount: Int,
    val lastActive: String,
    @DrawableRes val avatarRes: Int,
    val grade: String = "",
    val rank: Int = 1,
    val accuracy: Int = 0,
    val avatarUrl: String? = null,
    val batchNames: List<String> = emptyList()
)

/**
 * High-fidelity Teacher Students Screen matching uploaded design.
 */
@Composable
fun TeacherStudentsScreen(
    teacherUser: User? = null,
    submissions: List<QuizSessionState> = emptyList(),
    onBackClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Search, Filter & Pagination State
    var searchQuery by remember { mutableStateOf("") }
    var selectedSortOption by remember { mutableStateOf(StudentSortOption.DEFAULT) }
    var showSortMenu by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(1) }
    val itemsPerPage = 8

    // Selected Student for detailed Dialog inspection
    var selectedStudentForDetail by remember { mutableStateOf<StudentItem?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val teacherId = teacherUser?.id ?: (com.example.onlinequizapp.data.session.AuthSessionManager.getSavedUser()?.id ?: "")

    // Section Selector: 0 = Batches & Join Codes, 1 = Student Directory
    var selectedSection by remember { mutableIntStateOf(0) }
    val batchesFlow = remember(teacherId) { BatchRepositoryImpl.instance.getBatchesByTeacher(teacherId) }
    val batches by batchesFlow.collectAsState(initial = emptyList())

    // Real database students aggregated across teacher's batches
    var directoryStudents by remember { mutableStateOf<List<StudentItem>>(emptyList()) }
    var isLoadingDirectory by remember { mutableStateOf(false) }

    LaunchedEffect(batches, submissions) {
        isLoadingDirectory = true
        val aggregatedList = mutableListOf<EnrolledStudent>()
        val studentBatchesMap = mutableMapOf<String, MutableList<String>>()

        for (batch in batches) {
            val result = BatchRepositoryImpl.instance.getStudentsInBatch(batch.id, teacherId)
            result.getOrNull()?.let { enrolledList ->
                aggregatedList.addAll(enrolledList)
                for (item in enrolledList) {
                    val currentList = studentBatchesMap.getOrPut(item.studentId) { mutableListOf() }
                    if (!currentList.contains(batch.name)) {
                        currentList.add(batch.name)
                    }
                }
            }
        }

        // Deduplicate students by studentId
        val uniqueStudents = aggregatedList.distinctBy { it.studentId }

        val studentItems = uniqueStudents.mapIndexed { index, enrolled ->
            // EnrolledStudent already contains synced quizzesCompleted and avgScore
            val quizzesCount = enrolled.quizzesCompleted
            val avgScore = enrolled.avgScore

            val studentCode = "STU" + enrolled.studentId.takeLast(4).uppercase().padStart(4, '0')
            val avatarRes = R.drawable.img_student_hero

            val resolvedAvatar = enrolled.avatarUrl
                ?: run {
                    val avatarDir = File(context.filesDir, "profile_avatars")
                    if (avatarDir.exists()) {
                        avatarDir.listFiles { _, name -> name.startsWith("avatar_${enrolled.studentId}_") }
                            ?.maxByOrNull { it.lastModified() }
                            ?.let { Uri.fromFile(it).toString() }
                    } else null
                }

            val resolvedDegree = enrolled.degree.takeIf { it.isNotBlank() } ?: "Student"
            val enrolledBatchNames = studentBatchesMap[enrolled.studentId] ?: emptyList()

            StudentItem(
                id = enrolled.studentId,
                name = enrolled.name,
                email = enrolled.email,
                studentCode = studentCode,
                avgScore = avgScore,
                quizzesCount = quizzesCount,
                lastActive = "Active",
                avatarRes = avatarRes,
                grade = resolvedDegree,
                rank = index + 1,
                accuracy = if (avgScore > 0) avgScore else 0,
                avatarUrl = resolvedAvatar,
                batchNames = enrolledBatchNames
            )
        }

        directoryStudents = studentItems
        isLoadingDirectory = false
    }

    val allStudents = directoryStudents

    // Dynamic Filter & Search logic
    val filteredStudents = remember(searchQuery, selectedSortOption, allStudents) {
        var list = allStudents.filter { student ->
            searchQuery.isBlank() ||
                    student.name.contains(searchQuery, ignoreCase = true) ||
                    student.email.contains(searchQuery, ignoreCase = true) ||
                    student.studentCode.contains(searchQuery, ignoreCase = true)
        }

        // Sorting
        list = when (selectedSortOption) {
            StudentSortOption.DEFAULT -> list
            StudentSortOption.HIGHEST_SCORE -> list.sortedByDescending { it.avgScore }
            StudentSortOption.LOWEST_SCORE -> list.sortedBy { it.avgScore }
            StudentSortOption.MOST_QUIZZES -> list.sortedByDescending { it.quizzesCount }
            StudentSortOption.NAME_AZ -> list.sortedBy { it.name }
        }

        list
    }

    // Pagination calculations
    val totalCount = filteredStudents.size
    val totalPages = maxOf(1, (totalCount + itemsPerPage - 1) / itemsPerPage)
    val safeCurrentPage = currentPage.coerceIn(1, totalPages)
    val startIndex = (safeCurrentPage - 1) * itemsPerPage
    val pageItems = filteredStudents.drop(startIndex).take(itemsPerPage)

    var showCreateBatchDialog by remember { mutableStateOf(false) }
    var showJoinRequestsDialog by remember { mutableStateOf(false) }
    val joinRequestsFlow = remember(teacherId) {
        BatchRepositoryImpl.instance.getJoinRequestsForTeacher(teacherId)
    }
    val joinRequests by joinRequestsFlow.collectAsState(initial = emptyList())
    val pendingRequestsCount = remember(joinRequests) {
        joinRequests.count { it.isPending }
    }

    LaunchedEffect(teacherId) {
        BatchRepositoryImpl.instance.refreshJoinRequests(teacherId)
    }

    var selectedBatchForStudents by remember { mutableStateOf<Batch?>(null) }
    var batchPendingDelete by remember { mutableStateOf<Batch?>(null) }
    var batchPendingEdit by remember { mutableStateOf<Batch?>(null) }
    var editBatchNameInput by remember { mutableStateOf("") }
    var editBatchDescInput by remember { mutableStateOf("") }
    var editBatchAccessMode by remember { mutableStateOf(BatchAccessMode.PUBLIC) }
    var editErrorMessage by remember { mutableStateOf<String?>(null) }
    var isEditingSubmitting by remember { mutableStateOf(false) }

    // Student Detail Dialog
    selectedStudentForDetail?.let { student ->
        StudentDetailDialog(
            student = student,
            onDismiss = { selectedStudentForDetail = null }
        )
    }

    if (showCreateBatchDialog) {
        CreateBatchDialog(
            teacherId = teacherId,
            onDismiss = { showCreateBatchDialog = false },
            onBatchCreated = { showCreateBatchDialog = false }
        )
    }

    if (showJoinRequestsDialog) {
        TeacherJoinRequestsDialog(
            teacherId = teacherId,
            onDismiss = { showJoinRequestsDialog = false }
        )
    }

    selectedBatchForStudents?.let { batch ->
        BatchStudentsDialog(
            batch = batch,
            teacherId = teacherId,
            onDismiss = { selectedBatchForStudents = null },
            onStudentRemoved = { }
        )
    }

    // Delete Confirmation Dialog
    batchPendingDelete?.let { batch ->
        AlertDialog(
            onDismissRequest = { batchPendingDelete = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = if (isTeacherInDarkTheme()) Color(0xFF0F1738) else Color(0xFFFFFFFF),
            title = {
                Text(
                    text = "Delete Batch?",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${batch.name}\"? This action cannot be undone.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val res = BatchRepositoryImpl.instance.deleteBatch(batch.id, teacherId)
                            batchPendingDelete = null
                            if (res.isSuccess) {
                                Toast.makeText(context, "Batch deleted.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { batchPendingDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Edit Batch Dialog (Matches CreateBatchDialog layout & styling)
    batchPendingEdit?.let { batch ->
        Dialog(
            onDismissRequest = { batchPendingEdit = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.92f),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, if (isTeacherInDarkTheme()) Color(0xFF1E2D64) else Color(0xFFE2E8F0)),
                color = if (isTeacherInDarkTheme()) Color(0xFF0A122E) else Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    // Header Row: Top-left icon in gradient circle + Title + Subtitle + Close button
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(PurplePrimary, Color(0xFF6366F1))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Edit Batch",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Update batch information & access mode",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = { batchPendingEdit = null },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isTeacherInDarkTheme()) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = if (isTeacherInDarkTheme()) Color(0xFF1E2D64) else Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Batch Name Input (Reduced corner, leading icon)
                    OutlinedTextField(
                        value = editBatchNameInput,
                        onValueChange = {
                            editBatchNameInput = it
                            editErrorMessage = null
                        },
                        label = { Text("Batch Name (e.g. MCA Semester 1)", color = TextSecondary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = PurplePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = if (isTeacherInDarkTheme()) Color(0xFF1E2D64) else Color(0xFFCBD5E1),
                            focusedContainerColor = if (isTeacherInDarkTheme()) Color(0xFF0A102E) else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isTeacherInDarkTheme()) Color(0xFF0A102E) else Color(0xFFF8FAFC),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description Input (Optional)
                    OutlinedTextField(
                        value = editBatchDescInput,
                        onValueChange = { editBatchDescInput = it },
                        label = { Text("Subject / Description (Optional)", color = TextSecondary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = PurplePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = if (isTeacherInDarkTheme()) Color(0xFF1E2D64) else Color(0xFFCBD5E1),
                            focusedContainerColor = if (isTeacherInDarkTheme()) Color(0xFF0A102E) else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isTeacherInDarkTheme()) Color(0xFF0A102E) else Color(0xFFF8FAFC),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Batch Access Mode Selector
                    Text(
                        text = "Access Mode",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Public Card
                        val isPub = editBatchAccessMode == BatchAccessMode.PUBLIC
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isPub) {
                                        if (isTeacherInDarkTheme()) Color(0xFF0F253E) else Color(0xFFEFF6FF)
                                    } else {
                                        if (isTeacherInDarkTheme()) Color(0xFF070B1F) else Color(0xFFF8FAFC)
                                    }
                                )
                                .border(
                                    BorderStroke(
                                        if (isPub) 1.5.dp else 1.dp,
                                        if (isPub) PurplePrimary else if (isTeacherInDarkTheme()) Color(0xFF1E2B60) else Color(0xFFE2E8F0)
                                    ),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { editBatchAccessMode = BatchAccessMode.PUBLIC }
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
                                        tint = if (isPub) PurplePrimary else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Public",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPub) PurplePrimary else TextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Students join directly with code",
                                    fontSize = 10.5.sp,
                                    color = TextSecondary,
                                    lineHeight = 13.sp
                                )
                            }
                        }

                        // Private Card
                        val isPriv = editBatchAccessMode == BatchAccessMode.PRIVATE
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isPriv) {
                                        if (isTeacherInDarkTheme()) Color(0xFF28183E) else Color(0xFFFAF5FF)
                                    } else {
                                        if (isTeacherInDarkTheme()) Color(0xFF070B1F) else Color(0xFFF8FAFC)
                                    }
                                )
                                .border(
                                    BorderStroke(
                                        if (isPriv) 1.5.dp else 1.dp,
                                        if (isPriv) PurplePrimary else if (isTeacherInDarkTheme()) Color(0xFF1E2B60) else Color(0xFFE2E8F0)
                                    ),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { editBatchAccessMode = BatchAccessMode.PRIVATE }
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
                                        tint = if (isPriv) PurplePrimary else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Private",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPriv) PurplePrimary else TextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Requires teacher approval",
                                    fontSize = 10.5.sp,
                                    color = TextSecondary,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }

                    if (editErrorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = editErrorMessage ?: "",
                            color = Color(0xFFF43F5E),
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
                            onClick = { batchPendingEdit = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel", color = TextSecondary)
                        }

                        Button(
                            onClick = {
                                if (editBatchNameInput.trim().isBlank()) {
                                    editErrorMessage = "Please enter a batch name."
                                    return@Button
                                }
                                isEditingSubmitting = true
                                editErrorMessage = null
                                coroutineScope.launch {
                                    val res = BatchRepositoryImpl.instance.updateBatch(
                                        batchId = batch.id,
                                        teacherId = teacherId,
                                        newName = editBatchNameInput.trim(),
                                        description = editBatchDescInput.trim(),
                                        accessMode = editBatchAccessMode
                                    )
                                    isEditingSubmitting = false
                                    batchPendingEdit = null
                                    if (res.isSuccess) {
                                        Toast.makeText(context, "Batch updated successfully.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to update batch: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isEditingSubmitting,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Top Bar
        item {
            TeacherStudentsTopBar(
                teacherUser = teacherUser,
                onBackClick = onBackClick,
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick
            )
        }

        // 2. Hero Banner Card (Matches Screenshot with 3D Students & Cap)
        item {
            Spacer(modifier = Modifier.height(6.dp))
            TeacherStudentsHeroCard(
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 2.5 Section Selector Tabs: [ 🏫 Batches (N) ] | [ 👥 Directory (N) ]
        item {
            Spacer(modifier = Modifier.height(14.dp))
            TeacherSectionTabs(
                selectedSection = selectedSection,
                onSectionSelected = { selectedSection = it },
                batchesCount = batches.size,
                studentsCount = allStudents.size,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (selectedSection == 0) {
            // ==========================================
            // SECTION A: BATCHES & JOIN CODES
            // ==========================================
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "My Batches",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${batches.size} active batches",
                            fontSize = 12.5.sp,
                            color = TextSecondary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "Join Requests" Pill Button with Amber Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isTeacherInDarkTheme()) Color(0xFF131D45) else Color(0xFFEDE9FE))
                                .border(
                                    1.dp,
                                    if (isTeacherInDarkTheme()) Color(0xFF263775) else Color(0xFFDDD6FE),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { showJoinRequestsDialog = true }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PendingActions,
                                    contentDescription = "Requests",
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Requests",
                                    color = PurplePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                                if (pendingRequestsCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFFF59E0B))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "$pendingRequestsCount",
                                            color = Color.Black,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // "+ Create Batch" Pill Button with Purple Gradient
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
                                    )
                                )
                                .clickable { showCreateBatchDialog = true }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Create",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            if (batches.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardBg)
                            .border(BorderStroke(1.dp, CardBorderColor), RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Batches Created Yet",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Create a batch to generate a unique 6-character code and invite students to your class.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showCreateBatchDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create First Batch", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(batches, key = { it.id }) { batch ->
                    Spacer(modifier = Modifier.height(12.dp))
                    TeacherBatchCard(
                        batch = batch,
                        onViewStudents = { selectedBatchForStudents = it },
                        onEditBatch = { b ->
                            editBatchNameInput = b.name
                            editBatchDescInput = b.description
                            editBatchAccessMode = b.accessMode
                            editErrorMessage = null
                            isEditingSubmitting = false
                            batchPendingEdit = b
                        },
                        onDeleteBatch = { batchPendingDelete = it },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            // ==========================================
            // SECTION B: STUDENT DIRECTORY & METRICS
            // ==========================================
            // 3. Two Overview Metric Cards (Total Students & Avg Performance)
            item {
                Spacer(modifier = Modifier.height(14.dp))
                val avgPerformance = if (allStudents.isNotEmpty()) {
                    allStudents.sumOf { it.avgScore } / allStudents.size
                } else {
                    0
                }
                TeacherStudentsMetricCardsRow(
                    totalCount = allStudents.size,
                    avgPerformance = avgPerformance,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 4. Search Bar & Filter Button Row
            item {
                Spacer(modifier = Modifier.height(14.dp))
                TeacherSearchAndFilterBar(
                    searchQuery = searchQuery,
                    onSearchChange = {
                        searchQuery = it
                        currentPage = 1
                    },
                    selectedSortOption = selectedSortOption,
                    showSortMenu = showSortMenu,
                    onShowSortMenuChange = { showSortMenu = it },
                    onSortOptionSelected = {
                        selectedSortOption = it
                        currentPage = 1
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 5. Student Cards List
            if (pageItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (allStudents.isEmpty()) Icons.Default.Groups else Icons.Default.Search,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (allStudents.isEmpty()) "No Students Enrolled Yet" else "No students match your filter",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (allStudents.isEmpty()) {
                                    "When students join your batches via join codes, they will automatically appear here."
                                } else {
                                    "Try adjusting your search query or sorting filter."
                                },
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(pageItems, key = { it.id }) { student ->
                    Spacer(modifier = Modifier.height(10.dp))
                    StudentCardItem(
                        student = student,
                        onClick = { selectedStudentForDetail = student },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // 7. Pagination Footer
            item {
                Spacer(modifier = Modifier.height(16.dp))
                TeacherStudentsPaginationBar(
                    currentPage = safeCurrentPage,
                    totalPages = totalPages,
                    totalStudentsCount = totalCount,
                    currentShowingStart = if (totalCount > 0) startIndex + 1 else 0,
                    currentShowingEnd = minOf(startIndex + itemsPerPage, totalCount),
                    onPageSelected = { currentPage = it },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 1. Top Bar Component
// -------------------------------------------------------------
@Composable
private fun TeacherStudentsTopBar(
    teacherUser: User?,
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Back button & QuizMaster Logo + Subtitle
        Row(verticalAlignment = Alignment.CenterVertically) {
            val isDark = isTeacherInDarkTheme()
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QuizMasterBrandTitle(
                        fontSize = 21.sp,
                        isDarkTheme = isDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "✦",
                        color = Color(0xFF38BDF8),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Learn • Practice • Succeed",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Right: Notification Bell
        TeacherNotificationButton(
            onClick = onNotificationClick
        )
    }
}

// -------------------------------------------------------------
// 2. Hero Banner Card Component (Exact Match with 3D Students & Cap)
// -------------------------------------------------------------
@Composable
private fun TeacherStudentsHeroCard(
    modifier: Modifier = Modifier
) {
    val isDark = isTeacherInDarkTheme()
    val heroBg = if (isDark) {
        Brush.horizontalGradient(
            listOf(
                Color(0xFF150A4E).copy(alpha = 0.95f),
                Color(0xFF0C1448).copy(alpha = 0.95f),
                Color(0xFF092055).copy(alpha = 0.95f)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                Color(0xFFEFF3FF),
                Color(0xFFE0E7FF),
                Color(0xFFF5F3FF)
            )
        )
    }

    val heroBorder = if (isDark) {
        BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFFC084FC),
                    Color(0xFF818CF8),
                    Color(0xFF38BDF8)
                )
            )
        )
    } else {
        BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF818CF8).copy(alpha = 0.6f),
                    Color(0xFFC7D2FE),
                    Color(0xFFE2E8F0)
                )
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(heroBg)
            .border(
                heroBorder.width,
                heroBorder.brush,
                RoundedCornerShape(22.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Squircle Icon + Title in a Row, followed by description
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Purple Glowing Squircle Icon
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    if (isDark) {
                                        listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
                                    } else {
                                        listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                                    }
                                )
                            )
                            .border(
                                1.dp,
                                if (isDark) Color(0xFFC084FC).copy(alpha = 0.5f) else Color(0xFFA5B4FC).copy(alpha = 0.8f),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = "Students",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Students",
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Manage your students, track progress\nand help them achieve more!",
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            // Right: 3D Student Character at Laptop Illustration
            Box(
                modifier = Modifier
                    .size(width = 145.dp, height = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_student_hero),
                    contentDescription = "Students Learning Illustration",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Overview Metric Cards Row (Total Students & Avg. Performance)
// -------------------------------------------------------------
@Composable
private fun TeacherStudentsMetricCardsRow(
    totalCount: Int,
    avgPerformance: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Total Students
        TeacherStudentMetricCard(
            icon = Icons.Default.Person,
            iconBg = Color(0xFF7C3AED),
            iconTint = Color(0xFFA78BFA),
            value = "$totalCount",
            label = "Total Students",
            modifier = Modifier.weight(1f)
        )

        // 2. Avg. Performance
        TeacherStudentMetricCard(
            icon = Icons.Default.WorkspacePremium,
            iconBg = Color(0xFFD97706),
            iconTint = Color(0xFFFBBF24),
            value = "$avgPerformance%",
            label = "Avg. Performance",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TeacherStudentMetricCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(StatCardBg)
            .border(1.dp, StatBorderColor, RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circular Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconBg.copy(alpha = 0.25f))
                    .border(1.dp, iconTint.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Stat Value
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Stat Label
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -------------------------------------------------------------
// 4. Search and Filter Bar Row
// -------------------------------------------------------------
@Composable
private fun TeacherSearchAndFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedSortOption: StudentSortOption,
    showSortMenu: Boolean,
    onShowSortMenuChange: (Boolean) -> Unit,
    onSortOptionSelected: (StudentSortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search TextField
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            placeholder = {
                Text(
                    text = "Search students by name, email or ID...",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg,
                focusedBorderColor = Color(0xFF3B82F6),
                unfocusedBorderColor = CardBorderColor,
                cursorColor = PurplePrimary
            )
        )

        // Filter Dropdown Button
        Box {
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBg)
                    .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                    .clickable { onShowSortMenuChange(true) }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    text = "Filter",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Dropdown Menu for Sorting & Options
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { onShowSortMenuChange(false) },
                modifier = Modifier
                    .background(CardBg)
                    .border(1.dp, CardBorderColor, RoundedCornerShape(8.dp))
            ) {
                StudentSortOption.values().forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = option.title,
                                    color = if (selectedSortOption == option) Color(0xFF818CF8) else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedSortOption == option) FontWeight.Bold else FontWeight.Normal
                                )
                                if (selectedSortOption == option) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF818CF8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onSortOptionSelected(option)
                            onShowSortMenuChange(false)
                        }
                    )
                }
            }
        }
    }
}



// -------------------------------------------------------------
// 6. Student Card Item Component (Pixel Perfect Match to Screenshot)
// -------------------------------------------------------------
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun StudentCardItem(
    student: StudentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 11.dp, vertical = 7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Student 3D Avatar with circular frame and subtle rim lighting
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isTeacherInDarkTheme()) Color(0xFF0F172A) else Color(0xFFEEF2FF))
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFF818CF8))
                        ),
                        RoundedCornerShape(12.dp)
                    ),
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
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        error = {
                            Image(
                                painter = painterResource(id = student.avatarRes),
                                contentDescription = student.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    )
                } else {
                    Image(
                        painter = painterResource(id = student.avatarRes),
                        contentDescription = student.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Student Name, Status Badge, Email, and ID
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = student.name,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Email
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(top = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = student.email,
                        color = TextSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Badges: Degree Badge + Enrolled Batch Badges
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Grade / Degree Badge
                    if (student.grade.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isTeacherInDarkTheme()) Color(0xFF1E293B) else Color(0xFFEEF2FF))
                                .border(
                                    1.dp,
                                    if (isTeacherInDarkTheme()) Color(0xFF334155) else Color(0xFFC7D2FE),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = student.grade,
                                color = if (isTeacherInDarkTheme()) Color(0xFF93C5FD) else Color(0xFF4F46E5),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }

                    // Enrolled Batch Badges
                    student.batchNames.forEach { bName ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isTeacherInDarkTheme()) Color(0xFF2E1065).copy(alpha = 0.5f) else Color(0xFFF3E8FF))
                                .border(
                                    1.dp,
                                    if (isTeacherInDarkTheme()) Color(0xFF581C87).copy(alpha = 0.7f) else Color(0xFFD8B4FE),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = bName,
                                color = if (isTeacherInDarkTheme()) Color(0xFFD8B4FE) else Color(0xFF7E22CE),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Three Performance Columns matching Screenshot:
            // 1) Avg. Score
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${student.avgScore}%",
                    color = TextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Avg. Score",
                    color = TextSecondary,
                    fontSize = 9.5.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2) Quizzes Count
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${student.quizzesCount}",
                    color = TextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Quizzes",
                    color = TextSecondary,
                    fontSize = 9.5.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 3) Last Active
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Last active",
                    color = TextMuted,
                    fontSize = 9.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = student.lastActive,
                    color = if (isTeacherInDarkTheme()) Color(0xFFCBD5E1) else Color(0xFF334155),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Trailing Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Details",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// 7. Pagination Footer Component (Matches Screenshot)
// -------------------------------------------------------------
@Composable
private fun TeacherStudentsPaginationBar(
    currentPage: Int,
    totalPages: Int,
    totalStudentsCount: Int,
    currentShowingStart: Int,
    currentShowingEnd: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Page buttons: < [1] [2] [3] [4] [5] >
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Previous button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isTeacherInDarkTheme()) Color(0xFF0F172A) else Color(0xFFFFFFFF))
                    .border(1.dp, CardBorderColor, RoundedCornerShape(6.dp))
                    .clickable(enabled = currentPage > 1) {
                        onPageSelected(currentPage - 1)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Page",
                    tint = if (currentPage > 1) TextPrimary else TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Visible Page Numbers (1..minOf(5, totalPages))
            val maxVisiblePages = minOf(5, totalPages)
            for (page in 1..maxVisiblePages) {
                val isSelected = page == currentPage
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected) Color(0xFF7C3AED) else if (isTeacherInDarkTheme()) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFFA78BFA) else CardBorderColor,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onPageSelected(page) }
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$page",
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // Next button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isTeacherInDarkTheme()) Color(0xFF0F172A) else Color(0xFFFFFFFF))
                    .border(1.dp, CardBorderColor, RoundedCornerShape(6.dp))
                    .clickable(enabled = currentPage < totalPages) {
                        onPageSelected(currentPage + 1)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Page",
                    tint = if (currentPage < totalPages) TextPrimary else TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Showing Count Text
        Text(
            text = "Showing $currentShowingStart-$currentShowingEnd of $totalStudentsCount students",
            color = TextMuted,
            fontSize = 10.5.sp
        )
    }
}

// -------------------------------------------------------------
// 8. Student Detail Dialog (Interactive Inspection)
// -------------------------------------------------------------
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun StudentDetailDialog(
    student: StudentItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = if (isTeacherInDarkTheme()) Color(0xFF0F1738) else Color(0xFFFFFFFF),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                // Header with Avatar and Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp)),
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
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop,
                                error = {
                                    Image(
                                        painter = painterResource(id = student.avatarRes),
                                        contentDescription = student.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            )
                        } else {
                            Image(
                                painter = painterResource(id = student.avatarRes),
                                contentDescription = student.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = student.name,
                            color = TextPrimary,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = student.email,
                                color = TextSecondary,
                                fontSize = 12.5.sp
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (student.grade.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(if (isTeacherInDarkTheme()) Color(0xFF1E293B) else Color(0xFFEEF2FF))
                                        .border(
                                            1.dp,
                                            if (isTeacherInDarkTheme()) Color(0xFF334155) else Color(0xFFC7D2FE),
                                            RoundedCornerShape(5.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = student.grade,
                                        color = if (isTeacherInDarkTheme()) Color(0xFF93C5FD) else Color(0xFF4F46E5),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            student.batchNames.forEach { bName ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(if (isTeacherInDarkTheme()) Color(0xFF2E1065).copy(alpha = 0.5f) else Color(0xFFF3E8FF))
                                        .border(
                                            1.dp,
                                            if (isTeacherInDarkTheme()) Color(0xFF581C87).copy(alpha = 0.7f) else Color(0xFFD8B4FE),
                                            RoundedCornerShape(5.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = bName,
                                        color = if (isTeacherInDarkTheme()) Color(0xFFD8B4FE) else Color(0xFF7E22CE),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Performance Highlights Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isTeacherInDarkTheme()) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                        .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${student.avgScore}%", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Average", color = TextSecondary, fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${student.quizzesCount}", color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Completed", color = TextSecondary, fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${student.accuracy}%", color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Accuracy", color = TextSecondary, fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("#${student.rank}", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Class Rank", color = TextSecondary, fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Activity Info
                Text(
                    text = "Recent Activity",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (student.quizzesCount > 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        StudentHistoryRow("Completed Quizzes", "Avg: ${student.avgScore}%", "Active", student.avgScore >= 60)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isTeacherInDarkTheme()) Color(0xFF080E2A) else Color(0xFFF1F5F9))
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No quiz attempts recorded yet",
                            color = TextMuted,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun StudentHistoryRow(
    title: String,
    score: String,
    time: String,
    passed: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isTeacherInDarkTheme()) Color(0xFF080E2A) else Color(0xFFF1F5F9))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, color = TextPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
            Text(time, color = TextMuted, fontSize = 9.5.sp)
        }
        Text(
            score,
            color = if (passed) Color(0xFF34D399) else Color(0xFFEF4444),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
