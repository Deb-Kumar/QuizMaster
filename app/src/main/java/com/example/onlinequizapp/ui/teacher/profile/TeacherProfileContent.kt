package com.example.onlinequizapp.ui.teacher.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.WorkspacePremium
import com.example.onlinequizapp.data.repository.QuizRepositoryImpl
import com.example.onlinequizapp.ui.components.QuizMasterBrandTitle
import com.example.onlinequizapp.ui.components.UserAvatar
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.onlinequizapp.ui.home.profile.AboutUsDialog
import com.example.onlinequizapp.ui.home.profile.ChangePasswordDialog
import com.example.onlinequizapp.ui.home.profile.DeleteAccountDialog
import com.example.onlinequizapp.ui.home.profile.FeedbackRemarksDialog
import com.example.onlinequizapp.ui.home.profile.HelpSupportDialog
import com.example.onlinequizapp.ui.home.profile.LogoutConfirmDialog
import com.example.onlinequizapp.ui.home.profile.PrivacyPolicyDialog
import com.example.onlinequizapp.ui.home.profile.ProfilePictureOptionsDialog
import com.example.onlinequizapp.ui.home.profile.ThemeDialog
import com.example.onlinequizapp.ui.home.profile.ViewProfilePictureDialog
import com.example.onlinequizapp.ui.home.profile.copyUriToInternalStorage
import com.example.onlinequizapp.ui.home.profile.saveBitmapToInternalStorage
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme
import com.example.onlinequizapp.ui.home.LocalQuizDark
import com.example.onlinequizapp.ui.theme.ThemeManager
import com.example.onlinequizapp.ui.theme.ThemeMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.R
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.ui.components.showToast
import com.example.onlinequizapp.ui.teacher.TeacherCardBg
import com.example.onlinequizapp.ui.teacher.TeacherCardBorder
import com.example.onlinequizapp.ui.teacher.TeacherDashboardUiState
import com.example.onlinequizapp.ui.teacher.TeacherTextMuted
import com.example.onlinequizapp.ui.teacher.TeacherTextWhite
import com.example.onlinequizapp.ui.teacher.components.TeacherMetricCard
import com.example.onlinequizapp.ui.teacher.components.TeacherNotificationButton

