package com.example.onlinequizapp.ui.teacher.create

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Event
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.DialogProperties
import com.example.onlinequizapp.ui.components.SubjectLogoItem
import com.example.onlinequizapp.ui.components.SubjectLogoRegistry
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.onlinequizapp.R
import com.example.onlinequizapp.data.session.CategoryManager
import com.example.onlinequizapp.domain.model.Difficulty
import com.example.onlinequizapp.domain.model.QuestionType
import com.example.onlinequizapp.domain.model.QuizCreationMethod
import com.example.onlinequizapp.domain.model.TimerMode
import com.example.onlinequizapp.ui.teacher.QuizBuilderCardBg
import com.example.onlinequizapp.ui.teacher.QuizBuilderCardBorder
import com.example.onlinequizapp.ui.teacher.QuizBuilderInputBg
import com.example.onlinequizapp.ui.teacher.QuizBuilderInputBorder
import com.example.onlinequizapp.ui.teacher.QuizBuilderChipBg
import com.example.onlinequizapp.ui.teacher.QuizBuilderChipBorder
import com.example.onlinequizapp.ui.teacher.QuizBuilderTextPrimary
import com.example.onlinequizapp.ui.teacher.QuizBuilderTextSecondary
import com.example.onlinequizapp.ui.teacher.QuizBuilderTextMuted
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme

private val StandardTimeLimits = listOf(5, 10, 15, 30, 45)

private val CardBackground: Color
    @Composable get() = QuizBuilderCardBg

private val CardBorderColor: Color
    @Composable get() = QuizBuilderCardBorder

private val InputContainerBg: Color
    @Composable get() = QuizBuilderInputBg

private val InputBorderColor: Color
    @Composable get() = QuizBuilderInputBorder

