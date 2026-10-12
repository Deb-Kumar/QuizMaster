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
fun StudentOnboardingScreen(
    onComplete: (User) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StudentOnboardingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) }
    var departmentDropdownExpanded by remember { mutableStateOf(false) }
    var courseDropdownExpanded by remember { mutableStateOf(false) }
    var isCustomDeptSelected by remember { mutableStateOf(false) }
    var isCustomCourseSelected by remember { mutableStateOf(false) }

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
        "Primary Education",
        "Secondary Education",
        "Higher Secondary Education",
        "CBSE",
        "ICSE",
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

    val popularCourses = listOf(
        "Class I",
        "Class II",
        "Class III",
        "Class IV",
        "Class V",
        "Class VI",
        "Class VII",
        "Class VIII",
        "Class IX",
        "Class X",
        "Class XI",
        "Class XII",
        "Diploma in CSE",
        "Diploma in EE",
        "Diploma in ME",
        "Diploma in Robotics and Automation",
        "D. Pharm",
        "GNM",
        "DMLS",
        "B.Sc Agriculture",
        "BAOTT",
        "BMRIT",
        "BMLS",
        "Bachelor of Optometry",
        "BPA",
        "Bachelor of Physiotherapy",
        "B.Sc. CCT",
        "M.Optometry",
        "B.Tech CSE",
        "B.Tech CSE AI & ML",
        "B.Tech CSE (DS)",
        "B.Tech CSE - AI & Robotics",
        "B.Tech Cyber Security",
        "B.Tech Data Science & AI",
        "B.Sc. (H) Adv Networking & Cyber Security",
        "BCA",
        "BCA in AI & ML",
        "BBA",
        "BBA Business Analytics",
        "BBA Digital Marketing",
        "BBA Hospital Management",
        "B.Com Acc Fin & Banking",
        "B.Pharm",
        "B.Sc. OTT",
        "B.Sc. MRIT",
        "B.Sc. MLT",
        "B.Sc. in Nursing",
        "B.Sc. Food Nutrition & Dietetics",
        "B.Tech Biotechnology",
        "B.Sc. Biotechnology",
        "LLB",
        "BBA LLB",
        "BA LLB",
        "B.Sc. (H) Animation & Multimedia",
        "B.Sc. Animation, VFX & Gaming",
        "B.Sc. Digital MSJ (AI-Integrated)",
        "B.Sc. in Psychology",
        "BA Applied English",
        "B.Sc. Mathematics",
        "B.Sc. in Physics",
        "B.Sc. in Chemistry",
        "M.Tech CSE- AIML",
        "M.Tech Robotics & Automation",
        "M.Tech in CSE",
        "M.Tech CSE - Data Science",
        "MCA",
        "M.Sc. Adv.Networking Cyber Security",
        "M.Sc. Mathematics",
        "MBA",
        "M. Com",
        "MBA Hospital Management",
        "M. Pharm in Pharmaceutics",
        "M.Pharm in Pharmaceutical Chemistry",
        "M.Optometry",
        "Master of MLS",
        "M.Sc. In Nutrition & Dietetics",
        "MSc Biotechnology",
        "M.Sc. In Bioinformatics",
        "M.Sc Agriculture (Agronomy)",
        "M.Sc Agriculture (Horticulture)",
        "LLM",
        "M.Sc.Animation & Multimedia",
        "M.Sc.Media Science & Journalism",
        "MA English",
        "M.Sc. Applied Psychology",
        "Ph.D in Science (Computer Science)",
        "Ph.D in Science (Mathematics)",
        "Ph.D in Science (Biotechnology)",
        "Ph.D in Pharmaceutical Sciences",
        "Ph.D in Law",
        "Ph.D in Engineering (Electronics & Communication Engineering)",
        "Ph.D in Engineering (Computer Science & Engineering)",
        "Ph.D in Management",
        "Ph.D in Commerce",
        "Ph.D in Hospital Management",
        "Ph.D in Humanities (English)"
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
    val isPredefinedCourse = popularCourses.any { it.equals(uiState.course, ignoreCase = true) }

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
                    .padding(horizontal = 20.dp, vertical = 12.dp),
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
                            stepText = "STEP 1 OF 4 • UNIVERSITY",
                            titleMain = "Your",
                            titleAccent = "University",
                            subtitle = "What is the name of your university, college, or institute?",
                            icon = Icons.Default.School
                        )
                        2 -> OnboardingStepHeader(
                            stepText = "STEP 2 OF 4 • DEPARTMENT",
                            titleMain = "Your",
                            titleAccent = "Department",
                            subtitle = "Select your field of study or enter custom department",
                            icon = Icons.Default.Apartment
                        )
                        3 -> OnboardingStepHeader(
                            stepText = "STEP 3 OF 4 • DEGREE / COURSE",
                            titleMain = "Your Course /",
                            titleAccent = "Degree",
                            subtitle = "What degree or course are you currently studying?",
                            icon = Icons.AutoMirrored.Filled.MenuBook
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
                        label = "StudentStepAnimation"
                    ) { step ->
                        when (step) {
                            // -------------------------------------------------
                            // STEP 1: University / College
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
                                                text = "University or College Name *",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }

                                        AuthTextField(
                                            value = uiState.university,
                                            onValueChange = { input ->
                                                val formatted = input.replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase() else ch.toString() }
                                                viewModel.onUniversityChanged(formatted)
                                            },
                                            placeholder = "e.g. Delhi University / ABC College",
                                            leadingIcon = Icons.Default.School,
                                            fieldHeight = 52.dp,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Text,
                                                imeAction = ImeAction.Next
                                            ),
                                            keyboardActions = KeyboardActions(
                                                onNext = {
                                                    if (uiState.university.isBlank()) {
                                                        context.showToast("Please enter your university or college name.")
                                                        viewModel.setError("Please enter your university or college name.")
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
                                            text = "💡 Quizzes and topics will be tailored to your college curriculum.",
                                            fontSize = 12.sp,
                                            color = AuthTextDim,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }

                            // -------------------------------------------------
                            // STEP 2: Department
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
                            // STEP 3: Course / Degree (Dropdown Selection)
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
                                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                                    contentDescription = null,
                                                    tint = AuthAccentCyan,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Your Course or Degree *",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }

                                        // Course Dropdown Trigger
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(54.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(AuthSurface)
                                                    .border(
                                                        1.dp,
                                                        if (courseDropdownExpanded) AuthBorderFocused else AuthBorder,
                                                        RoundedCornerShape(16.dp)
                                                    )
                                                    .clickable {
                                                        focusManager.clearFocus()
                                                        courseDropdownExpanded = !courseDropdownExpanded
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
                                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                                        contentDescription = null,
                                                        tint = if (uiState.course.isNotBlank()) AuthAccentCyan else AuthTextMuted,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    val displayCourse = when {
                                                        isCustomCourseSelected && uiState.course.isNotBlank() && !isPredefinedCourse -> "Other / Custom: ${uiState.course}"
                                                        isCustomCourseSelected -> "Other / Custom Course..."
                                                        uiState.course.isNotBlank() -> uiState.course
                                                        else -> "Choose Course / Degree from dropdown..."
                                                    }
                                                    Text(
                                                        text = displayCourse,
                                                        color = if (uiState.course.isNotBlank() || isCustomCourseSelected) Color.White else AuthTextDim,
                                                        fontSize = 14.5.sp,
                                                        fontWeight = if (uiState.course.isNotBlank()) FontWeight.Medium else FontWeight.Normal,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                Icon(
                                                    imageVector = if (courseDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "Dropdown Arrow",
                                                    tint = AuthAccentCyan,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = courseDropdownExpanded,
                                                onDismissRequest = { courseDropdownExpanded = false },
                                                modifier = Modifier
                                                    .fillMaxWidth(0.88f)
                                                    .heightIn(max = 300.dp)
                                                    .background(Color(0xFF0F172A))
                                                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                            ) {
                                                popularCourses.forEach { courseItem ->
                                                    val isSelected = !isCustomCourseSelected && uiState.course.equals(courseItem, ignoreCase = true)
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
                                                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                                                        contentDescription = null,
                                                                        tint = if (isSelected) AuthAccentCyan else Color(0xFF64748B),
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                    Spacer(modifier = Modifier.width(10.dp))
                                                                    Text(
                                                                        text = courseItem,
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
                                                            courseDropdownExpanded = false
                                                            isCustomCourseSelected = false
                                                            viewModel.onCourseSelected(courseItem)
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
                                                                text = "Other / Custom Course...",
                                                                color = AuthAccentPurple,
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontSize = 14.sp
                                                            )
                                                        }
                                                    },
                                                    onClick = {
                                                        courseDropdownExpanded = false
                                                        isCustomCourseSelected = true
                                                        if (isPredefinedCourse) {
                                                            viewModel.onCourseSelected("")
                                                        }
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // If selected from predefined dropdown list, field is read-only.
                                        // If custom or "Other" chosen, field is editable and first letter capitalized.
                                        val isCourseReadOnly = !isCustomCourseSelected && isPredefinedCourse

                                        AuthTextField(
                                            value = uiState.course,
                                            onValueChange = { input ->
                                                if (!isCourseReadOnly) {
                                                    val formatted = input.replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase() else ch.toString() }
                                                    viewModel.onCourseSelected(formatted)
                                                    isCustomCourseSelected = true
                                                    viewModel.clearError()
                                                }
                                            },
                                            readOnly = isCourseReadOnly,
                                            placeholder = if (isCourseReadOnly) uiState.course else "Or type course (e.g. BCA, MCA, MBA)...",
                                            leadingIcon = if (isCourseReadOnly) Icons.Default.Lock else Icons.Default.Edit,
                                            trailingIcon = if (isCourseReadOnly) {
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
                                                    if (uiState.course.isBlank()) {
                                                        context.showToast("Please select or enter your course/degree.")
                                                        viewModel.setError("Please select or enter your course/degree.")
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
                                            text = if (isCourseReadOnly) {
                                                "🔒 Selected from dropdown (cannot be edited). To type custom course, choose 'Other' in dropdown."
                                            } else {
                                                "💡 Tap the dropdown to choose your course, or type custom above."
                                            },
                                            fontSize = 12.sp,
                                            color = if (isCourseReadOnly) AuthAccentCyan.copy(alpha = 0.8f) else AuthTextDim,
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
                                    if (uiState.university.isBlank()) {
                                        context.showToast("Please enter your university or college name.")
                                        viewModel.setError("Please enter your university or college name.")
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
                                    text = "Next: Course →",
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
                                        if (uiState.course.isBlank()) {
                                            context.showToast("Please select or enter your course/degree.")
                                            viewModel.setError("Please select or enter your course/degree.")
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
                                    text = "Complete & Start Quizzes →",
                                    onClick = {
                                        focusManager.clearFocus()
                                        viewModel.submit { user ->
                                            context.showToast("Student profile completed! Welcome to QuizMaster.")
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

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
