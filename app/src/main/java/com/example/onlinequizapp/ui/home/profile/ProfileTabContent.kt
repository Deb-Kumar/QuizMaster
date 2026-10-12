package com.example.onlinequizapp.ui.home.profile

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.example.onlinequizapp.data.repository.QuizRepositoryImpl
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.R
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.domain.repository.UserStats
import com.example.onlinequizapp.ui.components.QuizMasterBrandTitle
import com.example.onlinequizapp.ui.components.UserAvatar
import com.example.onlinequizapp.ui.components.showToast
import com.example.onlinequizapp.ui.home.HomeBrandGradient
import com.example.onlinequizapp.ui.home.DarkCardBorder
import com.example.onlinequizapp.ui.home.DarkCardSurface
import com.example.onlinequizapp.ui.home.LocalQuizDark
import com.example.onlinequizapp.ui.home.TextWhite
import com.example.onlinequizapp.ui.theme.ThemeManager
import com.example.onlinequizapp.ui.theme.ThemeMode

@Composable
internal fun ProfileTabContent(
    user: User?,
    stats: UserStats,
    onBackToHome: () -> Unit,
    onNotificationClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onUpdateProfile: (name: String, university: String, department: String, course: String, avatarUrl: String?, email: String?, contactNumber: String?, onResult: (Boolean) -> Unit) -> Unit,
    onSetOrChangePassword: (currentPassword: String?, newPassword: String, onResult: (Result<User>) -> Unit) -> Unit,
    onDeleteAccount: ((onResult: (Boolean) -> Unit) -> Unit)? = null,
    onSubmitFeedback: ((rating: Int, category: String, comment: String, onResult: (Boolean) -> Unit) -> Unit)? = null,
    onCertificatesClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showProfilePictureOptionsDialog by remember { mutableStateOf(false) }
    var showViewProfilePictureDialog by remember { mutableStateOf(false) }

    // Camera capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val savedUri = saveBitmapToInternalStorage(context, bitmap, user?.id ?: "student")
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
            val savedUri = copyUriToInternalStorage(context, uri, user?.id ?: "student")
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
    var showPersonalInfoDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showHelpSupportDialog by remember { mutableStateOf(false) }
    var showAboutUsDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showAchievementsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Bar: Back Arrow, "QuizMaster" Logo, Notification Bell
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = onBackToHome,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = TextWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Brand Title "QuizMaster"
                QuizMasterBrandTitle(isDarkTheme = LocalQuizDark.current)

                // Notification Bell with Red Dot
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (LocalQuizDark.current) Color(0xFF131B38) else Color(0xFFE2E8F0))
                        .clickable(role = Role.Button, onClick = onNotificationClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-6).dp, y = 6.dp)
                            .background(Color(0xFFEF4444), CircleShape)
                    )
                }
            }
        }

        // 2. Student Profile Card (Dynamic Theme: Midnight Indigo in Dark Mode, Pristine White in Light Mode)
        item {
            val isDark = LocalQuizDark.current
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .then(
                        if (isDark) {
                            Modifier
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF131B46),
                                            Color(0xFF1B245C),
                                            Color(0xFF131B46)
                                        )
                                    )
                                )
                                .border(
                                    1.dp,
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF38BDF8).copy(alpha = 0.5f),
                                            Color(0xFF818CF8).copy(alpha = 0.6f)
                                        )
                                    ),
                                    RoundedCornerShape(22.dp)
                                )
                        } else {
                            Modifier
                                .background(DarkCardSurface)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(22.dp))
                        }
                    )
                    .padding(18.dp)
            ) {
                // Profile Content Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar with glowing neon ring and camera button
                    Box(
                        modifier = Modifier.size(86.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.5.dp,
                                    brush = Brush.sweepGradient(
                                        listOf(
                                            Color(0xFF38BDF8),
                                            Color(0xFF818CF8),
                                            Color(0xFFC084FC),
                                            Color(0xFF38BDF8)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .padding(3.dp)
                                .clickable { showProfilePictureOptionsDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            UserAvatar(
                                avatarUrl = user?.avatarUrl,
                                name = user?.name ?: "Dev Kumar",
                                size = 80.dp,
                                defaultDrawableRes = R.drawable.img_student_hero
                            )
                        }

                        // Camera overlay button
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB))
                                .border(2.dp, if (isDark) Color(0xFF131B46) else Color.White, CircleShape)
                                .clickable { showProfilePictureOptionsDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change Photo",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Text Info
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = user?.name?.takeIf { it.isNotBlank() } ?: "Dev Kumar",
                                modifier = Modifier.weight(1f, fill = false),
                                style = TextStyle(
                                    color = if (isDark) Color.White else TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // Role Pill Badge (shifted to right side)
                            val roleLabel = user?.role?.label ?: "Student"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF2563EB))
                                    .padding(horizontal = 7.dp, vertical = 2.5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎓", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = roleLabel,
                                        style = TextStyle(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Email & Contact Number side-by-side with icons
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
                                    contentDescription = "Email",
                                    tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = user?.email?.takeIf { it.isNotBlank() } ?: "devkumar@email.com",
                                    style = TextStyle(
                                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Contact number with icon (ONLY if user actually set their contact number)
                            val contactNum = user?.contactNumber?.takeIf { it.isNotBlank() }
                            if (contactNum != null) {
                                Row(
                                    modifier = Modifier.weight(1f, fill = false),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Contact Number",
                                        tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = contactNum,
                                        style = TextStyle(
                                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = user?.displayUniversity?.takeIf { it.isNotBlank() } ?: "Delhi University",
                                style = TextStyle(
                                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Department & Course side-by-side below University
                        val deptName = user?.displayDepartment?.takeIf { it.isNotBlank() } ?: "Computer Science"
                        val courseName = user?.displayCourse?.takeIf { it.isNotBlank() } ?: "B.Tech CS"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Department chip
                            Row(
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                                    .border(
                                        0.7.dp,
                                        if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apartment,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = deptName,
                                    style = TextStyle(
                                        color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Course chip
                            Row(
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                                    .border(
                                        0.7.dp,
                                        if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = courseName,
                                    style = TextStyle(
                                        color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Stats Row (4 metrics in 1 unified dark card with vertical dividers)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                    .padding(vertical = 14.dp, horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quizzes Taken
                    ProfileStatColumn(
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        iconBg = Color(0x338B5CF6),
                        iconTint = Color(0xFFA78BFA),
                        value = "${stats.quizzesTaken}",
                        label = "Quizzes Taken",
                        modifier = Modifier.weight(1f)
                    )

                    // Divider
                    Box(modifier = Modifier.width(1.dp).height(34.dp).background(DarkCardBorder))

                    // Certificates
                    ProfileStatColumn(
                        icon = Icons.Default.EmojiEvents,
                        iconBg = Color(0x33F59E0B),
                        iconTint = Color(0xFFF59E0B),
                        value = "${stats.certificatesCount}",
                        label = "Certificates",
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (onCertificatesClick != null) {
                                    Modifier.clickable { onCertificatesClick() }
                                } else Modifier
                            )
                    )

                    // Divider
                    Box(modifier = Modifier.width(1.dp).height(34.dp).background(DarkCardBorder))

                    // Avg Score
                    ProfileStatColumn(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconBg = Color(0x3310B981),
                        iconTint = Color(0xFF10B981),
                        value = "${stats.averageScorePercentage}%",
                        label = "Avg Score",
                        modifier = Modifier.weight(1f)
                    )

                    // Divider
                    Box(modifier = Modifier.width(1.dp).height(34.dp).background(DarkCardBorder))

                    // Day Streak
                    ProfileStatColumn(
                        icon = Icons.Default.LocalFireDepartment,
                        iconBg = Color(0x33F43F5E),
                        iconTint = Color(0xFFF43F5E),
                        value = "${stats.dayStreak}",
                        label = "Day Streak",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Achievements Section (Enclosed in a Unified Rounded Container Card)
        item {
            val isDark = LocalQuizDark.current
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isDark) Color(0xFF09112E) else Color.White)
                    .border(
                        1.dp,
                        if (isDark) Color(0xFF1B2856) else Color(0xFFE2E8F0),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Row: 🏆 Achievements + View All >
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🏆", fontSize = 19.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Achievements",
                                style = TextStyle(
                                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showAchievementsDialog = true }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "View All",
                                style = TextStyle(
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "View All",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // 4 Achievement Badges in a Row (Inside the Container)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val firstQuizUnlocked = stats.quizzesTaken >= 1
                        val streakUnlocked = stats.dayStreak >= 1
                        val certUnlocked = stats.certificatesCount >= 1
                        val championUnlocked = stats.quizzesTaken >= 10

                        val lockedBadgeBg = if (isDark) {
                            Brush.linearGradient(listOf(Color(0xFF141D3E), Color(0xFF0F172A)))
                        } else {
                            Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)))
                        }
                        val lockedIconTint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)

                        // Badge 1: First Quiz
                        ProfileAchievementCard(
                            title = "First Quiz",
                            subtitle = if (firstQuizUnlocked) "Completed" else "Complete 1 quiz",
                            icon = if (firstQuizUnlocked) Icons.Default.Star else Icons.Default.Lock,
                            iconTint = if (firstQuizUnlocked) Color(0xFFFFD700) else lockedIconTint,
                            badgeBg = if (firstQuizUnlocked) {
                                Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF6366F1), Color(0xFF8B5CF6)))
                            } else {
                                lockedBadgeBg
                            },
                            isLocked = !firstQuizUnlocked,
                            onClick = { showAchievementsDialog = true },
                            modifier = Modifier.weight(1f)
                        )

                        // Badge 2: Day Streak
                        ProfileAchievementCard(
                            title = "Day Streak",
                            subtitle = if (streakUnlocked) "${stats.dayStreak}d streak" else "Start a streak",
                            icon = if (streakUnlocked) Icons.Default.LocalFireDepartment else Icons.Default.Lock,
                            iconTint = if (streakUnlocked) Color.White else lockedIconTint,
                            badgeBg = if (streakUnlocked) {
                                Brush.linearGradient(listOf(Color(0xFFF43F5E), Color(0xFFE11D48)))
                            } else {
                                lockedBadgeBg
                            },
                            isLocked = !streakUnlocked,
                            onClick = { showAchievementsDialog = true },
                            modifier = Modifier.weight(1f)
                        )

                        // Badge 3: Scholar (Certificate)
                        ProfileAchievementCard(
                            title = "Scholar",
                            subtitle = if (certUnlocked) "${stats.certificatesCount} earned" else "Score 80%+",
                            icon = if (certUnlocked) Icons.Default.CardMembership else Icons.Default.Lock,
                            iconTint = if (certUnlocked) Color.White else lockedIconTint,
                            badgeBg = if (certUnlocked) {
                                Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF0EA5E9), Color(0xFF38BDF8)))
                            } else {
                                lockedBadgeBg
                            },
                            isLocked = !certUnlocked,
                            onClick = { showAchievementsDialog = true },
                            modifier = Modifier.weight(1f)
                        )

                        // Badge 4: Champion (10 quizzes)
                        ProfileAchievementCard(
                            title = "Champion",
                            subtitle = if (championUnlocked) "Completed" else "${stats.quizzesTaken}/10 quizzes",
                            icon = if (championUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                            iconTint = if (championUnlocked) Color(0xFFFFD700) else lockedIconTint,
                            badgeBg = if (championUnlocked) {
                                Brush.linearGradient(listOf(Color(0xFFD97706), Color(0xFFF59E0B)))
                            } else {
                                lockedBadgeBg
                            },
                            isLocked = !championUnlocked,
                            onClick = { showAchievementsDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 5. Account Settings Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Account Settings",
                    style = TextStyle(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkCardSurface)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // 1. Personal Information
                        SettingsRowItem(
                            icon = Icons.Default.Person,
                            iconBg = Color(0xFF2563EB),
                            title = "Personal Information",
                            subtitle = "Update your details",
                            onClick = { showPersonalInfoDialog = true }
                        )

                        SettingsDivider()

                        // 2. Change / Set Password
                        val needsPasswordSetup = user?.needsPasswordSetup == true
                        SettingsRowItem(
                            icon = if (needsPasswordSetup) Icons.Default.Key else Icons.Default.Security,
                            iconBg = if (needsPasswordSetup) Color(0xFFEF4444) else Color(0xFF059669),
                            title = if (needsPasswordSetup) "Set Your Password" else "Change Password",
                            subtitle = if (needsPasswordSetup) "Action required • Protect your account" else "Keep your account secure",
                            hasBadge = needsPasswordSetup,
                            badgeText = if (needsPasswordSetup) "Action Needed" else null,
                            onClick = { showChangePasswordDialog = true }
                        )

                        SettingsDivider()

                        // 3. Notifications
                        SettingsRowItem(
                            icon = Icons.Default.Notifications,
                            iconBg = Color(0xFFD97706),
                            title = "Notifications",
                            subtitle = "Quiz reminders & updates",
                            onClick = { showNotificationsDialog = true }
                        )

                        SettingsDivider()

                        // 4. Appearance
                        SettingsRowItem(
                            icon = Icons.Default.Palette,
                            iconBg = Color(0xFFDB2777),
                            title = "Appearance",
                            subtitle = "Light / Dark mode",
                            onClick = { showThemeDialog = true }
                        )

                        SettingsDivider()

                        // 5. Help & Support
                        SettingsRowItem(
                            icon = Icons.AutoMirrored.Filled.HelpOutline,
                            iconBg = Color(0xFF0284C7),
                            title = "Help & Support",
                            subtitle = "Get help when you need it",
                            onClick = { showHelpSupportDialog = true }
                        )

                        SettingsDivider()

                        // 6. About Us
                        SettingsRowItem(
                            icon = Icons.Default.Info,
                            iconBg = Color(0xFF6366F1),
                            title = "About Us",
                            subtitle = "Version, mission & features",
                            onClick = { showAboutUsDialog = true }
                        )

                        SettingsDivider()

                        // 6.5. Privacy Policy
                        SettingsRowItem(
                            icon = Icons.Default.Policy,
                            iconBg = Color(0xFF0EA5E9),
                            title = "Privacy Policy",
                            subtitle = "Data security, privacy & terms",
                            onClick = { showPrivacyPolicyDialog = true }
                        )

                        SettingsDivider()

                        // 6.6. Feedback and remarks
                        SettingsRowItem(
                            icon = Icons.Default.RateReview,
                            iconBg = Color(0xFFF59E0B),
                            title = "Feedback and remarks",
                            subtitle = "Share your rating, opinion & issues",
                            onClick = { showFeedbackDialog = true }
                        )

                        SettingsDivider()

                        // 7. Delete Account
                        SettingsRowItem(
                            icon = Icons.Default.DeleteForever,
                            iconBg = Color(0xFFDC2626),
                            title = "Delete Account",
                            subtitle = "Permanently remove your account & data",
                            onClick = { showDeleteAccountDialog = true }
                        )

                        SettingsDivider()

                        // 8. Log Out
                        SettingsRowItem(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            iconBg = Color(0xFF991B1B),
                            title = "Log Out",
                            subtitle = "Sign out from your account",
                            onClick = { showLogoutConfirmDialog = true }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Interactive Dialogs
    if (showProfilePictureOptionsDialog) {
        ProfilePictureOptionsDialog(
            user = user,
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
            onDismiss = { showViewProfilePictureDialog = false },
            onChangePhotoClick = {
                showViewProfilePictureDialog = false
                showProfilePictureOptionsDialog = true
            }
        )
    }

    if (showPersonalInfoDialog) {
        PersonalInfoDialog(
            user = user,
            onDismiss = { showPersonalInfoDialog = false },
            onSave = { name, university, department, course, avatarUrl, email, contactNumber, onResult ->
                onUpdateProfile(name, university, department, course, avatarUrl, email, contactNumber) { success ->
                    onResult(success)
                    if (success) {
                        context.showToast("Personal information updated successfully! ✨")
                    } else {
                        context.showToast("Failed to update personal information.")
                    }
                }
            }
        )
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            isFirstTimeSetup = user?.needsPasswordSetup == true,
            onDismiss = { showChangePasswordDialog = false },
            onSubmit = { currentPass, newPass, onResult ->
                onSetOrChangePassword(currentPass, newPass, onResult)
            }
        )
    }

    if (showNotificationsDialog) {
        NotificationsDialog(
            onDismiss = { showNotificationsDialog = false },
            onSave = {
                context.showToast("Notification preferences saved! 🔔")
                showNotificationsDialog = false
            }
        )
    }

    if (showThemeDialog) {
        ThemeDialog(
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { mode ->
                ThemeManager.setTheme(context, mode)
                val label = when (mode) {
                    ThemeMode.DARK   -> "Dark"
                    ThemeMode.LIGHT  -> "Light"
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
                            userName = user?.name ?: "Student",
                            userEmail = user?.email ?: "student@quizmaster.com",
                            role = user?.role?.label ?: "Student",
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

    if (showAchievementsDialog) {
        AchievementsViewAllDialog(
            stats = stats,
            onDismiss = { showAchievementsDialog = false }
        )
    }
}