private val ButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFF3B82F6))
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BasicInfoStep(
    creationMethod: QuizCreationMethod,
    title: String,
    onTitleChanged: (String) -> Unit,
    selectedLogo: String = "",
    onLogoSelected: (String) -> Unit = {},
    description: String,
    onDescriptionChanged: (String) -> Unit,
    selectedCategories: List<String> = emptyList(),
    onCategoriesChanged: (List<String>) -> Unit = {},
    category: String = "",
    onCategoryChanged: (String) -> Unit = {},
    difficulty: Difficulty,
    onDifficultyChanged: (Difficulty) -> Unit,
    questionCount: Int,
    onQuestionCountChanged: (Int) -> Unit,
    questionType: QuestionType,
    onQuestionTypeChanged: (QuestionType) -> Unit,
    timerMode: TimerMode,
    onTimerModeChanged: (TimerMode) -> Unit,
    timeLimitMinutes: Int,
    onTimeLimitMinutesChanged: (Int) -> Unit,
    allowHints: Boolean,
    onAllowHintsChanged: (Boolean) -> Unit,

    isGenerating: Boolean,
    generatingStatusText: String,
    onBackClick: () -> Unit,
    onSubmitClick: () -> Unit,
    isEditing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = isTeacherInDarkTheme()
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var availableCategories by remember { mutableStateOf(CategoryManager.getAllCategories()) }
    var categorySearchQuery by remember { mutableStateOf("") }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showLogoPickerModal by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }
    var newCategoryError by remember { mutableStateOf<String?>(null) }

    // Filtered categories based on search query
    val filteredCategories = remember(availableCategories, categorySearchQuery) {
        val query = categorySearchQuery.trim()
        if (query.isBlank()) {
            availableCategories
        } else {
            availableCategories.filter { it.contains(query, ignoreCase = true) }
        }
    }

    // Resolve current selected categories (up to 5)
    val currentCategories = remember(selectedCategories, category) {
        val rawList = if (selectedCategories.isNotEmpty()) {
            selectedCategories
        } else if (category.isNotBlank()) {
            category.split(",")
        } else {
            emptyList()
        }
        rawList
            .flatMap { it.split(",") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(5)
    }

    val updateCategories: (List<String>) -> Unit = { newList ->
        val clamped = newList
            .flatMap { it.split(",") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(5)
        onCategoriesChanged(clamped)
        onCategoryChanged(clamped.joinToString(", "))
    }

    var isCustomCountSelected by remember {
        mutableStateOf(questionCount !in listOf(5, 10, 15, 20))
    }
    var customCountInput by remember {
        mutableStateOf(if (questionCount !in listOf(5, 10, 15, 20)) questionCount.toString() else "")
    }

    var isCustomTimeSelected by remember {
        mutableStateOf(timeLimitMinutes !in StandardTimeLimits)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // -----------------------------------------------------------------
        // Hero Banner: "Quiz Details" with Illustration
        // -----------------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF818CF8), Color(0xFF6366F1), Color(0xFF4F46E5))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = if (creationMethod == QuizCreationMethod.AI_GENERATED && !isEditing) "Configure AI Quiz" else "Quiz Details",
                        color = QuizBuilderTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "Set up basic quiz metadata, difficulty, timers, and rules.",
                        color = QuizBuilderTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Image(
                painter = painterResource(id = R.drawable.img_quiz_builder_clipboard),
                contentDescription = "Quiz Details Hero",
                modifier = Modifier.size(84.dp),
                contentScale = ContentScale.Fit
            )
        }

        // -----------------------------------------------------------------
        // 1. Quiz Subject Logo Card
        // -----------------------------------------------------------------
        if (creationMethod == QuizCreationMethod.MANUAL || isEditing) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogoPickerModal = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, CardBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 13.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(9.dp))
                        Column {
                            Text(
                                text = "Quiz Subject Logo",
                                color = QuizBuilderTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Choose an official tech / subject icon for this quiz",
                                color = QuizBuilderTextSecondary,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    if (selectedLogo.isNotBlank()) {
                        TextButton(
                            onClick = { onLogoSelected("") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Clear", color = Color(0xFFF87171), fontSize = 11.sp)
                        }
                    }
                }

                val currentLogo = SubjectLogoRegistry.getLogoById(selectedLogo)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(InputContainerBg)
                        .border(
                            1.dp,
                            if (currentLogo != null) Color(0xFF8B5CF6) else InputBorderColor,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { showLogoPickerModal = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (currentLogo != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = currentLogo.drawableRes),
                                        contentDescription = currentLogo.name,
                                        modifier = Modifier.size(26.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Column {
                                    Text(
                                        text = currentLogo.name,
                                        color = QuizBuilderTextPrimary,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = currentLogo.category,
                                        color = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Change",
                                    color = Color(0xFF818CF8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color(0xFF818CF8),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = QuizBuilderTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Select a subject / technology logo...",
                                    color = QuizBuilderTextMuted,
                                    fontSize = 13.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = QuizBuilderTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

        // -----------------------------------------------------------------
        // 2. Quiz Title / Topic * Card
        // -----------------------------------------------------------------
        if (creationMethod == QuizCreationMethod.MANUAL || isEditing) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 13.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF6366F1)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(9.dp))
                    Text(
                        text = "Quiz Title / Topic *",
                        color = QuizBuilderTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { input ->
                            if (input.length <= 100) {
                                val capitalized = input.replaceFirstChar { char ->
                                    if (char.isLowerCase()) char.titlecase() else char.toString()
                                }
                                onTitleChanged(capitalized)
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        placeholder = { Text("Enter quiz title...", color = QuizBuilderTextMuted, fontSize = 13.5.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = InputBorderColor,
                            focusedTextColor = QuizBuilderTextPrimary,
                            unfocusedTextColor = QuizBuilderTextPrimary,
                            focusedContainerColor = InputContainerBg,
                            unfocusedContainerColor = InputContainerBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${title.length}/100",
                        color = QuizBuilderTextMuted,
                        fontSize = 10.5.sp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 8.dp)
                    )
                }
            }
        }
    }

        // -----------------------------------------------------------------
        // 3. Quiz Description / AI Prompt * Card
        // -----------------------------------------------------------------
        val isAiMode = creationMethod == QuizCreationMethod.AI_GENERATED && !isEditing
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, CardBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 13.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isAiMode) Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)))
                                else Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF3B82F6)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAiMode) Icons.Default.AutoAwesome else Icons.Default.ChatBubble,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(9.dp))
                    Column {
                        Text(
                            text = if (isAiMode) "AI Quiz Prompt & Description *" else "Quiz Description *",
                            color = QuizBuilderTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isAiMode) {
                            Text(
                                text = "Describe what, topic, rules, and question style for AI to generate",
                                color = QuizBuilderTextSecondary,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }

                val maxPromptLen = if (isAiMode) 1000 else 250
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            if (it.length <= maxPromptLen) onDescriptionChanged(it)
                        },
                        placeholder = {
                            Text(
                                text = if (isAiMode) {
                                    "Enter your prompt in detail (e.g., 'Generate 15 challenging questions about Java multithreading, concurrency concepts, ExecutorService, locks, and synchronized blocks with code snippets')..."
                                } else {
                                    "Enter quiz description, goals, or instructions..."
                                },
                                color = QuizBuilderTextMuted,
                                fontSize = 13.sp
                            )
                        },
                        minLines = if (isAiMode) 4 else 1,
                        maxLines = if (isAiMode) 8 else 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isAiMode) Color(0xFF8B5CF6) else Color(0xFF3B82F6),
                            unfocusedBorderColor = InputBorderColor,
                            focusedTextColor = QuizBuilderTextPrimary,
                            unfocusedTextColor = QuizBuilderTextPrimary,
                            focusedContainerColor = InputContainerBg,
                            unfocusedContainerColor = InputContainerBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${description.length}/$maxPromptLen",
                        color = QuizBuilderTextMuted,
                        fontSize = 10.5.sp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 8.dp)
                    )
                }
            }
        }

        // -----------------------------------------------------------------
        // 4. Category Card (Manual Mode Only)
        // -----------------------------------------------------------------
        if (creationMethod == QuizCreationMethod.MANUAL || isEditing) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 13.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF0284C7)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(9.dp))
                        Text(
                            text = "Category *",
                            color = QuizBuilderTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "${currentCategories.size}/5 selected",
                        color = if (currentCategories.isNotEmpty()) Color(0xFF38BDF8) else QuizBuilderTextMuted,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(InputContainerBg)
                            .border(1.dp, InputBorderColor, RoundedCornerShape(12.dp))
                            .clickable { categoryDropdownExpanded = true }
                            .padding(horizontal = 13.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentCategories.isEmpty()) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isDark) Color(0xFF1E284D) else Color(0xFFE2E8F0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GridView,
                                        contentDescription = null,
                                        tint = QuizBuilderTextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Select Quiz category",
                                    color = QuizBuilderTextMuted,
                                    fontSize = 13.5.sp
                                )
                            }
                        } else {
                            FlowRow(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                currentCategories.forEach { cat ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isDark) Color(0xFF16255C) else Color(0xFFEEF2FF))
                                            .border(1.dp, if (isDark) Color(0xFF3B82F6) else Color(0xFFC7D2FE), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = cat,
                                                color = if (isDark) Color.White else Color(0xFF4338CA),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove $cat",
                                                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                                modifier = Modifier
                                                    .size(13.dp)
                                                    .clickable {
                                                        updateCategories(currentCategories.filter { it != cat })
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select Categories",
                            tint = QuizBuilderTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = {
                            categoryDropdownExpanded = false
                            categorySearchQuery = ""
                        },
                        modifier = Modifier
                            .widthIn(min = 280.dp, max = 340.dp)
                            .background(CardBackground)
                            .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = "Select up to 5 categories (${currentCategories.size}/5)",
                            color = QuizBuilderTextSecondary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )

                        // Category Search Input Field
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            OutlinedTextField(
                                value = categorySearchQuery,
                                onValueChange = { categorySearchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "Search category...",
                                        color = QuizBuilderTextMuted,
                                        fontSize = 12.5.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search Categories",
                                        tint = Color(0xFF818CF8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (categorySearchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { categorySearchQuery = "" },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear search",
                                                tint = QuizBuilderTextMuted,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF818CF8),
                                    unfocusedBorderColor = InputBorderColor,
                                    focusedTextColor = QuizBuilderTextPrimary,
                                    unfocusedTextColor = QuizBuilderTextPrimary,
                                    focusedContainerColor = InputContainerBg,
                                    unfocusedContainerColor = InputContainerBg
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Scrollable list of categories
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (filteredCategories.isEmpty()) {
                                Text(
                                    text = "No category matching \"$categorySearchQuery\"",
                                    color = QuizBuilderTextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )

                                // If query is non-empty, provide quick option to create it directly
                                val trimmedQuery = categorySearchQuery.trim()
                                if (trimmedQuery.isNotBlank()) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.AddCircleOutline,
                                                    contentDescription = null,
                                                    tint = Color(0xFF38BDF8),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Add \"$trimmedQuery\"",
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        },
                                        onClick = {
                                            CategoryManager.addCustomCategory(trimmedQuery)
                                            availableCategories = CategoryManager.getAllCategories()
                                            if (currentCategories.size < 5 && !currentCategories.any { it.equals(trimmedQuery, ignoreCase = true) }) {
                                                updateCategories(currentCategories + trimmedQuery)
                                            }
                                            categorySearchQuery = ""
                                        }
                                    )
                                }
                            } else {
                                filteredCategories.forEach { cat ->
                                    val isSelected = currentCategories.any { it.equals(cat, ignoreCase = true) }
                                    val canSelect = isSelected || currentCategories.size < 5

                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = cat,
                                                    color = if (isSelected) Color(0xFF818CF8) else if (canSelect) QuizBuilderTextPrimary else QuizBuilderTextMuted,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                if (isSelected) {
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
                                            if (isSelected) {
                                                updateCategories(currentCategories.filter { !it.equals(cat, ignoreCase = true) })
                                            } else if (currentCategories.size < 5) {
                                                updateCategories(currentCategories + cat)
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Add Custom Category Action Item
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AddCircleOutline,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "+ Create Custom Category",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            onClick = {
                                categoryDropdownExpanded = false
                                newCategoryInput = categorySearchQuery.trim()
                                categorySearchQuery = ""
                                newCategoryError = null
                                showAddCategoryDialog = true
                            }
                        )

                        // Done Button to dismiss menu
                        DropdownMenuItem(
                            text = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF4F46E5))
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Done (${currentCategories.size}/5)",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            onClick = {
                                categoryDropdownExpanded = false
                                categorySearchQuery = ""
                            }
                        )
                    }
                }
            }
        }
    }

        // -----------------------------------------------------------------
        // 4. Difficulty Level (Defaults to Easy)
        // -----------------------------------------------------------------
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = "Difficulty Level",
                    color = QuizBuilderTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple(Difficulty.EASY, "Easy", Icons.Default.SentimentSatisfied),
                    Triple(Difficulty.MEDIUM, "Medium", Icons.Default.SentimentNeutral),
                    Triple(Difficulty.HARD, "Hard", Icons.Default.SentimentDissatisfied)
                ).forEach { (diff, label, icon) ->
                    val isSelected = difficulty == diff
                    val pillBg = if (isSelected) {
                        if (isDark) Color(0xFF4F46E5).copy(alpha = 0.35f) else Color(0xFFEEF2FF)
                    } else {
                        if (isDark) Color(0xFF0C1438) else Color(0xFFF1F5F9)
                    }
                    val pillBorder = if (isSelected) {
                        Color(0xFF6366F1)
                    } else {
                        if (isDark) Color(0xFF16255C) else Color(0xFFE2E8F0)
                    }
                    val contentColor = if (isSelected) {
                        if (isDark) Color.White else Color(0xFF4338CA)
                    } else {
                        if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(pillBg)
                            .border(if (isSelected) 1.5.dp else 1.dp, pillBorder, RoundedCornerShape(12.dp))
                            .clickable { onDifficultyChanged(diff) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                color = contentColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // 5. Time Mode Card (Shown for Manual Quiz Creation)
        // -----------------------------------------------------------------
        if (creationMethod == QuizCreationMethod.MANUAL) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 13.dp, vertical = 11.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color(0xFF93C5FD),
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "Time Mode",
                            color = QuizBuilderTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // 2 Mode Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Timed Pill
                        val isTimed = timerMode == TimerMode.COUNTDOWN
                        val timedBg = if (isTimed) Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF3B82F6)))
                        else Brush.linearGradient(listOf(if (isDark) Color(0xFF0C1438) else Color(0xFFF1F5F9), if (isDark) Color(0xFF0C1438) else Color(0xFFF1F5F9)))
                        val timedBorder = if (isTimed) Color(0xFF818CF8) else (if (isDark) Color(0xFF16255C) else Color(0xFFE2E8F0))
                        val timedText = if (isTimed) Color.White else QuizBuilderTextSecondary

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(timedBg)
                                .border(1.dp, timedBorder, RoundedCornerShape(12.dp))
                                .clickable { onTimerModeChanged(TimerMode.COUNTDOWN) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = timedText,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Timed",
                                    color = timedText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }

                        // No Limit Pill
                        val isNoLimit = timerMode == TimerMode.NONE
                        val noLimitBg = if (isNoLimit) Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF3B82F6)))
                        else Brush.linearGradient(listOf(if (isDark) Color(0xFF0C1438) else Color(0xFFF1F5F9), if (isDark) Color(0xFF0C1438) else Color(0xFFF1F5F9)))
                        val noLimitBorder = if (isNoLimit) Color(0xFF818CF8) else (if (isDark) Color(0xFF16255C) else Color(0xFFE2E8F0))
                        val noLimitText = if (isNoLimit) Color.White else QuizBuilderTextSecondary

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(noLimitBg)
                                .border(1.dp, noLimitBorder, RoundedCornerShape(12.dp))
                                .clickable { onTimerModeChanged(TimerMode.NONE) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = noLimitText,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "No Limit",
                                    color = noLimitText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }

                    // Timed Configuration Section: 5m, 10m, 15m, 30m, 45m, Custom
                    if (timerMode == TimerMode.COUNTDOWN) {
                        Spacer(modifier = Modifier.height(1.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF93C5FD),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Time Limit: $timeLimitMinutes min",
                                color = QuizBuilderTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Pills Row: 5m, 10m, 15m, 30m, 45m, and Custom
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            StandardTimeLimits.forEach { min ->
                                val isSel = !isCustomTimeSelected && timeLimitMinutes == min
                                val pillBg = if (isSel) Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))
                                else Brush.linearGradient(listOf(if (isDark) Color(0xFF0F1A42) else Color(0xFFF1F5F9), if (isDark) Color(0xFF0F1A42) else Color(0xFFF1F5F9)))
                                val pillBorder = if (isSel) Color(0xFF818CF8) else (if (isDark) Color(0xFF1A2A62) else Color(0xFFE2E8F0))
                                val pillText = if (isSel) Color.White else QuizBuilderTextSecondary

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(pillBg)
                                        .border(1.dp, pillBorder, RoundedCornerShape(9.dp))
                                        .clickable {
                                            isCustomTimeSelected = false
                                            onTimeLimitMinutesChanged(min)
                                        }
                                        .padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${min}m",
                                        color = pillText,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Custom Pill after 45m
                            val isCustomActive = isCustomTimeSelected
                            val customBg = if (isCustomActive) Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))
                            else Brush.linearGradient(listOf(if (isDark) Color(0xFF0F1A42) else Color(0xFFF1F5F9), if (isDark) Color(0xFF0F1A42) else Color(0xFFF1F5F9)))
                            val customBorder = if (isCustomActive) Color(0xFF818CF8) else (if (isDark) Color(0xFF1A2A62) else Color(0xFFE2E8F0))
                            val customText = if (isCustomActive) Color.White else QuizBuilderTextSecondary

                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(customBg)
                                    .border(1.dp, customBorder, RoundedCornerShape(9.dp))
                                    .clickable {
                                        isCustomTimeSelected = true
                                        if (timeLimitMinutes in StandardTimeLimits) {
                                            onTimeLimitMinutesChanged(60)
                                        }
                                    }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Custom",
                                    color = customText,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Below: Option to select custom time (Stepper + Fast chips)
                        AnimatedVisibility(visible = isCustomTimeSelected) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(InputContainerBg)
                                    .border(1.dp, InputBorderColor, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 11.dp, vertical = 9.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Select Custom Duration",
                                        color = if (isDark) Color(0xFF93C5FD) else Color(0xFF2563EB),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "1 – 180 min",
                                        color = QuizBuilderTextMuted,
                                        fontSize = 10.5.sp
                                    )
                                }

                                // Stepper Controls
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // -5m Button
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isDark) Color(0xFF0F1A42) else Color(0xFFE2E8F0))
                                            .border(1.dp, InputBorderColor, RoundedCornerShape(8.dp))
                                            .clickable {
                                                val newVal = (timeLimitMinutes - 5).coerceIn(1, 180)
                                                onTimeLimitMinutesChanged(newVal)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-5m", color = QuizBuilderTextPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // -1m Button
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isDark) Color(0xFF0F1A42) else Color(0xFFE2E8F0))
                                            .border(1.dp, InputBorderColor, RoundedCornerShape(8.dp))
                                            .clickable {
                                                val newVal = (timeLimitMinutes - 1).coerceIn(1, 180)
                                                onTimeLimitMinutesChanged(newVal)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-1", color = QuizBuilderTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Center readout
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(InputContainerBg)
                                            .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "$timeLimitMinutes",
                                                color = QuizBuilderTextPrimary,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "min",
                                                color = QuizBuilderTextSecondary,
                                                fontSize = 11.5.sp
                                            )
                                        }
                                    }

                                    // +1m Button
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isDark) Color(0xFF0F1A42) else Color(0xFFE2E8F0))
                                            .border(1.dp, InputBorderColor, RoundedCornerShape(8.dp))
                                            .clickable {
                                                val newVal = (timeLimitMinutes + 1).coerceIn(1, 180)
                                                onTimeLimitMinutesChanged(newVal)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+1", color = QuizBuilderTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // +5m Button
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isDark) Color(0xFF0F1A42) else Color(0xFFE2E8F0))
                                            .border(1.dp, InputBorderColor, RoundedCornerShape(8.dp))
                                            .clickable {
                                                val newVal = (timeLimitMinutes + 5).coerceIn(1, 180)
                                                onTimeLimitMinutesChanged(newVal)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+5m", color = QuizBuilderTextPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Quick chips: 60m, 90m, 120m
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(60 to "60m (1 hr)", 90 to "90m (1.5 hr)", 120 to "120m (2 hr)").forEach { (presetVal, label) ->
                                        val isCurrent = timeLimitMinutes == presetVal
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(7.dp))
                                                .background(if (isCurrent) Color(0xFF4338CA) else (if (isDark) Color(0xFF0C1438) else Color(0xFFF1F5F9)))
                                                .border(1.dp, if (isCurrent) Color(0xFF818CF8) else (if (isDark) Color(0xFF1B2A60) else Color(0xFFE2E8F0)), RoundedCornerShape(7.dp))
                                                .clickable { onTimeLimitMinutesChanged(presetVal) }
                                                .padding(vertical = 5.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isCurrent) Color.White else QuizBuilderTextSecondary,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Medium
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

        // -----------------------------------------------------------------
        // 6. Allow Hints Card (Defaults to OFF)
        // -----------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, CardBorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 13.dp, vertical = 9.dp),
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
                            .background(Brush.linearGradient(listOf(Color(0xFF1D4ED8), Color(0xFF2563EB)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Allow Hints",
                            color = QuizBuilderTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Students can view clues during the quiz",
                            color = QuizBuilderTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = allowHints,
                    onCheckedChange = onAllowHintsChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF7C3AED),
                        uncheckedThumbColor = QuizBuilderTextSecondary,
                        uncheckedTrackColor = if (isDark) Color(0xFF1E2850) else Color(0xFFCBD5E1)
                    )
                )
            }
        }

        // -----------------------------------------------------------------
        // 7. If AI generation: Question Count & Question Type
        // -----------------------------------------------------------------
        if (creationMethod == QuizCreationMethod.AI_GENERATED && !isEditing) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 13.dp, vertical = 11.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Number of Questions",
                            color = QuizBuilderTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isCustomCountSelected || questionCount !in listOf(5, 10, 15, 20)) {
                            Text(
                                text = "Current: $questionCount questions",
                                color = if (isDark) Color(0xFF93C5FD) else Color(0xFF2563EB),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Preset chips: 5, 10, 15, 20 + Custom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5, 10, 15, 20).forEach { cnt ->
                            val isSelected = !isCustomCountSelected && questionCount == cnt
                            val chipBg = if (isSelected) Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))
                            else Brush.linearGradient(listOf(if (isDark) Color(0xFF0C1438) else Color(0xFFF1F5F9), if (isDark) Color(0xFF0C1438) else Color(0xFFF1F5F9)))
                            val chipBorder = if (isSelected) Color(0xFF818CF8) else (if (isDark) Color(0xFF16255C) else Color(0xFFE2E8F0))
                            val chipText = if (isSelected) Color.White else QuizBuilderTextPrimary

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(chipBg)
                                    .border(1.dp, chipBorder, RoundedCornerShape(10.dp))
                                    .clickable {
                                        isCustomCountSelected = false
                                        onQuestionCountChanged(cnt)
                                    }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$cnt", color = chipText, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            }
                        }

                        // Custom Chip
                        val customChipBg = if (isCustomCountSelected) Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))
                        else Brush.linearGradient(listOf(if (isDark) Color(0xFF0C1438) else Color(0xFFF1F5F9), if (isDark) Color(0xFF0C1438) else Color(0xFFF1F5F9)))
                        val customChipBorder = if (isCustomCountSelected) Color(0xFF818CF8) else (if (isDark) Color(0xFF16255C) else Color(0xFFE2E8F0))
                        val customChipText = if (isCustomCountSelected) Color.White else QuizBuilderTextSecondary

                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(customChipBg)
                                .border(1.dp, customChipBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    isCustomCountSelected = true
                                    if (customCountInput.isBlank()) {
                                        customCountInput = questionCount.toString()
                                    }
                                }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Custom",
                                color = customChipText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Expandable Custom Question Count Input
                    AnimatedVisibility(visible = isCustomCountSelected) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(InputContainerBg)
                                .border(1.dp, InputBorderColor, RoundedCornerShape(12.dp))
                                .padding(horizontal = 11.dp, vertical = 9.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Enter Custom Question Count",
                                    color = if (isDark) Color(0xFF93C5FD) else Color(0xFF2563EB),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "1 – 50 questions",
                                    color = QuizBuilderTextMuted,
                                    fontSize = 10.5.sp
                                )
                            }

                            // Stepper Controls + Direct Input
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // -5 Button
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDark) Color(0xFF0F1A42) else Color(0xFFE2E8F0))
                                        .border(1.dp, InputBorderColor, RoundedCornerShape(8.dp))
                                        .clickable {
                                            val newVal = (questionCount - 5).coerceIn(1, 50)
                                            customCountInput = newVal.toString()
                                            onQuestionCountChanged(newVal)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-5", color = QuizBuilderTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // -1 Button
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDark) Color(0xFF0F1A42) else Color(0xFFE2E8F0))
                                        .border(1.dp, InputBorderColor, RoundedCornerShape(8.dp))
                                        .clickable {
                                            val newVal = (questionCount - 1).coerceIn(1, 50)
                                            customCountInput = newVal.toString()
                                            onQuestionCountChanged(newVal)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-1", color = QuizBuilderTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Direct numeric input field
                                OutlinedTextField(
                                    value = customCountInput,
                                    onValueChange = { input ->
                                        val digitsOnly = input.filter { it.isDigit() }.take(2)
                                        customCountInput = digitsOnly
                                        val parsed = digitsOnly.toIntOrNull()
                                        if (parsed != null && parsed in 1..50) {
                                            onQuestionCountChanged(parsed)
                                        }
                                    },
                                    placeholder = {
                                        Text(
                                            text = "$questionCount",
                                            color = QuizBuilderTextMuted,
                                            fontSize = 13.sp
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF6366F1),
                                        unfocusedBorderColor = InputBorderColor,
                                        focusedTextColor = QuizBuilderTextPrimary,
                                        unfocusedTextColor = QuizBuilderTextPrimary,
                                        focusedContainerColor = InputContainerBg,
                                        unfocusedContainerColor = InputContainerBg
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )

                                // +1 Button
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDark) Color(0xFF0F1A42) else Color(0xFFE2E8F0))
                                        .border(1.dp, InputBorderColor, RoundedCornerShape(8.dp))
                                        .clickable {
                                            val newVal = (questionCount + 1).coerceIn(1, 50)
                                            customCountInput = newVal.toString()
                                            onQuestionCountChanged(newVal)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+1", color = QuizBuilderTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // +5 Button
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDark) Color(0xFF0F1A42) else Color(0xFFE2E8F0))
                                        .border(1.dp, InputBorderColor, RoundedCornerShape(8.dp))
                                        .clickable {
                                            val newVal = (questionCount + 5).coerceIn(1, 50)
                                            customCountInput = newVal.toString()
                                            onQuestionCountChanged(newVal)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+5", color = QuizBuilderTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Question Type",
                        color = QuizBuilderTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    // Row 1: Single & Multiple Choice
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            QuestionType.SINGLE to "Single Choice (MCQ)",
                            QuestionType.MULTIPLE to "Multiple Choice"
                        ).forEach { (type, label) ->
                            val isSelected = questionType == type
                            val typeBg = if (isSelected) Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))
                            else Brush.linearGradient(listOf(if (isDark) Color(0xFF0C1438) else Color(0xFFF1F5F9), if (isDark) Color(0xFF0C1438) else Color(0xFFF1F5F9)))
                            val typeBorder = if (isSelected) Color(0xFF818CF8) else (if (isDark) Color(0xFF16255C) else Color(0xFFE2E8F0))
                            val typeText = if (isSelected) Color.White else QuizBuilderTextSecondary

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(typeBg)
                                    .border(1.dp, typeBorder, RoundedCornerShape(10.dp))
                                    .clickable { onQuestionTypeChanged(type) }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = typeText,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }

                    // Row 2: Mixed (Single choice and Multiple choice)
                    val isMixedSelected = questionType == QuestionType.MIXED
                    val mixedBg = if (isMixedSelected) Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))
                    else Brush.linearGradient(listOf(if (isDark) Color(0xFF0C1438) else Color(0xFFF1F5F9), if (isDark) Color(0xFF0C1438) else Color(0xFFF1F5F9)))
                    val mixedBorder = if (isMixedSelected) Color(0xFF818CF8) else (if (isDark) Color(0xFF16255C) else Color(0xFFE2E8F0))
                    val mixedText = if (isMixedSelected) Color.White else QuizBuilderTextSecondary

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(mixedBg)
                            .border(1.dp, mixedBorder, RoundedCornerShape(10.dp))
                            .clickable { onQuestionTypeChanged(QuestionType.MIXED) }
                            .padding(vertical = 9.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Mixed (Single choice and Multiple choice)",
                            color = mixedText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // Generating Banner if AI Generation is in progress
        // -----------------------------------------------------------------
        AnimatedVisibility(visible = isGenerating) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF))
                    .border(1.dp, Color(0xFF818CF8), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = Color(0xFF818CF8), modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(generatingStatusText, color = QuizBuilderTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // -----------------------------------------------------------------
        // 8. Bottom Action Bar: Back & Open Question Editor Buttons
        // -----------------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onBackClick,
                enabled = !isGenerating,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isDark) Color(0xFF0A102E) else Color(0xFFF1F5F9),
                    contentColor = QuizBuilderTextPrimary
                ),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF243266) else Color(0xFFCBD5E1)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = QuizBuilderTextPrimary,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Back", color = QuizBuilderTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
            }

            val isDescriptionValid = description.trim().isNotBlank()
            val isCategoryValid = currentCategories.isNotEmpty() || category.isNotBlank()
            val isSubmitEnabled = if (isEditing) {
                !isGenerating && title.trim().isNotBlank() && isDescriptionValid && isCategoryValid
            } else if (creationMethod == QuizCreationMethod.AI_GENERATED) {
                !isGenerating && isDescriptionValid
            } else {
                !isGenerating && title.trim().isNotBlank() && isDescriptionValid && isCategoryValid
            }

            Button(
                onClick = onSubmitClick,
                enabled = isSubmitEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSubmitEnabled) ButtonGradient
                        else Brush.linearGradient(listOf(if (isDark) Color(0xFF1E284A) else Color(0xFFCBD5E1), if (isDark) Color(0xFF1E284A) else Color(0xFFCBD5E1)))
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isEditing) {
                        Icon(imageVector = Icons.Default.EditNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                        Spacer(modifier = Modifier.width(7.dp))
                        Text("Next: Review Questions", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.5.sp)
                    } else if (creationMethod == QuizCreationMethod.AI_GENERATED) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(7.dp))
                        Text("Next: Select AI Model", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.5.sp)
                    } else {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(7.dp))
                        Text("Open Question Editor", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.5.sp)
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------
    // Dialog: Create & Save Custom Category
    // -----------------------------------------------------------------
    if (showAddCategoryDialog) {
        val dialogBg = if (isDark) Color(0xFF091030) else Color.White
        val dialogBorder = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
        val dialogTextPrimary = if (isDark) Color.White else Color(0xFF0F172A)
        val dialogTextSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

        Dialog(onDismissRequest = { showAddCategoryDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = dialogBg),
                border = BorderStroke(1.dp, dialogBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF38BDF8)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Create Custom Category",
                                color = dialogTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Saved for this and future quizzes",
                                color = dialogTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    OutlinedTextField(
                        value = newCategoryInput,
                        onValueChange = {
                            newCategoryInput = it
                            newCategoryError = null
                        },
                        placeholder = {
                            Text("e.g. System Design, DevOps...", color = QuizBuilderTextMuted, fontSize = 13.sp)
                        },
                        singleLine = true,
                        isError = newCategoryError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = InputBorderColor,
                            focusedTextColor = dialogTextPrimary,
                            unfocusedTextColor = dialogTextPrimary,
                            focusedContainerColor = InputContainerBg,
                            unfocusedContainerColor = InputContainerBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (newCategoryError != null) {
                        Text(
                            text = newCategoryError ?: "",
                            color = Color(0xFFF87171),
                            fontSize = 11.5.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddCategoryDialog = false }) {
                            Text("Cancel", color = dialogTextSecondary, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val trimmed = newCategoryInput.trim()
                                if (trimmed.isBlank()) {
                                    newCategoryError = "Category name cannot be empty."
                                    return@Button
                                }
                                if (trimmed.length > 40) {
                                    newCategoryError = "Category name must be under 40 characters."
                                    return@Button
                                }
                                CategoryManager.addCustomCategory(trimmed)
                                availableCategories = CategoryManager.getAllCategories()
                                if (currentCategories.size < 5 && !currentCategories.any { it.equals(trimmed, ignoreCase = true) }) {
                                    updateCategories(currentCategories + trimmed)
                                }
                                showAddCategoryDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save Category", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------
    // Logo Picker Dialog / Modal
    // -----------------------------------------------------------------
    if (showLogoPickerModal) {
        ChooseLogoModal(
            selectedLogoId = selectedLogo,
            onLogoSelected = { logoId ->
                onLogoSelected(logoId)
                showLogoPickerModal = false
            },
            onDismiss = { showLogoPickerModal = false }
        )
    }
}




