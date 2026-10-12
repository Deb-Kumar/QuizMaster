package com.example.onlinequizapp.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.ui.components.*
import com.example.onlinequizapp.ui.theme.DangerRed
import com.example.onlinequizapp.ui.theme.DangerRedBg

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeacherOnboardingScreen(
    onComplete: (User) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TeacherOnboardingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) }
    var departmentDropdownExpanded by remember { mutableStateOf(false) }
    var designationDropdownExpanded by remember { mutableStateOf(false) }
    var isCustomDeptSelected by remember { mutableStateOf(false) }
    var isCustomDesignationSelected by remember { mutableStateOf(false) }

    // Intercept hardware/gesture Back button to go to previous step
    BackHandler(enabled = currentStep > 1) {
        viewModel.clearError()
        currentStep--
    }

    // Show toast for validation errors
    androidx.compose.runtime.LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            context.showToast(msg)
        }
    }

    val popularDepartments = listOf(
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

    val popularDesignations = listOf(
        "Head of Department (HOD)",
        "Professor",
        "Associate Professor",
        "Assistant Professor",
        "Teaching Staff / Lecturer",
        "Adjunct / Visiting Faculty"
    )

    val referralSources = listOf(
        "YouTube",
        "Facebook",
        "Instagram",
        "Friend / Classmate",
        "Professor / Teacher",
        "Google Search",
        "Online Ad",
        "WhatsApp/Telegram",
        "Brochure/Flyer",
        "College Event",
        "Other"
    )

    val isPredefinedDept = popularDepartments.any { it.equals(uiState.department, ignoreCase = true) }
    val isPredefinedDesignation = popularDesignations.any { it.equals(uiState.designation, ignoreCase = true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuthBg)
    ) {
        AuthBackgroundCanvas()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // -------------------------------------------------------------
            // 1. Top Navigation & Step Progress Bar (Pinned at top)
            // -------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    IconButton(
                        onClick = {
                            viewModel.clearError()
                            focusManager.clearFocus()
                            currentStep--
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Step",
                            tint = AuthAccentCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                OnboardingProgressBar(
                    currentStep = currentStep,
                    totalSteps = 4,
                    modifier = Modifier.weight(1f)
                )
            }

            // -------------------------------------------------------------
            // 2. Middle Steps Container - Vertically Centered & Uplifted on Screen
            // -------------------------------------------------------------
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-8).dp)
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Step Header dynamically based on step
                    when (currentStep) {
                        1 -> OnboardingStepHeader(
                            stepText = "STEP 1 OF 4 • ORGANIZATION",
                            titleMain = "Your",
                            titleAccent = "Institution",
                            subtitle = "Enter your organization, college, or university name",
                            icon = Icons.Default.School
                        )
                        2 -> OnboardingStepHeader(
                            stepText = "STEP 2 OF 4 • DEPARTMENT",
                            titleMain = "Your",
                            titleAccent = "Department",
                            subtitle = "Which academic department do you belong to?",
                            icon = Icons.Default.Apartment
                        )
                        3 -> OnboardingStepHeader(
                            stepText = "STEP 3 OF 4 • DESIGNATION",
                            titleMain = "Your",
                            titleAccent = "Designation",
                            subtitle = "What is your academic role or title?",
                            icon = Icons.Default.Badge
                        )
                        4 -> OnboardingStepHeader(
                            stepText = "STEP 4 OF 4 • DISCOVERY",
                            titleMain = "Almost",
                            titleAccent = "Done!",
                            subtitle = "Where did you hear about QuizMaster? (Optional)",
                            icon = Icons.Default.Star
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Error Banner if validation fails
                    if (uiState.errorMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DangerRedBg)
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = DangerRed,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // ---------------------------------------------------------
                    // SMOOTH HORIZONTAL SLIDE TRANSITION FOR EACH STEP
                    // ---------------------------------------------------------
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally(animationSpec = tween(320)) { width -> width } + fadeIn(animationSpec = tween(320)))
                                    .togetherWith(slideOutHorizontally(animationSpec = tween(320)) { width -> -width } + fadeOut(animationSpec = tween(320)))
                            } else {
                                (slideInHorizontally(animationSpec = tween(320)) { width -> -width } + fadeIn(animationSpec = tween(320)))
                                    .togetherWith(slideOutHorizontally(animationSpec = tween(320)) { width -> width } + fadeOut(animationSpec = tween(320)))
                            }
                        },
                        label = "TeacherStepAnimation"
                    ) { step ->
                        when (step) {
                            // -------------------------------------------------
                            // STEP 1: Organization / College / University
                            // -------------------------------------------------
                            1 -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .border(1.dp, AuthBorder, RoundedCornerShape(18.dp))
                                        .background(Color(0xFF0C1129).copy(alpha = 0.85f))
                                        .padding(18.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(AuthAccentPurple.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.School,
                                                    contentDescription = null,
                                                    tint = AuthAccentCyan,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Organization / College / University *",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }

                                        AuthTextField(
                                            value = uiState.organization,
                                            onValueChange = { viewModel.onOrganizationChanged(it) },
                                            placeholder = "e.g. Oxford University / Apex Institute",
                                            leadingIcon = Icons.Default.School,
                                            fieldHeight = 52.dp,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Text,
                                                imeAction = ImeAction.Next
                                            ),
                                            keyboardActions = KeyboardActions(
                                                onNext = {
                                                    if (uiState.organization.isBlank()) {
                                                        context.showToast("Please enter your organization or college name.")
                                                        viewModel.setError("Please enter your organization or college name.")
                                                    } else {
                                                        viewModel.clearError()
                                                        focusManager.clearFocus()
                                                        currentStep = 2
                                                    }
                                                }
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "💡 Enter the full name of your school, college, or university.",
                                            fontSize = 12.sp,
                                            color = AuthTextDim,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }

                            // -------------------------------------------------
                            // STEP 2: Department (Dropdown Selection)
                            // -------------------------------------------------
                            2 -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .border(1.dp, AuthBorder, RoundedCornerShape(18.dp))
                                        .background(Color(0xFF0C1129).copy(alpha = 0.85f))
                                        .padding(18.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(AuthAccentPurple.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Apartment,
                                                    contentDescription = null,
                                                    tint = AuthAccentCyan,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Select Department *",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }

                                        // Department Dropdown Trigger
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(54.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(AuthSurface)
                                                    .border(
                                                        1.dp,
                                                        if (departmentDropdownExpanded) AuthBorderFocused else AuthBorder,
                                                        RoundedCornerShape(16.dp)
                                                    )
                                                    .clickable {
                                                        focusManager.clearFocus()
                                                        departmentDropdownExpanded = !departmentDropdownExpanded
                                                    }
                                                    .padding(horizontal = 16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Apartment,
                                                        contentDescription = null,
                                                        tint = if (uiState.department.isNotBlank()) AuthAccentCyan else AuthTextMuted,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    val displayDept = when {
                                                        isCustomDeptSelected && uiState.department.isNotBlank() && !isPredefinedDept -> "Other / Custom: ${uiState.department}"
                                                        isCustomDeptSelected -> "Other / Custom Department..."
                                                        uiState.department.isNotBlank() -> uiState.department
                                                        else -> "Choose Department from dropdown..."
                                                    }
                                                    Text(
                                                        text = displayDept,
                                                        color = if (uiState.department.isNotBlank() || isCustomDeptSelected) Color.White else AuthTextDim,
                                                        fontSize = 14.5.sp,
                                                        fontWeight = if (uiState.department.isNotBlank()) FontWeight.Medium else FontWeight.Normal,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                Icon(
                                                    imageVector = if (departmentDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "Dropdown Arrow",
                                                    tint = AuthAccentCyan,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = departmentDropdownExpanded,
                                                onDismissRequest = { departmentDropdownExpanded = false },
                                                modifier = Modifier
                                                    .fillMaxWidth(0.85f)
                                                    .heightIn(max = 280.dp)
                                                    .background(Color(0xFF0F172A))
                                                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                            ) {
                                                popularDepartments.forEach { dept ->
                                                    val isSelected = !isCustomDeptSelected && uiState.department.equals(dept, ignoreCase = true)
                                                    DropdownMenuItem(
                                                        text = {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    modifier = Modifier.weight(1f)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Apartment,
                                                                        contentDescription = null,
                                                                        tint = if (isSelected) AuthAccentCyan else Color(0xFF64748B),
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                    Spacer(modifier = Modifier.width(10.dp))
                                                                    Text(
                                                                        text = dept,
                                                                        color = if (isSelected) AuthAccentCyan else Color.White,
                                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                        fontSize = 14.sp,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis
                                                                    )
                                                                }
                                                                if (isSelected) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Check,
                                                                        contentDescription = null,
                                                                        tint = AuthAccentCyan,
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }
                                                            }
                                                        },
                                                        onClick = {
                                                            departmentDropdownExpanded = false
                                                            isCustomDeptSelected = false
                                                            viewModel.onDepartmentSelected(dept)
                                                            viewModel.clearError()
                                                        }
                                                    )
                                                }

                                                HorizontalDivider(
                                                    color = Color(0xFF1E293B),
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
                                                                tint = AuthAccentPurple,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(10.dp))
                                                            Text(
                                                                text = "Other / Custom Department...",
                                                                color = AuthAccentPurple,
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontSize = 14.sp
                                                            )
                                                        }
                                                    },
                                                    onClick = {
                                                        departmentDropdownExpanded = false
                                                        isCustomDeptSelected = true
                                                        if (isPredefinedDept) {
                                                            viewModel.onDepartmentSelected("")
                                                        }
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // If selected from predefined dropdown list, field is read-only.
                                        // If custom or "Other" chosen, field is editable and first letter capitalized.
                                        val isDeptReadOnly = !isCustomDeptSelected && isPredefinedDept

                                        AuthTextField(
                                            value = uiState.department,
                                            onValueChange = { input ->
                                                if (!isDeptReadOnly) {
                                                    val formatted = input.replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase() else ch.toString() }
                                                    viewModel.onDepartmentSelected(formatted)
                                                    isCustomDeptSelected = true
                                                    viewModel.clearError()
                                                }
                                            },
                                            readOnly = isDeptReadOnly,
                                            placeholder = if (isDeptReadOnly) uiState.department else "Or type custom department...",
                                            leadingIcon = if (isDeptReadOnly) Icons.Default.Lock else Icons.Default.Edit,
                                            trailingIcon = if (isDeptReadOnly) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = "Selected from list",
                                                        tint = AuthTextDim,
                                                        modifier = Modifier.size(17.dp)
                                                    )
                                                }
                                            } else null,
                                            fieldHeight = 50.dp,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Text,
                                                imeAction = ImeAction.Next
                                            ),
                                            keyboardActions = KeyboardActions(
                                                onNext = {
                                                    if (uiState.department.isBlank()) {
                                                        context.showToast("Please select or enter your department.")
                                                        viewModel.setError("Please select or enter your department.")
                                                    } else {
                                                        viewModel.clearError()
                                                        focusManager.clearFocus()
                                                        currentStep = 3
                                                    }
                                                }
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = if (isDeptReadOnly) {
                                                "🔒 Selected from dropdown (cannot be edited). To type custom department, choose 'Other' in dropdown."
                                            } else {
                                                "💡 Tap the dropdown to choose your department, or type custom above."
                                            },
                                            fontSize = 12.sp,
                                            color = if (isDeptReadOnly) AuthAccentCyan.copy(alpha = 0.8f) else AuthTextDim,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }

                            // -------------------------------------------------
                            // STEP 3: Designation (Dropdown Selection)
                            // -------------------------------------------------
                            3 -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .border(1.dp, AuthBorder, RoundedCornerShape(18.dp))
                                        .background(Color(0xFF0C1129).copy(alpha = 0.85f))
                                        .padding(18.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(AuthAccentPurple.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Badge,
                                                    contentDescription = null,
                                                    tint = AuthAccentCyan,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Your Designation / Academic Role *",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }

                                        // Designation Dropdown Trigger
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(54.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(AuthSurface)
                                                    .border(
                                                        1.dp,
                                                        if (designationDropdownExpanded) AuthBorderFocused else AuthBorder,
                                                        RoundedCornerShape(16.dp)
                                                    )
                                                    .clickable {
                                                        focusManager.clearFocus()
                                                        designationDropdownExpanded = !designationDropdownExpanded
                                                    }
                                                    .padding(horizontal = 16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Badge,
                                                        contentDescription = null,
                                                        tint = if (uiState.designation.isNotBlank()) AuthAccentCyan else AuthTextMuted,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    val displayDesig = when {
                                                        isCustomDesignationSelected && uiState.designation.isNotBlank() && !isPredefinedDesignation -> "Other / Custom: ${uiState.designation}"
                                                        isCustomDesignationSelected -> "Other / Custom Designation..."
                                                        uiState.designation.isNotBlank() -> uiState.designation
                                                        else -> "Choose Designation from dropdown..."
                                                    }
                                                    Text(
                                                        text = displayDesig,
                                                        color = if (uiState.designation.isNotBlank() || isCustomDesignationSelected) Color.White else AuthTextDim,
                                                        fontSize = 14.5.sp,
                                                        fontWeight = if (uiState.designation.isNotBlank()) FontWeight.Medium else FontWeight.Normal,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                Icon(
                                                    imageVector = if (designationDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "Dropdown Arrow",
                                                    tint = AuthAccentCyan,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = designationDropdownExpanded,
                                                onDismissRequest = { designationDropdownExpanded = false },
                                                modifier = Modifier
                                                    .fillMaxWidth(0.85f)
                                                    .heightIn(max = 280.dp)
                                                    .background(Color(0xFF0F172A))
                                                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                            ) {
                                                popularDesignations.forEach { desig ->
                                                    val isSelected = !isCustomDesignationSelected && uiState.designation.equals(desig, ignoreCase = true)
                                                    DropdownMenuItem(
                                                        text = {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    modifier = Modifier.weight(1f)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Badge,
                                                                        contentDescription = null,
                                                                        tint = if (isSelected) AuthAccentCyan else Color(0xFF64748B),
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                    Spacer(modifier = Modifier.width(10.dp))
                                                                    Text(
                                                                        text = desig,
                                                                        color = if (isSelected) AuthAccentCyan else Color.White,
                                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                        fontSize = 14.sp,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis
                                                                    )
                                                                }
                                                                if (isSelected) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Check,
                                                                        contentDescription = null,
                                                                        tint = AuthAccentCyan,
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }
                                                            }
                                                        },
                                                        onClick = {
                                                            designationDropdownExpanded = false
                                                            isCustomDesignationSelected = false
                                                            viewModel.onDesignationSelected(desig)
                                                            viewModel.clearError()
                                                        }
                                                    )
                                                }

                                                HorizontalDivider(
                                                    color = Color(0xFF1E293B),
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
                                                                tint = AuthAccentPurple,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(10.dp))
                                                            Text(
                                                                text = "Other / Custom Designation...",
                                                                color = AuthAccentPurple,
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontSize = 14.sp
                                                            )
                                                        }
                                                    },
                                                    onClick = {
                                                        designationDropdownExpanded = false
                                                        isCustomDesignationSelected = true
                                                        if (isPredefinedDesignation) {
                                                            viewModel.onDesignationSelected("")
                                                        }
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // If selected from predefined dropdown list, field is read-only.
                                        // If custom or "Other" chosen, field is editable and first letter capitalized.
                                        val isDesignationReadOnly = !isCustomDesignationSelected && isPredefinedDesignation

                                        AuthTextField(
                                            value = uiState.designation,
                                            onValueChange = { input ->
                                                if (!isDesignationReadOnly) {
                                                    val formatted = input.replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase() else ch.toString() }
                                                    viewModel.onDesignationSelected(formatted)
                                                    isCustomDesignationSelected = true
                                                    viewModel.clearError()
                                                }
                                            },
                                            readOnly = isDesignationReadOnly,
                                            placeholder = if (isDesignationReadOnly) uiState.designation else "Or type custom designation (e.g. Associate Dean)...",
                                            leadingIcon = if (isDesignationReadOnly) Icons.Default.Lock else Icons.Default.Edit,
                                            trailingIcon = if (isDesignationReadOnly) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = "Selected from list",
                                                        tint = AuthTextDim,
                                                        modifier = Modifier.size(17.dp)
                                                    )
                                                }
                                            } else null,
                                            fieldHeight = 50.dp,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Text,
                                                imeAction = ImeAction.Next
                                            ),
                                            keyboardActions = KeyboardActions(
                                                onNext = {
                                                    if (uiState.designation.isBlank()) {
                                                        context.showToast("Please select or enter your designation.")
                                                        viewModel.setError("Please select or enter your designation.")
                                                    } else {
                                                        viewModel.clearError()
                                                        focusManager.clearFocus()
                                                        currentStep = 4
                                                    }
                                                }
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = if (isDesignationReadOnly) {
                                                "🔒 Selected from dropdown (cannot be edited). To type custom designation, choose 'Other' in dropdown."
                                            } else {
                                                "💡 Tap the dropdown to choose your designation, or type custom above."
                                            },
                                            fontSize = 12.sp,
                                            color = if (isDesignationReadOnly) AuthAccentCyan.copy(alpha = 0.8f) else AuthTextDim,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }

                            // -------------------------------------------------
                            // STEP 4: Referral Source (Vertical Radio Buttons)
                            // -------------------------------------------------
                            4 -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .border(1.dp, AuthBorder, RoundedCornerShape(18.dp))
                                        .background(Color(0xFF0C1129).copy(alpha = 0.85f))
                                        .padding(18.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(AuthAccentPurple.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFBBF24),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Where did you hear about QuizMaster?",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }

                                        Text(
                                            text = "Select an option below (Optional):",
                                            fontSize = 12.sp,
                                            color = AuthTextDim,
                                            modifier = Modifier.padding(start = 42.dp, bottom = 12.dp)
                                        )

                                        // Vertical radio button options list
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            referralSources.forEach { source ->
                                                val isSelected = uiState.referralSource.equals(source, ignoreCase = true) ||
                                                    (source == "Other" && uiState.referralSource.isNotBlank() && !referralSources.contains(uiState.referralSource))

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(if (isSelected) AuthAccentPurple.copy(alpha = 0.18f) else AuthSurface)
                                                        .border(
                                                            width = 1.dp,
                                                            color = if (isSelected) AuthAccentCyan else AuthBorder,
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                        .clickable {
                                                            focusManager.clearFocus()
                                                            viewModel.onReferralSourceSelected(source)
                                                        }
                                                        .padding(horizontal = 14.dp, vertical = 11.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    RadioButton(
                                                        selected = isSelected,
                                                        onClick = {
                                                            focusManager.clearFocus()
                                                            viewModel.onReferralSourceSelected(source)
                                                        },
                                                        colors = RadioButtonDefaults.colors(
                                                            selectedColor = AuthAccentCyan,
                                                            unselectedColor = AuthTextDim
                                                        ),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(
                                                        text = source,
                                                        fontSize = 14.sp,
                                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                        color = if (isSelected) Color.White else AuthTextMuted
                                                    )
                                                }
                                            }
                                        }

                                        // If "Other" selected, allow custom input with capitalized first letter
                                        val isOtherSelected = uiState.referralSource.equals("Other", ignoreCase = true) ||
                                            (uiState.referralSource.isNotBlank() && !referralSources.contains(uiState.referralSource))

                                        if (isOtherSelected) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            AuthTextField(
                                                value = if (uiState.referralSource.equals("Other", ignoreCase = true)) "" else uiState.referralSource,
                                                onValueChange = { input ->
                                                    val formatted = input.replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase() else ch.toString() }
                                                    viewModel.onReferralSourceSelected(formatted.ifBlank { "Other" })
                                                },
                                                placeholder = "Please tell us how you discovered us...",
                                                leadingIcon = Icons.Default.Edit,
                                                fieldHeight = 48.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 3. Sticky Glassmorphic Bottom Navigation Action Bar (Pinned at bottom)
            // -------------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                AuthBg.copy(alpha = 0.92f),
                                AuthBg,
                                AuthBg
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (currentStep) {
                        1 -> {
                            AuthPrimaryButton(
                                text = "Next: Department →",
                                onClick = {
                                    if (uiState.organization.isBlank()) {
                                        context.showToast("Please enter your organization or college name.")
                                        viewModel.setError("Please enter your organization or college name.")
                                    } else {
                                        viewModel.clearError()
                                        focusManager.clearFocus()
                                        currentStep = 2
                                    }
                                },
                                isLoading = false,
                                buttonHeight = 52.dp
                            )
                        }
                        2 -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.clearError()
                                        focusManager.clearFocus()
                                        currentStep = 1
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, AuthBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = AuthSurface)
                                ) {
                                    Text("← Back", color = AuthTextMuted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }

                                AuthPrimaryButton(
                                    text = "Next: Designation →",
                                    onClick = {
                                        if (uiState.department.isBlank()) {
                                            context.showToast("Please select or enter your department.")
                                            viewModel.setError("Please select or enter your department.")
                                        } else {
                                            viewModel.clearError()
                                            focusManager.clearFocus()
                                            currentStep = 3
                                        }
                                    },
                                    isLoading = false,
                                    modifier = Modifier.weight(1.8f),
                                    buttonHeight = 52.dp
                                )
                            }
                        }
                        3 -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.clearError()
                                        focusManager.clearFocus()
                                        currentStep = 2
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, AuthBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = AuthSurface)
                                ) {
                                    Text("← Back", color = AuthTextMuted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }

                                AuthPrimaryButton(
                                    text = "Next: Discovery →",
                                    onClick = {
                                        if (uiState.designation.isBlank()) {
                                            context.showToast("Please select or enter your designation.")
                                            viewModel.setError("Please select or enter your designation.")
                                        } else {
                                            viewModel.clearError()
                                            focusManager.clearFocus()
                                            currentStep = 4
                                        }
                                    },
                                    isLoading = false,
                                    modifier = Modifier.weight(1.8f),
                                    buttonHeight = 52.dp
                                )
                            }
                        }
                        4 -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.clearError()
                                        focusManager.clearFocus()
                                        currentStep = 3
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, AuthBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = AuthSurface)
                                ) {
                                    Text("← Back", color = AuthTextMuted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }

                                AuthPrimaryButton(
                                    text = "Complete & Start Teaching →",
                                    onClick = {
                                        focusManager.clearFocus()
                                        viewModel.submit { user ->
                                            context.showToast("Teacher profile completed! Welcome to QuizMaster.")
                                            onComplete(user)
                                        }
                                    },
                                    isLoading = uiState.isLoading,
                                    modifier = Modifier.weight(2f),
                                    buttonHeight = 52.dp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
