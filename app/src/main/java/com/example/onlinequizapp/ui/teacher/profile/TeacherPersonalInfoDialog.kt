package com.example.onlinequizapp.ui.teacher.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.ui.components.consumeClicks
import com.example.onlinequizapp.ui.components.dismissOnClickOutside
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme

// -------------------------------------------------------------
// Interactive Dialogs for Profile Page: Personal Info
// -------------------------------------------------------------
@Composable
internal fun TeacherPersonalInfoDialog(
    user: User?,
    onDismiss: () -> Unit,
    onSave: (name: String, university: String, department: String, course: String, avatarUrl: String?, email: String?, contactNumber: String?, onResult: (Boolean) -> Unit) -> Unit
) {
    val isSocial = user?.isSocialAccount == true ||
            user?.authProvider?.lowercase() in listOf("google", "github")

    var isEditing by remember { mutableStateOf(false) }
    var name by remember(user?.name) { mutableStateOf(user?.name ?: "") }
    var university by remember(user?.displayUniversity) { mutableStateOf(user?.displayUniversity ?: "") }
    var department by remember(user?.displayDepartment) { mutableStateOf(user?.displayDepartment ?: "") }
    var course by remember(user?.displayCourse) { mutableStateOf(user?.displayCourse ?: "") }
    var email by remember(user?.email) { mutableStateOf(user?.email ?: "") }
    val initialContact = remember(user?.contactNumber) {
        val raw = user?.contactNumber ?: ""
        raw.removePrefix("+91").trim()
    }
    var contactNumber by remember(user?.contactNumber) { mutableStateOf(initialContact) }
    var isSaving by remember { mutableStateOf(false) }
    var departmentDropdownOpen by remember { mutableStateOf(false) }
    var courseDropdownOpen by remember { mutableStateOf(false) }
    var isCustomDeptMode by remember { mutableStateOf(false) }
    var isCustomCourseMode by remember { mutableStateOf(false) }

    val popularDesignations = listOf(
        "Head of Department (HOD)",
        "Professor",
        "Associate Professor",
        "Assistant Professor",
        "Teaching Staff / Lecturer",
        "Adjunct / Visiting Faculty"
    )

    val departmentsList = listOf(
        "Agriculture",
        "Allied Health Sciences",
        "Biotechnology",
        "Computer Science & Engineering",
        "Computer Science & Engineering - AI",
        "Computer Science & Engineering - CS & DS",
        "Computational Sciences",
        "Cyber Science & Technology",
        "Civil Engineering",
        "Commerce",
        "Electrical Engineering",
        "Electronics & Communication Engineering",
        "English & Literary Studies",
        "Food & Nutrition",
        "Hospital Management",
        "Law",
        "Mechanical Engineering",
        "Media Science & Journalism",
        "Multimedia",
        "Management",
        "Mathematics",
        "Nursing",
        "Pharmaceutical Technology",
        "Psychology",
        "Physics",
        "Chemistry"
    )

    val isDark = isTeacherInDarkTheme()
    val dialogBg = if (isDark) Color(0xFF0A102E) else Color(0xFFFFFFFF)
    val dialogBorder = if (isDark) Color(0xFF1E2D64) else Color(0xFFE2E8F0)
    val cardBg = if (isDark) Color(0xFF10193D) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF22326C) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dismissOnClickOutside { if (!isSaving) onDismiss() }
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .consumeClicks()
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(26.dp))
                    .background(dialogBg)
                    .border(1.dp, dialogBorder, RoundedCornerShape(26.dp))
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
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
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF7C3AED), Color(0xFF3B82F6))
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

                        Column {
                            Text(
                                text = "Personal Information",
                                style = TextStyle(
                                    color = textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = if (isEditing) "Edit academic & faculty profile" else "Faculty & account details",
                                style = TextStyle(
                                    color = textSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = { if (!isSaving) onDismiss() },
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

                TeacherModalDivider(color = if (isDark) Color(0xFF1E2D64) else Color(0xFFE2E8F0))

                if (!isEditing) {
                    // --- VIEW MODE ---
                    // Badges Row (Role + Auth Provider + Lock status if social)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Role Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color(0xFF7C3AED).copy(alpha = 0.2f) else Color(0xFFEDE9FE))
                                .border(1.dp, if (isDark) Color(0xFF7C3AED).copy(alpha = 0.45f) else Color(0xFFDDD6FE), RoundedCornerShape(8.dp))
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "👨‍🏫 Teacher",
                                color = if (isDark) Color(0xFFA78BFA) else Color(0xFF6D28D9),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }

                        // Provider Pill
                        val providerLabel = when {
                            user?.authProvider?.contains("google", ignoreCase = true) == true -> "🔵 Google Account"
                            user?.authProvider?.contains("github", ignoreCase = true) == true -> "🐙 GitHub Account"
                            else -> "✉️ Email Account"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                                .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = providerLabel,
                                color = if (isDark) textSecondary else Color(0xFF334155),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            )
                        }

                        if (isSocial) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Color(0xFFF59E0B).copy(alpha = 0.12f) else Color(0xFFFEF3C7))
                                    .border(1.dp, if (isDark) Color(0xFFF59E0B).copy(alpha = 0.3f) else Color(0xFFFCD34D), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Email Locked",
                                        color = if (isDark) Color(0xFFF59E0B) else Color(0xFFB45309),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Section 1: Academic / Organization Details
                    Text(
                        text = "ACADEMIC & FACULTY DETAILS",
                        style = TextStyle(
                            color = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        TeacherInfoCardItem(
                            icon = Icons.Default.AccountBalance,
                            iconTint = Color(0xFF38BDF8),
                            label = "University / College",
                            value = user?.displayUniversity?.takeIf { it.isNotBlank() } ?: "Not Set",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                        TeacherModalDivider(color = cardBorder)
                        TeacherInfoCardItem(
                            icon = Icons.Default.Apartment,
                            iconTint = Color(0xFFA855F7),
                            label = "Department",
                            value = user?.displayDepartment?.takeIf { it.isNotBlank() } ?: "Not Set",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                        TeacherModalDivider(color = cardBorder)
                        TeacherInfoCardItem(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            iconTint = Color(0xFFEC4899),
                            label = "Course / Subject",
                            value = user?.displayCourse?.takeIf { it.isNotBlank() } ?: "Not Set",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }

                    // Section 2: Personal Details
                    Text(
                        text = "PERSONAL & CONTACT",
                        style = TextStyle(
                            color = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        TeacherInfoCardItem(
                            icon = Icons.Default.Person,
                            iconTint = Color(0xFF34D399),
                            label = "Full Name",
                            value = user?.name?.takeIf { it.isNotBlank() } ?: "Not Set",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                        TeacherModalDivider(color = cardBorder)
                        TeacherInfoCardItem(
                            icon = Icons.Default.Email,
                            iconTint = if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706),
                            label = "Email Address",
                            value = user?.email?.takeIf { it.isNotBlank() } ?: "Not Set",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            isLocked = isSocial
                        )
                        TeacherModalDivider(color = cardBorder)
                        TeacherInfoCardItem(
                            icon = Icons.Default.Phone,
                            iconTint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            label = "Contact Number",
                            value = user?.contactNumber?.takeIf { it.isNotBlank() } ?: "Not Added",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Action Buttons in View Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isDark) textSecondary else Color(0xFF475569)
                            )
                        ) {
                            Text("Close", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4F46E5)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Details", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // --- EDIT MODE ---
                    if (isSocial) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFFF59E0B).copy(alpha = 0.12f) else Color(0xFFFEF3C7))
                                .border(1.dp, if (isDark) Color(0xFFF59E0B).copy(alpha = 0.35f) else Color(0xFFFCD34D), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706),
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Your account is connected with ${if (user?.authProvider?.contains("github", ignoreCase = true) == true) "GitHub" else "Google"}. The email cannot be edited, but you can freely update your name, college, department, and course details.",
                                    color = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E),
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Editable Fields
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name", color = textSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedContainerColor = if (isDark) dialogBg else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isDark) dialogBg else Color(0xFFF8FAFC),
                            focusedBorderColor = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                            unfocusedBorderColor = if (isDark) cardBorder else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = university,
                        onValueChange = { university = it },
                        label = { Text("University / Organization Name", color = textSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedContainerColor = if (isDark) dialogBg else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isDark) dialogBg else Color(0xFFF8FAFC),
                            focusedBorderColor = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                            unfocusedBorderColor = if (isDark) cardBorder else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    val isPredefinedDept = departmentsList.any { it.equals(department, ignoreCase = true) }
                    val isDeptCustom = isCustomDeptMode || (!isPredefinedDept && department.isNotBlank())
                    val displayDept = when {
                        isDeptCustom && department.isNotBlank() && !isPredefinedDept -> "Other / Custom: $department"
                        isDeptCustom -> "Other / Custom Department..."
                        department.isNotBlank() -> department
                        else -> "Choose Department from dropdown..."
                    }

                    val isPredefinedDesignation = popularDesignations.any { it.equals(course, ignoreCase = true) }
                    val isCourseCustom = isCustomCourseMode || (!isPredefinedDesignation && course.isNotBlank())
                    val displayCourse = when {
                        isCourseCustom && course.isNotBlank() && !isPredefinedDesignation -> "Other / Custom: $course"
                        isCourseCustom -> "Other / Custom Designation..."
                        course.isNotBlank() -> course
                        else -> "Choose Designation / Role from dropdown..."
                    }

                    // Department Dropdown Trigger Box
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = displayDept,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Department", color = textSecondary) },
                            leadingIcon = {
                                Icon(Icons.Default.Apartment, contentDescription = null, tint = if (isDark) Color(0xFFA855F7) else Color(0xFF9333EA), modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!isDeptCustom && isPredefinedDept) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    IconButton(onClick = { departmentDropdownOpen = !departmentDropdownOpen }) {
                                        Icon(
                                            imageVector = if (departmentDropdownOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Select Department",
                                            tint = if (isDark) Color(0xFFA855F7) else Color(0xFF9333EA)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,
                                focusedContainerColor = if (isDark) dialogBg else Color(0xFFF8FAFC),
                                unfocusedContainerColor = if (isDark) dialogBg else Color(0xFFF8FAFC),
                                focusedBorderColor = if (isDark) Color(0xFFA855F7) else Color(0xFF9333EA),
                                unfocusedBorderColor = if (isDark) cardBorder else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Transparent overlay to ensure clicking anywhere opens the dropdown and suppresses the keyboard
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { departmentDropdownOpen = !departmentDropdownOpen }
                        )

                        DropdownMenu(
                            expanded = departmentDropdownOpen,
                            onDismissRequest = { departmentDropdownOpen = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .heightIn(max = 280.dp)
                                .background(if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF))
                                .border(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.35f) else Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                        ) {
                            departmentsList.forEach { dept ->
                                val isSelected = !isDeptCustom && department.equals(dept, ignoreCase = true)
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = dept,
                                                color = if (isSelected) (if (isDark) Color(0xFFA855F7) else Color(0xFF9333EA)) else textPrimary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.5.sp
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = if (isDark) Color(0xFFA855F7) else Color(0xFF9333EA),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        department = dept
                                        isCustomDeptMode = false
                                        departmentDropdownOpen = false
                                    }
                                )
                            }

                            HorizontalDivider(
                                color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = if (isDark) Color(0xFFA855F7) else Color(0xFF9333EA),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Other / Custom Department...",
                                            color = if (isDark) Color(0xFFA855F7) else Color(0xFF9333EA),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp
                                        )
                                    }
                                },
                                onClick = {
                                    isCustomDeptMode = true
                                    if (isPredefinedDept) {
                                        department = ""
                                    }
                                    departmentDropdownOpen = false
                                }
                            )
                        }
                    }

                    // If custom department is selected or typed, provide editable text field with auto-capitalized first letter
                    if (isDeptCustom) {
                        OutlinedTextField(
                            value = department,
                            onValueChange = { input ->
                                department = input.replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase() else ch.toString() }
                                isCustomDeptMode = true
                            },
                            label = { Text("Custom Department Name", color = textSecondary) },
                            placeholder = { Text("Type custom department name...", color = textSecondary.copy(alpha = 0.5f)) },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = if (isDark) Color(0xFFA855F7) else Color(0xFF9333EA), modifier = Modifier.size(20.dp))
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,
                                focusedContainerColor = if (isDark) dialogBg else Color(0xFFF8FAFC),
                                unfocusedContainerColor = if (isDark) dialogBg else Color(0xFFF8FAFC),
                                focusedBorderColor = if (isDark) Color(0xFFA855F7) else Color(0xFF9333EA),
                                unfocusedBorderColor = if (isDark) cardBorder else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Designation / Course Dropdown Trigger Box
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = displayCourse,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Designation / Course", color = textSecondary) },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = if (isDark) Color(0xFFEC4899) else Color(0xFFDB2777), modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!isCourseCustom && isPredefinedDesignation) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    IconButton(onClick = { courseDropdownOpen = !courseDropdownOpen }) {
                                        Icon(
                                            imageVector = if (courseDropdownOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Select Designation",
                                            tint = if (isDark) Color(0xFFEC4899) else Color(0xFFDB2777)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,
                                focusedContainerColor = if (isDark) dialogBg else Color(0xFFF8FAFC),
                                unfocusedContainerColor = if (isDark) dialogBg else Color(0xFFF8FAFC),
                                focusedBorderColor = if (isDark) Color(0xFFEC4899) else Color(0xFFDB2777),
                                unfocusedBorderColor = if (isDark) cardBorder else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Transparent overlay to ensure clicking anywhere opens the dropdown and suppresses the keyboard
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { courseDropdownOpen = !courseDropdownOpen }
                        )

                        DropdownMenu(
                            expanded = courseDropdownOpen,
                            onDismissRequest = { courseDropdownOpen = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .heightIn(max = 280.dp)
                                .background(if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF))
                                .border(1.dp, if (isDark) Color(0xFFEC4899).copy(alpha = 0.35f) else Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                        ) {
                            popularDesignations.forEach { desig ->
                                val isSelected = !isCourseCustom && course.equals(desig, ignoreCase = true)
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = desig,
                                                color = if (isSelected) (if (isDark) Color(0xFFEC4899) else Color(0xFFDB2777)) else textPrimary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.5.sp
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = if (isDark) Color(0xFFEC4899) else Color(0xFFDB2777),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        course = desig
                                        isCustomCourseMode = false
                                        courseDropdownOpen = false
                                    }
                                )
                            }

                            HorizontalDivider(
                                color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = if (isDark) Color(0xFFEC4899) else Color(0xFFDB2777),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Other / Custom Designation...",
                                            color = if (isDark) Color(0xFFEC4899) else Color(0xFFDB2777),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp
                                        )
                                    }
                                },
                                onClick = {
                                    isCustomCourseMode = true
                                    if (isPredefinedDesignation) {
                                        course = ""
                                    }
                                    courseDropdownOpen = false
                                }
                            )
                        }
                    }

                    // If custom designation/course is selected or typed, provide editable text field with auto-capitalized first letter
                    if (isCourseCustom) {
                        OutlinedTextField(
                            value = course,
                            onValueChange = { input ->
                                course = input.replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase() else ch.toString() }
                                isCustomCourseMode = true
                            },
                            label = { Text("Custom Designation / Course", color = textSecondary) },
                            placeholder = { Text("Type custom designation (e.g. Associate Dean)...", color = textSecondary.copy(alpha = 0.5f)) },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = if (isDark) Color(0xFFEC4899) else Color(0xFFDB2777), modifier = Modifier.size(20.dp))
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,
                                focusedContainerColor = if (isDark) dialogBg else Color(0xFFF8FAFC),
                                unfocusedContainerColor = if (isDark) dialogBg else Color(0xFFF8FAFC),
                                focusedBorderColor = if (isDark) Color(0xFFEC4899) else Color(0xFFDB2777),
                                unfocusedBorderColor = if (isDark) cardBorder else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { if (!isSocial) email = it },
                        enabled = !isSocial,
                        label = { Text("Email Address", color = textSecondary) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = if (isSocial) (if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706)) else (if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (isSocial) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        supportingText = {
                            if (isSocial) {
                                Text(
                                    text = "Connected with ${user?.authProvider?.replaceFirstChar { it.uppercase() }} — email cannot be changed",
                                    color = if (isDark) Color(0xFFF59E0B) else Color(0xFFB45309),
                                    fontSize = 10.5.sp
                                )
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            disabledTextColor = if (isDark) textSecondary else Color(0xFF475569),
                            focusedContainerColor = if (isDark) dialogBg else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isDark) dialogBg else Color(0xFFF8FAFC),
                            disabledContainerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.5f) else Color(0xFFF1F5F9),
                            disabledBorderColor = if (isDark) cardBorder else Color(0xFFCBD5E1),
                            disabledLeadingIconColor = if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706),
                            disabledTrailingIconColor = if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706),
                            disabledLabelColor = textSecondary,
                            focusedBorderColor = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                            unfocusedBorderColor = if (isDark) cardBorder else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = contactNumber,
                        onValueChange = { input ->
                            val digitsOnly = input.filter { it.isDigit() }.take(10)
                            contactNumber = digitsOnly
                        },
                        label = { Text("Contact Number", color = textSecondary) },
                        placeholder = { Text("98765 43210", color = textSecondary.copy(alpha = 0.5f)) },
                        leadingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+91",
                                    style = TextStyle(
                                        color = textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(18.dp)
                                        .background(if (isDark) cardBorder else Color(0xFFCBD5E1))
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedContainerColor = if (isDark) dialogBg else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isDark) dialogBg else Color(0xFFF8FAFC),
                            focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            unfocusedBorderColor = if (isDark) cardBorder else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Action Buttons in Edit Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isEditing = false },
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isDark) textSecondary else Color(0xFF475569)
                            )
                        ) {
                            Text("Cancel", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    isSaving = true
                                    val formattedContact = if (contactNumber.isNotBlank()) "+91 ${contactNumber.trim()}" else ""
                                    onSave(name.trim(), university.trim(), department.trim(), course.trim(), user?.avatarUrl, email.trim(), formattedContact) { success ->
                                        isSaving = false
                                        if (success) {
                                            isEditing = false
                                        }
                                    }
                                }
                            },
                            enabled = !isSaving,
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4F46E5)
                            )
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Saving...", color = Color.White, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