// -------------------------------------------------------------
// 3. Tab: Teacher Profile Content (Exact Match to Screenshot)
// -------------------------------------------------------------
@Composable
internal fun TeacherProfileContent(
    user: User?,
    stats: TeacherDashboardUiState,
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onCreateQuizClick: () -> Unit,
    onManageStudentsClick: () -> Unit,
    onViewAnalyticsClick: () -> Unit,
    onCertificatesClick: () -> Unit,
    onMyQuizzesClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onUpdateProfile: (name: String, university: String, department: String, course: String, avatarUrl: String?, email: String?, contactNumber: String?, onResult: (Boolean) -> Unit) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onSetOrChangePassword: ((currentPassword: String?, newPassword: String, onResult: (Result<User>) -> Unit) -> Unit)? = null,
    onDeleteAccount: ((onResult: (Boolean) -> Unit) -> Unit)? = null,
    onSubmitFeedback: ((rating: Int, category: String, comment: String, onResult: (Boolean) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val teacherName = user?.name?.ifBlank { "No User Name" } ?: "No User Name"
    val teacherEmail = user?.email?.ifBlank { "nouser@quizmaster.edu" } ?: "nouser@quizmaster.edu"
    val teacherUniversity = user?.displayUniversity?.ifBlank { "University not set" } ?: "University not set"
    val teacherDesignation = user?.teacherProfile?.designation?.takeIf { it.isNotBlank() }
        ?: user?.course?.takeIf { it.isNotBlank() && it != "Department no set" }
        ?: "Designation not set"
    var showPersonalInfoDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showAllAchievementsDialog by remember { mutableStateOf(false) }

    // Profile Picture Workflow State & Launchers (Reusing Student parity workflow)
    var showProfilePictureOptionsDialog by remember { mutableStateOf(false) }
    var showViewProfilePictureDialog by remember { mutableStateOf(false) }

    // Camera capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val savedUri = saveBitmapToInternalStorage(context, bitmap, user?.id ?: "teacher")
            if (savedUri != null) {
                onUpdateProfile(
                    user?.name ?: "",
                    user?.displayUniversity ?: "",
                    user?.displayDepartment ?: "",
                    user?.displayCourse ?: "",
                    savedUri,
                    user?.email,
                    user?.contactNumber
                ) { success ->
                    if (success) {
                        context.showToast("Profile picture updated! ✨")
                    } else {
                        context.showToast("Failed to update profile picture.")
                    }
                }
            } else {
                context.showToast("Failed to process captured photo.")
            }
        }
    }

    // Gallery upload launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val savedUri = copyUriToInternalStorage(context, uri, user?.id ?: "teacher")
            if (savedUri != null) {
                onUpdateProfile(
                    user?.name ?: "",
                    user?.displayUniversity ?: "",
                    user?.displayDepartment ?: "",
                    user?.displayCourse ?: "",
                    savedUri,
                    user?.email,
                    user?.contactNumber
                ) { success ->
                    if (success) {
                        context.showToast("Profile picture updated! ✨")
                    } else {
                        context.showToast("Failed to update profile picture.")
                    }
                }
            } else {
                context.showToast("Failed to process selected image.")
            }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            context.showToast("Camera permission is required to capture photo.")
        }
    }

    // Student-parity Modals
    var showThemeDialog by remember { mutableStateOf(false) }
    var showHelpSupportDialog by remember { mutableStateOf(false) }
    var showAboutUsDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    val isDark = isTeacherInDarkTheme()
    CompositionLocalProvider(LocalQuizDark provides isDark) {
        if (showPersonalInfoDialog) {
            TeacherPersonalInfoDialog(
                user = user,
                onDismiss = { showPersonalInfoDialog = false },
                onSave = { name, university, department, course, avatarUrl, email, contactNumber, onResult ->
                    onUpdateProfile(name, university, department, course, avatarUrl, email, contactNumber) { success ->
                        onResult(success)
                        if (success) {
                            context.showToast("Personal information updated! ✨")
                        } else {
                            context.showToast("Failed to update personal information.")
                        }
                    }
                }
            )
        }

        if (showSettingsDialog) {
            TeacherSettingsDialog(
                onDismiss = { showSettingsDialog = false }
            )
        }

        if (showHelpDialog) {
            TeacherHelpDialog(onDismiss = { showHelpDialog = false })
        }

        if (showAllAchievementsDialog) {
            TeacherAchievementsDialog(
                stats = stats,
                onDismiss = { showAllAchievementsDialog = false }
            )
        }
        if (showChangePasswordDialog) {
            ChangePasswordDialog(
                isFirstTimeSetup = user?.needsPasswordSetup == true,
                onDismiss = { showChangePasswordDialog = false },
                onSubmit = { currentPass, newPass, onResult ->
                    if (onSetOrChangePassword != null) {
                        onSetOrChangePassword(currentPass, newPass, onResult)
                    } else {
                        onResult(Result.failure(Exception("Password service unavailable.")))
                    }
                }
            )
        }

        if (showThemeDialog) {
            ThemeDialog(
                onDismiss = { showThemeDialog = false },
                onThemeSelected = { mode ->
                    ThemeManager.setTheme(context, mode)
                    val label = when (mode) {
                        ThemeMode.DARK -> "Dark"
                        ThemeMode.LIGHT -> "Light"
                        ThemeMode.SYSTEM -> "System default"
                    }
                    context.showToast("Theme changed to $label 🎨")
                    showThemeDialog = false
                }
            )
        }

        if (showHelpSupportDialog) {
            HelpSupportDialog(
                onDismiss = { showHelpSupportDialog = false }
            )
        }

        if (showAboutUsDialog) {
            AboutUsDialog(
                onDismiss = { showAboutUsDialog = false }
            )
        }

        if (showPrivacyPolicyDialog) {
            PrivacyPolicyDialog(
                onDismiss = { showPrivacyPolicyDialog = false }
            )
        }

        if (showFeedbackDialog) {
            val coroutineScope = rememberCoroutineScope()
            FeedbackRemarksDialog(
                user = user,
                onDismiss = { showFeedbackDialog = false },
                onSubmit = { rating, category, comment, onResult ->
                    if (onSubmitFeedback != null) {
                        onSubmitFeedback(rating, category, comment, onResult)
                    } else {
                        coroutineScope.launch {
                            val success = QuizRepositoryImpl.instance.submitFeedback(
                                userId = user?.id ?: "anonymous",
                                userName = user?.name ?: "Teacher",
                                userEmail = user?.email ?: "teacher@quizmaster.com",
                                role = user?.role?.label ?: "Teacher",
                                rating = rating,
                                category = category,
                                comment = comment
                            )
                            onResult(success)
                        }
                    }
                }
            )
        }

        if (showDeleteAccountDialog) {
            DeleteAccountDialog(
                onDismiss = { showDeleteAccountDialog = false },
                onConfirmDelete = {
                    onDeleteAccount?.invoke { success ->
                        if (success) {
                            showDeleteAccountDialog = false
                            context.showToast("Account deleted successfully.")
                            onLogoutClick()
                        } else {
                            context.showToast("Failed to delete account. Please try again.")
                        }
                    } ?: run {
                        showDeleteAccountDialog = false
                        context.showToast("Account deleted.")
                        onLogoutClick()
                    }
                }
            )
        }

        if (showLogoutConfirmDialog) {
            LogoutConfirmDialog(
                onDismiss = { showLogoutConfirmDialog = false },
                onConfirm = {
                    showLogoutConfirmDialog = false
                    context.showToast("Signed out successfully")
                    onLogoutClick()
                }
            )
        }

        // Profile Picture Dialogs (Exact parity with student workflow)
        if (showProfilePictureOptionsDialog) {
            ProfilePictureOptionsDialog(
                user = user,
                defaultDrawableRes = R.drawable.img_teacher_hero,
                onDismiss = { showProfilePictureOptionsDialog = false },
                onSeePictureClick = {
                    showProfilePictureOptionsDialog = false
                    showViewProfilePictureDialog = true
                },
                onCaptureFromCameraClick = {
                    showProfilePictureOptionsDialog = false
                    val hasCameraPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasCameraPermission) {
                        cameraLauncher.launch(null)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                onUploadFromGalleryClick = {
                    showProfilePictureOptionsDialog = false
                    galleryLauncher.launch("image/*")
                }
            )
        }

        if (showViewProfilePictureDialog) {
            ViewProfilePictureDialog(
                user = user,
                defaultDrawableRes = R.drawable.img_teacher_hero,
                onDismiss = { showViewProfilePictureDialog = false },
                onChangePhotoClick = {
                    showViewProfilePictureDialog = false
                    showProfilePictureOptionsDialog = true
                }
            )
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = TeacherTextWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color(0xFFA855F7),
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    QuizMasterBrandTitle(
                        fontSize = 22.sp,
                        isDarkTheme = isDark
                    )
                }

                // Standardized Notification Bell with Badge
                TeacherNotificationButton(
                    onClick = onNotificationClick
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Hero Profile Card
            val heroProfileBg = if (isDark) {
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF1E1B4B),
                        Color(0xFF172554),
                        Color(0xFF0F172A)
                    )
                )
            } else {
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFEDE9FE),
                        Color(0xFFE0E7FF),
                        Color(0xFFF1F5F9)
                    )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(heroProfileBg)
                    .border(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.35f) else Color(0xFFC7D2FE), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Teacher Avatar with Camera Badge
                Box(
                    modifier = Modifier.size(84.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(
                                2.dp,
                                Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF38BDF8),
                                        Color(0xFF818CF8),
                                        Color(0xFFA855F7),
                                        Color(0xFF38BDF8)
                                    )
                                ),
                                CircleShape
                            )
                            .clickable { showProfilePictureOptionsDialog = true }
                    ) {
                        UserAvatar(
                            avatarUrl = user?.avatarUrl,
                            name = teacherName,
                            size = 80.dp,
                            defaultDrawableRes = R.drawable.img_teacher_hero
                        )
                    }

                    // Camera Badge Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7))
                            .border(1.5.dp, if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF), CircleShape)
                            .clickable { showProfilePictureOptionsDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Name, Badge, Email & Quote
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = teacherName,
                            color = TeacherTextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Teacher Badge (shifted to right side like student profile card)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF7C3AED))
                                .padding(horizontal = 8.dp, vertical = 2.5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Teacher",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Email
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = teacherEmail,
                                color = if (isDark) Color(0xFFA5B4FC) else Color(0xFF334155),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Contact number (ONLY if user actually set their contact number)
                        val teacherContact = user?.contactNumber?.takeIf { it.isNotBlank() }
                        if (teacherContact != null) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Contact Number",
                                    tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = teacherContact,
                                    color = if (isDark) Color(0xFFA5B4FC) else Color(0xFF334155),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (teacherDesignation.isNotBlank()) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF818CF8) else Color(0xFF6366F1),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = teacherDesignation,
                                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (teacherUniversity.isNotBlank()) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = teacherUniversity,
                                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Metrics Row (4 Cards)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TeacherMetricCard(
                icon = Icons.Default.Groups,
                iconBgColor = Color(0xFF7C3AED).copy(alpha = 0.25f),
                iconTintColor = Color(0xFFA78BFA),
                value = "${stats.totalStudents}",
                label = "Total Students",
                modifier = Modifier.weight(1f)
            )
            TeacherMetricCard(
                icon = Icons.AutoMirrored.Filled.Assignment,
                iconBgColor = Color(0xFF2563EB).copy(alpha = 0.25f),
                iconTintColor = Color(0xFF60A5FA),
                value = "${stats.totalQuizzes}",
                label = "Quizzes Created",
                modifier = Modifier.weight(1f)
            )
            TeacherMetricCard(
                icon = Icons.Default.CheckCircle,
                iconBgColor = Color(0xFF059669).copy(alpha = 0.25f),
                iconTintColor = Color(0xFF34D399),
                value = "${stats.totalSubmissions}",
                label = "Total Attempts",
                modifier = Modifier.weight(1f)
            )
            TeacherMetricCard(
                icon = Icons.Default.Star,
                iconBgColor = Color(0xFFD97706).copy(alpha = 0.25f),
                iconTintColor = Color(0xFFFBBF24),
                value = "${stats.averageScore}%",
                label = "Avg. Score",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Menu List Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(TeacherCardBg)
                .border(1.dp, TeacherCardBorder, RoundedCornerShape(18.dp))
        ) {
            TeacherProfileMenuItem(
                icon = Icons.Default.Person,
                iconBg = Color(0xFF2563EB),
                title = "Personal Information",
                subtitle = "View and update your academic details",
                onClick = { showPersonalInfoDialog = true }
            )

            TeacherMenuDivider()

            val needsPasswordSetup = user?.needsPasswordSetup == true
            TeacherProfileMenuItem(
                icon = if (needsPasswordSetup) Icons.Default.Key else Icons.Default.Security,
                iconBg = if (needsPasswordSetup) Color(0xFFEF4444) else Color(0xFF059669),
                title = if (needsPasswordSetup) "Set Your Password" else "Change Password",
                subtitle = if (needsPasswordSetup) "Action required • Protect your account" else "Keep your account secure",
                hasBadge = needsPasswordSetup,
                badgeText = if (needsPasswordSetup) "Action Needed" else null,
                onClick = { showChangePasswordDialog = true }
            )

            TeacherMenuDivider()


            TeacherProfileMenuItem(
                icon = Icons.Default.Settings,
                iconBg = Color(0xFF64748B),
                title = "Settings",
                subtitle = "Account settings & security",
                onClick = { showSettingsDialog = true }
            )
            TeacherMenuDivider()

            // Appearance
            TeacherProfileMenuItem(
                icon = Icons.Default.Palette,
                iconBg = Color(0xFFDB2777),
                title = "Appearance",
                subtitle = "Light / Dark mode",
                onClick = { showThemeDialog = true }
            )
            TeacherMenuDivider()

            // Help & Support
            TeacherProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                iconBg = Color(0xFF0284C7),
                title = "Help & Support",
                subtitle = "Get help when you need it",
                onClick = { showHelpSupportDialog = true }
            )
            TeacherMenuDivider()

            // About Us
            TeacherProfileMenuItem(
                icon = Icons.Default.Info,
                iconBg = Color(0xFF6366F1),
                title = "About Us",
                subtitle = "Version, mission & features",
                onClick = { showAboutUsDialog = true }
            )
            TeacherMenuDivider()

            // Privacy Policy
            TeacherProfileMenuItem(
                icon = Icons.Default.Policy,
                iconBg = Color(0xFF0EA5E9),
                title = "Privacy Policy",
                subtitle = "Data security, privacy & terms",
                onClick = { showPrivacyPolicyDialog = true }
            )
            TeacherMenuDivider()

            // Feedback and remarks
            TeacherProfileMenuItem(
                icon = Icons.Default.RateReview,
                iconBg = Color(0xFFF59E0B),
                title = "Feedback and remarks",
                subtitle = "Share your rating, opinion & issues",
                onClick = { showFeedbackDialog = true }
            )
            TeacherMenuDivider()

            // Delete Account
            TeacherProfileMenuItem(
                icon = Icons.Default.DeleteForever,
                iconBg = Color(0xFFDC2626),
                title = "Delete Account",
                subtitle = "Permanently remove your account & data",
                onClick = { showDeleteAccountDialog = true }
            )
            TeacherMenuDivider()

            // Log Out
            TeacherProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.Logout,
                iconBg = Color(0xFF991B1B),
                title = "Log Out",
                subtitle = "Sign out from your account",
                onClick = { showLogoutConfirmDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 6. Achievements Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF78350F).copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(19.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Achievements",
                    color = TeacherTextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Text(
                text = "View All >",
                color = Color(0xFF60A5FA),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { showAllAchievementsDialog = true }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Glassmorphic Achievements Container
        val achievementsGlassBg = if (isDark) {
            Brush.linearGradient(
                listOf(
                    Color(0xFF0F172A).copy(alpha = 0.70f),
                    Color(0xFF1E1B4B).copy(alpha = 0.45f),
                    Color(0xFF020617).copy(alpha = 0.60f)
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.85f),
                    Color(0xFFEEF2FF).copy(alpha = 0.65f),
                    Color(0xFFF8FAFC).copy(alpha = 0.80f)
                )
            )
        }
        val achievementsGlassBorder = if (isDark) {
            Brush.linearGradient(
                listOf(
                    Color(0xFFF59E0B).copy(alpha = 0.35f),
                    Color(0xFF818CF8).copy(alpha = 0.25f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    Color(0xFFF59E0B).copy(alpha = 0.45f),
                    Color(0xFF818CF8).copy(alpha = 0.30f),
                    Color(0xFFCBD5E1)
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(achievementsGlassBg)
                .border(1.dp, achievementsGlassBorder, RoundedCornerShape(20.dp))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TeacherAchievementBadgeCard(
                    icon = Icons.Default.Star,
                    badgeColor = Color(0xFF8B5CF6),
                    title = "Quiz Creator",
                    description = "Created 10 quizzes",
                    modifier = Modifier.weight(1f)
                )
                TeacherAchievementBadgeCard(
                    icon = Icons.Default.Groups,
                    badgeColor = Color(0xFF06B6D4),
                    title = "Student Champion",
                    description = "50+ students",
                    modifier = Modifier.weight(1f)
                )
                TeacherAchievementBadgeCard(
                    icon = Icons.Default.WorkspacePremium,
                    badgeColor = Color(0xFF10B981),
                    title = "Top Educator",
                    description = "Avg. score 80%+",
                    modifier = Modifier.weight(1f)
                )
                TeacherAchievementBadgeCard(
                    icon = Icons.Default.MilitaryTech,
                    badgeColor = Color(0xFFEC4899),
                    title = "Certificate Pro",
                    description = "Issued 5+ certificates",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 7. Bottom Fun & Competitive Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF6366F1),
                            Color(0xFF2563EB),
                            Color(0xFF0284C7)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF4338CA).copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrackChanges,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Make Learning Fun & Competitive",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Create quizzes, track progress, and help students grow!",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Image(
                    painter = painterResource(id = R.drawable.img_books_cap),
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
}
